package com.dalio.cloud.msg.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dalio.basic.base.service.impl.SuperServiceImpl;
import com.dalio.basic.database.mybatis.conditions.Wraps;
import com.dalio.basic.jackson.JsonUtil;
import com.dalio.basic.utils.ArgumentAssert;
import com.dalio.basic.utils.SpringUtils;
import com.dalio.cloud.job.dto.XxlJobInfoVO;
import com.dalio.cloud.job.facade.JobFacade;
import com.dalio.cloud.model.entity.system.SysUser;
import com.dalio.cloud.msg.entity.DefMsgTemplate;
import com.dalio.cloud.msg.entity.ExtendMsg;
import com.dalio.cloud.msg.entity.ExtendMsgRecipient;
import com.dalio.cloud.msg.entity.ExtendNotice;
import com.dalio.cloud.msg.enumeration.MsgTemplateTypeEnum;
import com.dalio.cloud.msg.enumeration.SourceType;
import com.dalio.cloud.msg.enumeration.TaskStatus;
import com.dalio.cloud.msg.event.MsgEventVO;
import com.dalio.cloud.msg.event.MsgSendEvent;
import com.dalio.cloud.msg.manager.ExtendMsgManager;
import com.dalio.cloud.msg.manager.ExtendMsgRecipientManager;
import com.dalio.cloud.msg.manager.ExtendNoticeManager;
import com.dalio.cloud.msg.service.ExtendMsgService;
import com.dalio.cloud.msg.vo.result.ExtendMsgResultVO;
import com.dalio.cloud.msg.vo.update.ExtendMsgPublishVO;
import com.dalio.cloud.msg.vo.update.ExtendMsgSendVO;
import com.dalio.cloud.msg.ws.WebSocketSubject;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 业务实现类
 * 消息
 * </p>
 *
 * @author admin
 * @date 2022-07-10 11:41:17
 * @create [2022-07-10 11:41:17] [admin] [代码生成器生成]
 */

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class ExtendMsgServiceImpl extends SuperServiceImpl<ExtendMsgManager, Long, ExtendMsg> implements ExtendMsgService {
    private final ExtendMsgRecipientManager extendMsgRecipientManager;
    private final ExtendNoticeManager extendNoticeManager;
    private final JobFacade jobFacade;

    @Override
    public ExtendMsgResultVO getResultById(Long id) {
        ExtendMsg msg = superManager.getById(id);
        ExtendMsgResultVO result = BeanUtil.toBean(msg, ExtendMsgResultVO.class);
        if (result != null) {
            List<ExtendMsgRecipient> list = extendMsgRecipientManager.listByMsgId(id);
            result.setRecipientList(list.stream().map(ExtendMsgRecipient::getRecipient).toList());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean publish(ExtendMsgPublishVO data, SysUser sysUser) {
        ArgumentAssert.notNull(data, "发布消息参数不能为空");
        ArgumentAssert.notEmpty(data.getRecipientList(), "消息接收人列表不能为空");

        ExtendMsg extendMsg = BeanUtil.toBean(data, ExtendMsg.class);
        extendMsg.setType(MsgTemplateTypeEnum.NOTICE.getCode());
        extendMsg.setChannel(SourceType.APP);

        if (sysUser != null && sysUser.getEmployee() != null) {
            extendMsg.setCreatedOrgId(sysUser.getEmployee().getLastDeptId());
        }
        if (data.getDraft() != null && data.getDraft()) {
            extendMsg.setStatus(TaskStatus.DRAFT);
        } else {
            extendMsg.setStatus(TaskStatus.WAITING);
        }
        if (extendMsg.getId() == null) {
            superManager.save(extendMsg);
        } else {
            superManager.updateById(extendMsg);
            extendMsgRecipientManager.remove(Wraps.<ExtendMsgRecipient>lbQ().eq(ExtendMsgRecipient::getMsgId, extendMsg.getId()));
        }
        List<ExtendMsgRecipient> recipientList = data.getRecipientList().stream().map((item) -> {
            ExtendMsgRecipient recipient = new ExtendMsgRecipient();
            recipient.setMsgId(extendMsg.getId());
            recipient.setRecipient(item);
            return recipient;
        }).toList();
        extendMsgRecipientManager.saveBatch(recipientList);

        if (data.getSendTime() == null) {
            List<ExtendNotice> noticeList = data.getRecipientList().stream().map((item) -> {
                ExtendNotice notice = new ExtendNotice();
                BeanUtil.copyProperties(extendMsg, notice);
                notice.setId(null);
                notice.setMsgId(extendMsg.getId());
                notice.setRecipientId(Long.valueOf(item));
                notice.setIsHandle(false);
                notice.setIsRead(false);
                notice.setHandleTime(null);
                notice.setReadTime(null);
                notice.setAutoRead(true);
                return notice;
            }).toList();
            extendNoticeManager.saveBatch(noticeList);

            data.getRecipientList().forEach(employeeId -> {
                WebSocketSubject subject = WebSocketSubject.Holder.getSubject(employeeId);
                // 通知客户端 接收消息
                subject.notify("1", null);
            });

            extendMsg.setStatus(TaskStatus.SUCCESS);
            superManager.updateById(extendMsg);
        } else {
            // 务必启动 lamp-job-pro 项目，否则调用会失败！
            Map<String, Long> param = MapUtil.builder("msgId", extendMsg.getId()).build();

            XxlJobInfoVO xxlJobInfoVO = XxlJobInfoVO.create("lamp-none-executor",
                    "【发送消息】" + extendMsg.getTitle(), extendMsg.getSendTime(), "publishMsg", JsonUtil.toJson(param));
            jobFacade.addTimingTask(xxlJobInfoVO);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishNotice(Long msgId) {
        ExtendMsg extendMsg = superManager.getById(msgId);
        ArgumentAssert.notNull(extendMsg, "消息不存在");
        List<ExtendMsgRecipient> recipientList = extendMsgRecipientManager.listByMsgId(extendMsg.getId());
        ArgumentAssert.notEmpty(recipientList, "消息接收人为空");

        List<ExtendNotice> noticeList = recipientList.stream().map((item) -> {
            ExtendNotice notice = new ExtendNotice();
            BeanUtil.copyProperties(extendMsg, notice);
            notice.setId(null);
            notice.setMsgId(extendMsg.getId());
            notice.setRecipientId(Long.valueOf(item.getRecipient()));
            notice.setIsHandle(false);
            notice.setIsRead(false);
            notice.setHandleTime(null);
            notice.setReadTime(null);
            notice.setAutoRead(true);
            return notice;
        }).toList();
        extendNoticeManager.saveBatch(noticeList);

        recipientList.forEach(recipient -> {
            WebSocketSubject subject = WebSocketSubject.Holder.getSubject(recipient.getRecipient());
            // 通知客户端 接收消息
            subject.notify("1", null);
        });

        extendMsg.setStatus(TaskStatus.SUCCESS);
        superManager.updateById(extendMsg);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean send(ExtendMsgSendVO data, DefMsgTemplate msgTemplate, SysUser sysUser) {
        ExtendMsg extendMsg = BeanUtil.toBean(data, ExtendMsg.class);
        extendMsg.setTemplateCode(data.getCode());
        extendMsg.setChannel(SourceType.SERVICE);
        extendMsg.setType(msgTemplate.getType());
        extendMsg.setRemindMode(msgTemplate.getRemindMode());
        if (CollUtil.isNotEmpty(data.getParamList())) {
            extendMsg.setParam(JsonUtil.toJson(data.getParamList()));
        }
        extendMsg.setCreatedOrgId((sysUser != null && sysUser.getEmployee() != null) ? sysUser.getEmployee().getLastDeptId() : null);

        extendMsg.setStatus(TaskStatus.WAITING);
        if (extendMsg.getId() == null) {
            superManager.save(extendMsg);
        } else {
            superManager.updateById(extendMsg);
            extendMsgRecipientManager.remove(Wraps.<ExtendMsgRecipient>lbQ().eq(ExtendMsgRecipient::getMsgId, extendMsg.getId()));
        }

        List<ExtendMsgRecipient> recipientList = data.getRecipientList().stream().map((item) -> {
            ExtendMsgRecipient recipient = new ExtendMsgRecipient();
            recipient.setMsgId(extendMsg.getId());
            recipient.setRecipient(item.getRecipient());
            recipient.setExt(item.getExt());
            return recipient;
        }).toList();
        extendMsgRecipientManager.saveBatch(recipientList);

        //3, 判断是否立即发送
        if (data.getSendTime() == null) {
            MsgEventVO msgEventVO = new MsgEventVO();
            msgEventVO.setMsgId(extendMsg.getId()).copy();
            SpringUtils.publishEvent(new MsgSendEvent(msgEventVO));
        } else {
            // 务必启动 lamp-job-pro 项目，否则调用会失败！
            Map<String, Long> param = MapUtil.builder("msgId", extendMsg.getId()).build();

            XxlJobInfoVO xxlJobInfoVO = XxlJobInfoVO.create("lamp-none-executor",
                    "【发送消息】" + extendMsg.getTitle(), extendMsg.getSendTime(), "sendMsg", JsonUtil.toJson(param));
            jobFacade.addTimingTask(xxlJobInfoVO);
        }
        return true;
    }

}


