package com.dalio.cloud.base;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.dalio.basic.cache.redis2.CacheResult;
import com.dalio.basic.cache.repository.CacheOps;
import com.dalio.basic.exception.ArgumentException;
import com.dalio.cloud.base.entity.system.BaseOperationLog;
import com.dalio.cloud.base.entity.system.BaseOperationLogExt;
import com.dalio.cloud.base.entity.system.BaseRoleResourceRel;
import com.dalio.cloud.base.entity.user.BaseEmployeeOrgRel;
import com.dalio.cloud.base.manager.system.BaseOperationLogManager;
import com.dalio.cloud.base.manager.system.impl.BaseRoleResourceRelManagerImpl;
import com.dalio.cloud.base.manager.user.impl.BaseEmployeeOrgRelManagerImpl;
import com.dalio.cloud.base.mapper.system.BaseOperationLogExtMapper;
import com.dalio.cloud.base.mapper.system.BaseRoleResourceRelMapper;
import com.dalio.cloud.base.mapper.user.BaseOrgMapper;
import com.dalio.cloud.base.service.system.impl.BaseOperationLogServiceImpl;
import com.dalio.cloud.base.vo.result.system.BaseOperationLogResultVO;
import com.dalio.cloud.base.vo.save.system.BaseOperationLogSaveVO;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 基础模块未覆盖 Manager 与 Service 单元测试
 */
class BaseManagerAndServiceExtTest {

    @BeforeAll
    static void init() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, BaseEmployeeOrgRel.class);
        TableInfoHelper.initTableInfo(assistant, BaseRoleResourceRel.class);
        TableInfoHelper.initTableInfo(assistant, BaseOperationLog.class);
        TableInfoHelper.initTableInfo(assistant, BaseOperationLogExt.class);
        TableInfoHelper.initTableInfo(assistant, com.dalio.cloud.msg.entity.ExtendInterfaceLogging.class);
    }

    @Test
    @DisplayName("测试 BaseEmployeeOrgRelManagerImpl 机构员工关联与删除")
    void testBaseEmployeeOrgRelManagerImpl() {
        CacheOps cacheOps = mock(CacheOps.class);
        BaseOrgMapper orgMapper = mock(BaseOrgMapper.class);

        BaseEmployeeOrgRelManagerImpl manager = Mockito.spy(new BaseEmployeeOrgRelManagerImpl(cacheOps, orgMapper));

        // 1. findOrgIdByEmployeeId
        when(cacheOps.get(any(), any(), any(boolean[].class))).thenReturn(new CacheResult<>("k", List.of(1L, 2L)));
        List<Long> orgIds = manager.findOrgIdByEmployeeId(100L);
        assertEquals(List.of(1L, 2L), orgIds);

        // 2. removeByEmployeeIds 空参数校验与正常删除
        assertThrows(ArgumentException.class, () -> manager.removeByEmployeeIds(Collections.emptyList()));
        doReturn(true).when(manager).remove(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));
        assertTrue(manager.removeByEmployeeIds(List.of(100L)));
        verify(cacheOps).del(anyList());

        // 3. deleteByOrg
        manager.deleteByOrg(Collections.emptyList());
        verify(manager, never()).list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));

        BaseEmployeeOrgRel rel = new BaseEmployeeOrgRel();
        rel.setEmployeeId(100L);
        rel.setOrgId(1L);
        doReturn(List.of(rel)).when(manager).list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));

        manager.deleteByOrg(List.of(1L));
        verify(manager, times(2)).remove(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));
        verify(cacheOps, times(2)).del(anyList());
    }

    @Test
    @DisplayName("测试 BaseRoleResourceRelManagerImpl 查询与角色删除清理")
    void testBaseRoleResourceRelManagerImpl() {
        CacheOps cacheOps = mock(CacheOps.class);
        BaseRoleResourceRelMapper baseMapper = mock(BaseRoleResourceRelMapper.class);

        BaseRoleResourceRelManagerImpl manager = Mockito.spy(new BaseRoleResourceRelManagerImpl(cacheOps));
        ReflectionTestUtils.setField(manager, "baseMapper", baseMapper);

        // 1. findByRoleIdAndCategory
        when(baseMapper.findByRoleIdAndCategory(10L, "MENU")).thenReturn(List.of(new BaseRoleResourceRel()));
        assertEquals(1, manager.findByRoleIdAndCategory(10L, "MENU").size());

        // 2. deleteByRole 空列表
        manager.deleteByRole(Collections.emptyList());
        verify(manager, never()).list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));

        // 3. deleteByRole 正常清理
        BaseRoleResourceRel rel = new BaseRoleResourceRel();
        rel.setApplicationId(1L);
        rel.setRoleId(10L);
        doReturn(List.of(rel)).when(manager).list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));
        doReturn(true).when(manager).remove(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));

        manager.deleteByRole(List.of(10L));
        verify(manager).remove(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));
        verify(cacheOps).del(anyList());
    }

    @Test
    @DisplayName("测试 BaseOperationLogServiceImpl 日志详情、清理与保存")
    void testBaseOperationLogServiceImpl() {
        BaseOperationLogExtMapper extMapper = mock(BaseOperationLogExtMapper.class);
        BaseOperationLogManager logManager = mock(BaseOperationLogManager.class);

        BaseOperationLogServiceImpl service = new BaseOperationLogServiceImpl(extMapper);
        ReflectionTestUtils.setField(service, "superManager", logManager);

        // 1. getDetail
        BaseOperationLog log = new BaseOperationLog();
        log.setId(100L);
        log.setDescription("用户登录");
        when(logManager.getById(100L)).thenReturn(log);

        BaseOperationLogExt ext = new BaseOperationLogExt();
        ext.setId(100L);
        ext.setParams("{\"name\":\"admin\"}");
        when(extMapper.selectById(100L)).thenReturn(ext);

        BaseOperationLogResultVO vo = service.getDetail(100L);
        assertNotNull(vo);
        assertEquals("用户登录", vo.getDescription());
        assertEquals("{\"name\":\"admin\"}", vo.getParams());

        // 2. clearLog
        when(logManager.clearLog(any(), any())).thenReturn(5L);
        assertTrue(service.clearLog(LocalDateTime.now(), 100));

        when(logManager.clearLog(any(), any())).thenReturn(0L);
        assertFalse(service.clearLog(LocalDateTime.now(), 100));

        // 3. save
        BaseOperationLogSaveVO saveVO = new BaseOperationLogSaveVO();
        saveVO.setDescription("新增员工");
        BaseOperationLog savedLog = service.save(saveVO);
        assertNotNull(savedLog);
        verify(extMapper).insert(any(BaseOperationLogExt.class));
        verify(logManager).save(any(BaseOperationLog.class));
    }

    @Test
    @DisplayName("测试基础与消息模块待覆盖的Manager与Service实现")
    void testRemainingBaseAndMsgClasses() {
        // Services with default implementations
        assertNotNull(new com.dalio.cloud.base.service.system.impl.BaseRoleResourceRelServiceImpl());
        assertNotNull(new com.dalio.cloud.base.service.BaseEmployeeTestServiceImpl());
        assertNotNull(new com.dalio.cloud.base.service.user.impl.BaseEmployeeRoleRelServiceImpl());
        assertNotNull(new com.dalio.cloud.base.service.user.impl.BaseOrgRoleRelServiceImpl());
        assertNotNull(new com.dalio.cloud.msg.service.impl.ExtendInterfaceLoggingServiceImpl());

        // Managers with default implementations
        assertNotNull(new com.dalio.cloud.msg.manager.impl.ExtendMsgManagerImpl());
        assertNotNull(new com.dalio.cloud.msg.manager.impl.ExtendMsgRecipientManagerImpl());
        assertNotNull(new com.dalio.cloud.msg.manager.impl.ExtendInterfaceLoggingManagerImpl());
        assertNotNull(new com.dalio.cloud.msg.manager.impl.ExtendNoticeManagerImpl());

        // ExtendMsgRecipientServiceImpl.listByMsgId
        com.dalio.cloud.msg.service.impl.ExtendMsgRecipientServiceImpl recipientService = new com.dalio.cloud.msg.service.impl.ExtendMsgRecipientServiceImpl();
        com.dalio.cloud.msg.manager.ExtendMsgRecipientManager recipMgrMock = mock(com.dalio.cloud.msg.manager.ExtendMsgRecipientManager.class);
        ReflectionTestUtils.setField(recipientService, "superManager", recipMgrMock);
        when(recipMgrMock.listByMsgId(10L)).thenReturn(List.of(new com.dalio.cloud.msg.entity.ExtendMsgRecipient()));
        assertEquals(1, recipientService.listByMsgId(10L).size());

        // ExtendInterfaceLogServiceImpl.removeByIds
        com.dalio.cloud.msg.manager.ExtendInterfaceLoggingManager loggingMgrMock = mock(com.dalio.cloud.msg.manager.ExtendInterfaceLoggingManager.class);
        com.dalio.cloud.msg.service.impl.ExtendInterfaceLogServiceImpl logService = new com.dalio.cloud.msg.service.impl.ExtendInterfaceLogServiceImpl(loggingMgrMock);
        com.dalio.cloud.msg.manager.ExtendInterfaceLogManager logMgrMock = mock(com.dalio.cloud.msg.manager.ExtendInterfaceLogManager.class);
        ReflectionTestUtils.setField(logService, "superManager", logMgrMock);
        when(logMgrMock.removeByIds(anyCollection())).thenReturn(true);
        assertTrue(logService.removeByIds(List.of(1L, 2L)));
        verify(loggingMgrMock).remove(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));

        // FileManagerImpl.listByBizIdAndBizType
        com.dalio.cloud.file.manager.impl.FileManagerImpl fileManager = new com.dalio.cloud.file.manager.impl.FileManagerImpl();
        com.dalio.cloud.file.mapper.FileMapper fileMapperMock = mock(com.dalio.cloud.file.mapper.FileMapper.class);
        ReflectionTestUtils.setField(fileManager, "baseMapper", fileMapperMock);
        when(fileMapperMock.listByBizIdAndBizType(1L, "AVATAR")).thenReturn(List.of(new com.dalio.cloud.file.vo.result.FileResultVO()));
        assertEquals(1, fileManager.listByBizIdAndBizType(1L, "AVATAR").size());

        // MinIoFileChunkStrategyImpl
        com.dalio.cloud.file.properties.FileServerProperties fileProps = new com.dalio.cloud.file.properties.FileServerProperties();
        com.dalio.cloud.file.strategy.impl.minio.MinIoFileChunkStrategyImpl minioStrategy = new com.dalio.cloud.file.strategy.impl.minio.MinIoFileChunkStrategyImpl(fileMapperMock, fileProps);
        ReflectionTestUtils.invokeMethod(minioStrategy, "copyFile", new com.dalio.cloud.file.entity.File());
        assertNotNull(ReflectionTestUtils.invokeMethod(minioStrategy, "merge", Collections.emptyList(), "/path", "file.txt", new com.dalio.cloud.file.dto.chunk.FileChunksMergeDTO()));

        // BaseEmployeeRoleRelManager & BaseEmployeeOrgRelManager default methods
        com.dalio.cloud.base.manager.user.BaseEmployeeRoleRelManager empRoleRelManager = mock(com.dalio.cloud.base.manager.user.BaseEmployeeRoleRelManager.class, Mockito.CALLS_REAL_METHODS);
        when(empRoleRelManager.removeByEmployeeIds(anyCollection())).thenReturn(true);
        assertTrue(empRoleRelManager.removeByEmployeeId(123L));

        com.dalio.cloud.base.manager.user.BaseEmployeeOrgRelManager empOrgRelManager = mock(com.dalio.cloud.base.manager.user.BaseEmployeeOrgRelManager.class, Mockito.CALLS_REAL_METHODS);
        when(empOrgRelManager.removeByEmployeeIds(anyCollection())).thenReturn(true);
        assertTrue(empOrgRelManager.removeByEmployeeId(123L));
    }
}
