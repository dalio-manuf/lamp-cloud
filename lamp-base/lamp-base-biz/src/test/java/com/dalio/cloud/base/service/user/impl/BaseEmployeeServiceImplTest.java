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

    @Test
    @DisplayName("测试 updateOrgInfo 更新员工机构并淘汰缓存")
    void testUpdateOrgInfo() {
        BaseEmployeeRoleRelManager empRoleRelManager = Mockito.mock(BaseEmployeeRoleRelManager.class);
        BaseEmployeeOrgRelManager empOrgRelManager = Mockito.mock(BaseEmployeeOrgRelManager.class);
        BaseEmployeeManager empManager = Mockito.mock(BaseEmployeeManager.class);

        BaseEmployeeServiceImpl service = new BaseEmployeeServiceImpl(empRoleRelManager, empOrgRelManager);
        ReflectionTestUtils.setField(service, "superManager", empManager);

        service.updateOrgInfo(100L, 1L, 2L);
        verify(empManager).update(any());
        verify(empManager).delCache(100L);
    }

    @Test
    @DisplayName("测试 save 与 updateById 关联保存员工与机构")
    void testSaveAndUpdateEmployee() {
        BaseEmployeeRoleRelManager empRoleRelManager = Mockito.mock(BaseEmployeeRoleRelManager.class);
        BaseEmployeeOrgRelManager empOrgRelManager = Mockito.mock(BaseEmployeeOrgRelManager.class);
        BaseEmployeeManager empManager = Mockito.mock(BaseEmployeeManager.class);
        CacheOps cacheOps = Mockito.mock(CacheOps.class);

        BaseEmployeeServiceImpl service = new BaseEmployeeServiceImpl(empRoleRelManager, empOrgRelManager);
        ReflectionTestUtils.setField(service, "superManager", empManager);
        ReflectionTestUtils.setField(service, "cacheOps", cacheOps);

        com.dalio.cloud.base.vo.save.user.BaseEmployeeSaveVO saveVO = new com.dalio.cloud.base.vo.save.user.BaseEmployeeSaveVO();
        saveVO.setRealName("王五");
        saveVO.setOrgIdList(List.of(10L, 20L));

        BaseEmployee saved = service.save(saveVO);
        assertNotNull(saved);
        assertEquals("王五", saved.getRealName());
        verify(empManager).save(any(BaseEmployee.class));
        verify(empOrgRelManager).removeByEmployeeId(any());
        verify(empOrgRelManager).saveBatch(anyList());
        verify(cacheOps).del(any(com.dalio.basic.model.cache.CacheKey.class));

        // updateById
        com.dalio.cloud.base.vo.update.user.BaseEmployeeUpdateVO updateVO = new com.dalio.cloud.base.vo.update.user.BaseEmployeeUpdateVO();
        updateVO.setId(100L);
        updateVO.setRealName("王五修改");
        updateVO.setOrgIdList(List.of(10L));

        BaseEmployee updated = service.updateById(updateVO);
        assertNotNull(updated);
        assertEquals(100L, updated.getId());
        verify(empManager).updateById(any(BaseEmployee.class));
    }

    @Test
    @DisplayName("测试 saveBatchBaseEmployeeAndRole 成功绑定角色")
    void testSaveBatchBaseEmployeeAndRoleSuccess() {
        BaseEmployeeRoleRelManager empRoleRelManager = Mockito.mock(BaseEmployeeRoleRelManager.class);
        BaseEmployeeOrgRelManager empOrgRelManager = Mockito.mock(BaseEmployeeOrgRelManager.class);
        BaseEmployeeManager empManager = Mockito.mock(BaseEmployeeManager.class);

        BaseEmployeeServiceImpl service = new BaseEmployeeServiceImpl(empRoleRelManager, empOrgRelManager);
        ReflectionTestUtils.setField(service, "superManager", empManager);

        BaseEmployee emp = new BaseEmployee();
        emp.setId(300L);
        when(empRoleRelManager.bindRole(eq(List.of(300L)), anyString())).thenReturn(true);

        assertTrue(service.saveBatchBaseEmployeeAndRole(List.of(emp)));
        verify(empManager).saveBatch(anyList());
        verify(empRoleRelManager).bindRole(eq(List.of(300L)), eq(com.dalio.cloud.common.constant.RoleConstant.TENANT_ADMIN));
    }

    @Test
    @DisplayName("测试 delegate 方法: findPageResultVO, updateAllById, getEmployeeByUser, listEmployeeByUserId")
    void testDelegateMethods() {
        BaseEmployeeRoleRelManager empRoleRelManager = Mockito.mock(BaseEmployeeRoleRelManager.class);
        BaseEmployeeOrgRelManager empOrgRelManager = Mockito.mock(BaseEmployeeOrgRelManager.class);
        BaseEmployeeManager empManager = Mockito.mock(BaseEmployeeManager.class);

        BaseEmployeeServiceImpl service = new BaseEmployeeServiceImpl(empRoleRelManager, empOrgRelManager);
        ReflectionTestUtils.setField(service, "superManager", empManager);

        // findPageResultVO
        com.dalio.basic.base.request.PageParams<com.dalio.cloud.base.vo.query.user.BaseEmployeePageQuery> params = new com.dalio.basic.base.request.PageParams<>();
        com.dalio.cloud.base.vo.query.user.BaseEmployeePageQuery query = new com.dalio.cloud.base.vo.query.user.BaseEmployeePageQuery();
        query.setRealName("测试");
        query.setUserIdList(List.of(1L));
        params.setModel(query);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.dalio.cloud.base.vo.result.user.BaseEmployeeResultVO> mockPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>();
        when(empManager.selectPageResultVO(any(), any(), any())).thenReturn(mockPage);
        assertNotNull(service.findPageResultVO(params));

        // saveBatch entity
        BaseEmployee emp = new BaseEmployee();
        emp.setId(10L);
        when(empManager.saveBatch(List.of(emp))).thenReturn(true);
        assertTrue(service.saveBatch(List.of(emp)));

        // updateById & updateAllById
        when(empManager.updateById(emp)).thenReturn(true);
        assertTrue(service.updateById(emp));

        when(empManager.updateAllById(emp)).thenReturn(true);
        assertTrue(service.updateAllById(emp));

        // getEmployeeByUser & listEmployeeByUserId
        when(empManager.getEmployeeByUser(500L)).thenReturn(emp);
        assertEquals(emp, service.getEmployeeByUser(500L));

        when(empManager.listEmployeeByUserId(500L)).thenReturn(Collections.emptyList());
        assertTrue(service.listEmployeeByUserId(500L).isEmpty());
    }
}
