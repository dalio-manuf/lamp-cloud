package com.dalio.cloud.base.service.user.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import com.dalio.basic.cache.repository.CacheOps;
import com.dalio.basic.exception.ArgumentException;
import com.dalio.cloud.base.entity.user.BaseEmployee;
import com.dalio.cloud.base.entity.user.BaseEmployeeOrgRel;
import com.dalio.cloud.base.entity.user.BaseEmployeeRoleRel;
import com.dalio.cloud.base.manager.user.BaseEmployeeManager;
import com.dalio.cloud.base.manager.user.BaseEmployeeOrgRelManager;
import com.dalio.cloud.base.manager.user.BaseEmployeeRoleRelManager;
import com.dalio.cloud.base.vo.save.user.BaseEmployeeRoleRelSaveVO;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * BaseEmployeeServiceImpl 单元测试 (验证员工 CRUD、角色关联和缓存管理)
 */
class BaseEmployeeServiceImplTest {

    @BeforeAll
    static void init() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, BaseEmployee.class);
        TableInfoHelper.initTableInfo(assistant, BaseEmployeeRoleRel.class);
        TableInfoHelper.initTableInfo(assistant, BaseEmployeeOrgRel.class);
    }

    @Test
    @DisplayName("测试 removeByIds 员工删除与关联清理")
    void testRemoveByIds() {
        BaseEmployeeRoleRelManager empRoleRelManager = Mockito.mock(BaseEmployeeRoleRelManager.class);
        BaseEmployeeOrgRelManager empOrgRelManager = Mockito.mock(BaseEmployeeOrgRelManager.class);
        BaseEmployeeManager empManager = Mockito.mock(BaseEmployeeManager.class);
        CacheOps cacheOps = Mockito.mock(CacheOps.class);

        BaseEmployeeServiceImpl service = new BaseEmployeeServiceImpl(empRoleRelManager, empOrgRelManager);
        ReflectionTestUtils.setField(service, "superManager", empManager);
        ReflectionTestUtils.setField(service, "cacheOps", cacheOps);

        // 1. 空列表返回 false
        assertFalse(service.removeByIds(Collections.emptyList()));

        // 2. 正常删除
        when(empManager.removeByIds(List.of(100L, 200L))).thenReturn(true);
        boolean removed = service.removeByIds(List.of(100L, 200L));
        assertTrue(removed);
        verify(empOrgRelManager).removeByEmployeeIds(List.of(100L, 200L));
        verify(empRoleRelManager).removeByEmployeeIds(List.of(100L, 200L));
        verify(cacheOps).del(anyList());
    }

    @Test
    @DisplayName("测试 saveEmployeeRole 员工角色关联保存")
    void testSaveEmployeeRole() {
        BaseEmployeeRoleRelManager empRoleRelManager = Mockito.mock(BaseEmployeeRoleRelManager.class);
        BaseEmployeeOrgRelManager empOrgRelManager = Mockito.mock(BaseEmployeeOrgRelManager.class);
        BaseEmployeeManager empManager = Mockito.mock(BaseEmployeeManager.class);
        CacheOps cacheOps = Mockito.mock(CacheOps.class);

        BaseEmployeeServiceImpl service = Mockito.spy(new BaseEmployeeServiceImpl(empRoleRelManager, empOrgRelManager));
        ReflectionTestUtils.setField(service, "superManager", empManager);
        ReflectionTestUtils.setField(service, "cacheOps", cacheOps);

        BaseEmployeeRoleRelSaveVO saveVO = new BaseEmployeeRoleRelSaveVO();
        saveVO.setEmployeeId(100L);
        saveVO.setRoleIdList(Arrays.asList(1L, 2L));
        saveVO.setFlag(true);

        doReturn(List.of(1L, 2L)).when(service).findEmployeeRoleByEmployeeId(100L);

        List<Long> result = service.saveEmployeeRole(saveVO);
        assertEquals(List.of(1L, 2L), result);
        verify(empRoleRelManager).remove(any());
        verify(empRoleRelManager).saveBatch(anyList());
        verify(cacheOps).del(any(com.dalio.basic.model.cache.CacheKey.class));
    }

    @Test
    @DisplayName("测试 saveEmployeeRole flag=false 时仅清空不新增")
    void testSaveEmployeeRoleRemoveOnly() {
        BaseEmployeeRoleRelManager empRoleRelManager = Mockito.mock(BaseEmployeeRoleRelManager.class);
        BaseEmployeeOrgRelManager empOrgRelManager = Mockito.mock(BaseEmployeeOrgRelManager.class);
        BaseEmployeeManager empManager = Mockito.mock(BaseEmployeeManager.class);
        CacheOps cacheOps = Mockito.mock(CacheOps.class);

        BaseEmployeeServiceImpl service = Mockito.spy(new BaseEmployeeServiceImpl(empRoleRelManager, empOrgRelManager));
        ReflectionTestUtils.setField(service, "superManager", empManager);
        ReflectionTestUtils.setField(service, "cacheOps", cacheOps);

        BaseEmployeeRoleRelSaveVO saveVO = new BaseEmployeeRoleRelSaveVO();
        saveVO.setEmployeeId(100L);
        saveVO.setRoleIdList(Arrays.asList(1L, 2L));
        saveVO.setFlag(false);

        doReturn(Collections.emptyList()).when(service).findEmployeeRoleByEmployeeId(100L);

        service.saveEmployeeRole(saveVO);
        verify(empRoleRelManager).remove(any());
        verify(empRoleRelManager, never()).saveBatch(anyList());
    }

    @Test
    @DisplayName("测试 saveBatchBaseEmployeeAndRole 空列表校验")
    void testSaveBatchBaseEmployeeAndRoleEmpty() {
        BaseEmployeeRoleRelManager empRoleRelManager = Mockito.mock(BaseEmployeeRoleRelManager.class);
        BaseEmployeeOrgRelManager empOrgRelManager = Mockito.mock(BaseEmployeeOrgRelManager.class);
        BaseEmployeeManager empManager = Mockito.mock(BaseEmployeeManager.class);

        BaseEmployeeServiceImpl service = new BaseEmployeeServiceImpl(empRoleRelManager, empOrgRelManager);
        ReflectionTestUtils.setField(service, "superManager", empManager);

        // 空列表应抛出异常
        assertThrows(ArgumentException.class, () -> service.saveBatchBaseEmployeeAndRole(Collections.emptyList()));
        assertThrows(ArgumentException.class, () -> service.saveBatchBaseEmployeeAndRole(null));
    }

    @Test
    @DisplayName("测试 saveEmployeeRole flag 为 null 时自动设为 true")
    void testSaveEmployeeRoleFlagNull() {
        BaseEmployeeRoleRelManager empRoleRelManager = Mockito.mock(BaseEmployeeRoleRelManager.class);
        BaseEmployeeOrgRelManager empOrgRelManager = Mockito.mock(BaseEmployeeOrgRelManager.class);
        BaseEmployeeManager empManager = Mockito.mock(BaseEmployeeManager.class);
        CacheOps cacheOps = Mockito.mock(CacheOps.class);

        BaseEmployeeServiceImpl service = Mockito.spy(new BaseEmployeeServiceImpl(empRoleRelManager, empOrgRelManager));
        ReflectionTestUtils.setField(service, "superManager", empManager);
        ReflectionTestUtils.setField(service, "cacheOps", cacheOps);

        BaseEmployeeRoleRelSaveVO saveVO = new BaseEmployeeRoleRelSaveVO();
        saveVO.setEmployeeId(200L);
        saveVO.setRoleIdList(List.of(5L));
        saveVO.setFlag(null); // flag 为 null

        doReturn(List.of(5L)).when(service).findEmployeeRoleByEmployeeId(200L);

        service.saveEmployeeRole(saveVO);
        // flag 应被设为 true，因此 saveBatch 应被调用
        verify(empRoleRelManager).saveBatch(anyList());
    }
}
