package com.dalio.cloud.msg.strategy;

import cn.hutool.core.util.StrUtil;
import com.dalio.basic.jackson.JsonUtil;
import com.dalio.basic.model.Kv;
import com.dalio.cloud.common.utils.FreeMarkerUtil;
import com.dalio.cloud.msg.entity.DefMsgTemplate;
import com.dalio.cloud.msg.entity.ExtendMsg;
import com.dalio.cloud.msg.glue.GlueFactory;
import com.dalio.cloud.msg.strategy.domain.MsgParam;
import com.dalio.cloud.msg.strategy.domain.MsgResult;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author admin
 * @date 2022/7/10 0010 14:18
 */
public interface MsgStrategy {
    /**
     * 解析参数
     *
     * @param param param
     * @return java.util.Map<java.lang.String, java.lang.String>
     * @author admin
     * @date 2022/10/28 4:58 PM
     * @create [2022/10/28 4:58 PM ] [admin] [初始创建]
     */
    default Map<String, String> parseParam(String param) {
        Map<String, String> map = new LinkedHashMap<>();
        if (StrUtil.isNotEmpty(param)) {
            List<Kv> list = JsonUtil.parseArray(param, Kv.class);
            for (Kv kv : list) {
                map.put(kv.getKey(), kv.getValue());
            }
        }
        return map;
    }

    /**
     * 替换变量
     *
     * @param extendMsg         extendMsg
     * @param extendMsgTemplate extendMsgTemplate
     * @return com.dalio.cloud.msg.strategy.domain.MsgResult
     * @author admin
     * @date 2022/10/28 4:58 PM
     * @create [2022/10/28 4:58 PM ] [admin] [初始创建]
     */
    default MsgResult replaceVariable(ExtendMsg extendMsg, DefMsgTemplate extendMsgTemplate) {
        String script = extendMsgTemplate.getScript();
        String templateContent = extendMsgTemplate.getContent();
        String templateTitle = extendMsgTemplate.getTitle();
        Map<String, Object> params = new LinkedHashMap<>();
        if (StrUtil.isNotEmpty(extendMsg.getParam())) {
            List<Kv> list = JsonUtil.parseArray(extendMsg.getParam(), Kv.class);
            for (Kv kv : list) {
                params.put(kv.getKey(), kv.getValue());
            }
        }
        Map<String, Object> resultParams = params;
        String title = templateTitle;
        String content = templateContent;
        if (StrUtil.isNotEmpty(script)) {
            resultParams = (Map<String, Object>) GlueFactory.getInstance().exeGroovyScript(script, params);
        }
        if (StrUtil.isNotEmpty(templateTitle)) {
            title = FreeMarkerUtil.generateString(templateTitle, resultParams);
        }
        if (StrUtil.isNotEmpty(templateContent)) {
            content = FreeMarkerUtil.generateString(templateContent, resultParams);
        }

        return MsgResult.builder().title(title).content(content).build();
    }

    /**
     * 执行发送
     *
     * @param msgParam msgParam
     * @return com.dalio.cloud.msg.strategy.domain.MsgResult
     * @throws Exception 异常
     * @author admin
     * @date 2022/10/28 4:58 PM
     * @create [2022/10/28 4:58 PM ] [admin] [初始创建]
     */
    MsgResult exec(MsgParam msgParam) throws Exception;

    /**
     * 是否执行成功
     *
     * @param result 执行函数的返回值
     * @return
     */
    default boolean isSuccess(MsgResult result) {
        return true;
    }
}
