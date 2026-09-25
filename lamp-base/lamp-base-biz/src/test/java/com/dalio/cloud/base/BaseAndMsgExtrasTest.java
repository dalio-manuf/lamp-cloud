package com.dalio.cloud.base;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.dalio.basic.exception.BizException;
import com.dalio.cloud.base.biz.system.BaseRoleBiz;
import com.dalio.cloud.base.entity.system.BaseRole;
import com.dalio.cloud.base.service.system.BaseRoleService;
import com.dalio.cloud.file.enumeration.FileStorageType;
import com.dalio.cloud.file.properties.FileServerProperties;
import com.dalio.cloud.file.strategy.FileLock;
import com.dalio.cloud.model.enumeration.base.RoleCategoryEnum;
import com.dalio.cloud.model.enumeration.system.DataTypeEnum;
import com.dalio.cloud.msg.biz.MsgBiz;
import com.dalio.cloud.msg.entity.ExtendInterfaceLog;
import com.dalio.cloud.msg.entity.ExtendMsg;
import com.dalio.cloud.msg.event.MsgEventVO;
import com.dalio.cloud.msg.event.MsgSendEvent;
import com.dalio.cloud.msg.event.listener.MsgSendListener;
import com.dalio.cloud.msg.manager.impl.ExtendInterfaceLogManagerImpl;
import com.dalio.cloud.msg.mapper.ExtendInterfaceLogMapper;
import com.dalio.cloud.msg.service.ExtendMsgService;
import com.dalio.cloud.msg.strategy.domain.MsgParam;
import com.dalio.cloud.msg.strategy.domain.MsgResult;
import com.dalio.cloud.msg.strategy.impl.TestMsgStrategyImpl;
import com.dalio.cloud.msg.ws.WebSocketObserver;
import com.dalio.cloud.system.service.application.DefResourceService;
import jakarta.websocket.RemoteEndpoint;
import jakarta.websocket.Session;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 基础与消息模块零散策略、监听器、工具与Biz测试
 */
class BaseAndMsgExtrasTest {

    @BeforeAll
    static void init() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, ExtendInterfaceLog.class);
    }

    @Test
    @DisplayName("测试 BaseRoleBiz 资源权限查询")
    void testBaseRoleBiz() {
        BaseRoleService roleService = mock(BaseRoleService.class);
        DefResourceService resourceService = mock(DefResourceService.class);
        BaseRoleBiz roleBiz = new BaseRoleBiz(roleService, resourceService);

        // 1. role 不存在
        when(roleService.getById(1L)).thenReturn(null);
        assertTrue(roleBiz.findResourceIdByRoleId(1L).isEmpty());

        // 2. 超级管理员
        BaseRole adminRole = new BaseRole();
        adminRole.setId(2L);
        adminRole.setType(DataTypeEnum.SYSTEM.getCode());
        when(roleService.getById(2L)).thenReturn(adminRole);
        when(resourceService.findResource()).thenReturn(Map.of(10L, List.of(100L)));

        Map<Long, Collection<Long>> adminRes = roleBiz.findResourceIdByRoleId(2L);
        assertEquals(1, adminRes.size());

        // 3. 普通角色
        BaseRole normalRole = new BaseRole();
        normalRole.setId(3L);
        normalRole.setType(DataTypeEnum.BUSINESS.getCode());
        when(roleService.getById(3L)).thenReturn(normalRole);
        when(roleService.findResourceIdByRoleId(3L, RoleCategoryEnum.FUNCTION)).thenReturn(Map.of(20L, List.of(200L)));

        Map<Long, Collection<Long>> normalRes = roleBiz.findResourceIdByRoleId(3L);
        assertEquals(1, normalRes.size());
    }

    @Test
    @DisplayName("测试 FileLock 工具类")
    void testFileLock() {
        Lock lock1 = FileLock.getLock("test_key");
        assertNotNull(lock1);
        Lock lock2 = FileLock.getLock("test_key");
        assertSame(lock1, lock2);

        FileLock.removeLock("test_key");
        Lock lock3 = FileLock.getLock("test_key");
        assertNotSame(lock1, lock3);
        FileLock.removeLock("test_key");
    }

    @Test
    @DisplayName("测试 FileServerProperties 配置属性校验与 URL 拼接")
    void testFileServerProperties() {
        FileServerProperties props = new FileServerProperties();
        props.setStorageType(FileStorageType.LOCAL);
        props.setDelFile(true);
        props.setPublicBucket(Set.of("public-bucket"));

        assertEquals(FileStorageType.LOCAL, props.getStorageType());
        assertTrue(props.getDelFile());
        assertEquals(Set.of("public-bucket"), props.getPublicBucket());

        // validSuffix
        assertThrows(BizException.class, () -> props.validSuffix("test.png"));
        props.setSuffix("jpg,png");
        assertThrows(BizException.class, () -> props.validSuffix(null));
        assertThrows(BizException.class, () -> props.validSuffix(""));
        assertTrue(props.validSuffix("avatar.png"));
        assertFalse(props.validSuffix("document.pdf"));

        // FastDfs URL prefix
        FileServerProperties.FastDfs fastDfs = props.getFastDfs();
        fastDfs.setUrlPrefix("http://dfs.test.com");
        assertEquals("http://dfs.test.com/", fastDfs.getUrlPrefix());

        // Local prefixes
        FileServerProperties.Local local = props.getLocal();
        local.setUrlPrefix("http://local.test.com");
        local.setInnerUrlPrefix("http://inner.test.com");
        local.setStoragePath("/tmp/storage");
        assertEquals("http://local.test.com/", local.getUrlPrefix());
        assertEquals("http://inner.test.com/", local.getInnerUrlPrefix());
        assertTrue(local.getStoragePath().endsWith(java.io.File.separator));

        // Huawei
        FileServerProperties.Huawei huawei = props.getHuawei();
        huawei.setEndpoint("obs.huawei.com");
        huawei.setBucket("my-bucket");
        huawei.setUrlPrefix("");
        assertEquals("my-bucket.obs.huawei.com", huawei.getUrlPrefix());
        huawei.setUrlPrefix("http://huawei.custom.com");
        assertEquals("http://huawei.custom.com/", huawei.getUrlPrefix());

        // Ali
        FileServerProperties.Ali ali = props.getAli();
        ali.setEndpoint("oss-cn-hangzhou.aliyuncs.com");
        ali.setBucket("ali-bucket");
        ali.setUrlPrefix("");
        assertEquals("ali-bucket.oss-cn-hangzhou.aliyuncs.com", ali.getUrlPrefix());
        ali.setUrlPrefix("http://ali.custom.com");
        assertEquals("http://ali.custom.com/", ali.getUrlPrefix());

        // QiNiu
        FileServerProperties.QiNiu qiNiu = props.getQiNiu();
        qiNiu.setDomain("qiniu.cdn.com");
        qiNiu.setUseHttps(false);
        assertEquals("http://qiniu.cdn.com/", qiNiu.getUrlPrefix());
        qiNiu.setUseHttps(true);
        assertEquals("https://qiniu.cdn.com/", qiNiu.getUrlPrefix());

        // MinIo
        FileServerProperties.MinIo minIo = props.getMinIo();
        minIo.setEndpoint("http://minio.test.com:9000");
        assertEquals("http://minio.test.com:9000", minIo.getUrlPrefix());
    }

    @Test
    @DisplayName("测试 TestMsgStrategyImpl 执行")
    void testTestMsgStrategyImpl() {
        TestMsgStrategyImpl strategy = new TestMsgStrategyImpl();
        ExtendMsgService msgService = mock(ExtendMsgService.class);
        ReflectionTestUtils.setField(strategy, "extendMsgService", msgService);

        ExtendMsg msg = new ExtendMsg();
        msg.setId(99L);
        when(msgService.getById(99L)).thenReturn(msg);

        MsgParam param = MsgParam.builder().extendMsg(msg).build();
        MsgResult result = strategy.exec(param);
        assertNotNull(result);
        assertEquals("保存成功", result.getResult());
    }

    @Test
    @DisplayName("测试 MsgSendListener 事件监听与发送")
    void testMsgSendListener() {
        MsgBiz msgBiz = mock(MsgBiz.class);
        MsgSendListener listener = new MsgSendListener(msgBiz);

        MsgEventVO vo = new MsgEventVO();
        vo.setMsgId(888L);
        MsgSendEvent event = new MsgSendEvent(vo);

        listener.handleMsg(event);
        verify(msgBiz).execSend(888L);
    }

    @Test
    @DisplayName("测试 ExtendInterfaceLogManagerImpl 方法")
    void testExtendInterfaceLogManagerImpl() {
        ExtendInterfaceLogMapper mapper = mock(ExtendInterfaceLogMapper.class);
        ExtendInterfaceLogManagerImpl manager = spy(new ExtendInterfaceLogManagerImpl());
        ReflectionTestUtils.setField(manager, "baseMapper", mapper);

        ExtendInterfaceLog log = new ExtendInterfaceLog();
        log.setId(10L);
        log.setInterfaceId(55L);
        doReturn(log).when(manager).getOne(any(Wrapper.class));

        assertEquals(log, manager.getByInterfaceId(55L));

        manager.incrSuccessCount(10L);
        verify(mapper).incrSuccessCount(eq(10L), any(LocalDateTime.class));

        manager.incrFailCount(10L);
        verify(mapper).incrFailCount(eq(10L), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("测试 WebSocketObserver 消息推送、异常与 equals/hashCode")
    void testWebSocketObserver() throws Exception {
        Session session1 = mock(Session.class);
        RemoteEndpoint.Basic basicRemote1 = mock(RemoteEndpoint.Basic.class);
        when(session1.getId()).thenReturn("s1");
        when(session1.isOpen()).thenReturn(true);
        when(session1.getBasicRemote()).thenReturn(basicRemote1);

        WebSocketObserver observer1 = new WebSocketObserver(session1);
        assertEquals(session1, observer1.getSession());

        // 正常发送
        observer1.update(null, "hello");
        verify(basicRemote1).sendText("hello");

        // 抛异常不中断
        doThrow(new IOException("conn error")).when(basicRemote1).sendText("error");
        assertDoesNotThrow(() -> observer1.update(null, "error"));

        // session 关闭不发送
        when(session1.isOpen()).thenReturn(false);
        observer1.update(null, "closed");
        verify(basicRemote1, never()).sendText("closed");

        // null session
        WebSocketObserver nullObserver = new WebSocketObserver(null);
        assertDoesNotThrow(() -> nullObserver.update(null, "msg"));

        // equals & hashCode
        Session session2 = mock(Session.class);
        when(session2.getId()).thenReturn("s1");
        WebSocketObserver observer2 = new WebSocketObserver(session2);

        Session session3 = mock(Session.class);
        when(session3.getId()).thenReturn("s3");
        WebSocketObserver observer3 = new WebSocketObserver(session3);

        assertEquals(observer1, observer1);
        assertEquals(observer1, observer2);
        assertNotEquals(observer1, observer3);
        assertNotEquals(observer1, null);
        assertNotEquals(observer1, "some_str");
        assertEquals(observer1.hashCode(), observer2.hashCode());
        assertNotNull(nullObserver.hashCode());
    }
}
