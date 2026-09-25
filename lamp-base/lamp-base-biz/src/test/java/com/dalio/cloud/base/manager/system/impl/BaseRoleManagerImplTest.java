package com.dalio.cloud.base.manager.system.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.dalio.basic.cache.redis2.CacheResult;
import com.dalio.basic.cache.repository.CacheOps;
import com.dalio.basic.exception.ArgumentException;
import com.dalio.cloud.base.entity.system.BaseRole;
import com.dalio.cloud.base.manager.user.BaseEmployeeOrgRelManager;
import com.dalio.cloud.base.mapper.system.BaseRoleMapper;
import com.dalio.cloud.base.mapper.system.BaseRoleResourceRelMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * BaseRoleManagerImpl 单元测试
 */
class BaseRoleManagerImplTest {

    @BeforeAll
    static void init() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, BaseRole.class);
    }

    @Test
    @DisplayName("测试 cacheKeyBuilder 与 getRoleByCode")
    void testCacheKeyBuilderAndGetRoleByCode() {
        BaseRoleResourceRelMapper roleResRelMapper = Mockito.mock(BaseRoleResourceRelMapper.class);
        BaseEmployeeOrgRelManager empOrgRelManager = Mockito.mock(BaseEmployeeOrgRelManager.class);
        BaseRoleMapper roleMapper = Mockito.mock(BaseRoleMapper.class);

        BaseRoleManagerImpl manager = Mockito.spy(new BaseRoleManagerImpl(roleResRelMapper, empOrgRelManager));
        ReflectionTestUtils.setField(manager, "baseMapper", roleMapper);

        assertNotNull(manager.cacheKeyBuilder());

        // code 为空抛出异常
        assertThrows(ArgumentException.class, () -> manager.getRoleByCode(""));
        assertThrows(ArgumentException.class, () -> manager.getRoleByCode(null));

        BaseRole role = new BaseRole();
        role.setId(10L);
        role.setCode("ADMIN");
        doReturn(role).when(manager).getOne(any());

        BaseRole found = manager.getRoleByCode("ADMIN");
        assertEquals(10L, found.getId());
    }

    @Test
    @DisplayName("测试 listEmployeeIdByRoleId 和 checkRole")
    void testListEmployeeIdAndCheckRole() {
        BaseRoleResourceRelMapper roleResRelMapper = Mockito.mock(BaseRoleResourceRelMapper.class);
        BaseEmployeeOrgRelManager empOrgRelManager = Mockito.mock(BaseEmployeeOrgRelManager.class);
        BaseRoleMapper roleMapper = Mockito.mock(BaseRoleMapper.class);

        BaseRoleManagerImpl manager = new BaseRoleManagerImpl(roleResRelMapper, empOrgRelManager);
        ReflectionTestUtils.setField(manager, "baseMapper", roleMapper);

        when(roleMapper.listEmployeeIdByRoleId(List.of(1L, 2L))).thenReturn(List.of(100L, 200L));
        assertEquals(List.of(100L, 200L), manager.listEmployeeIdByRoleId(List.of(1L, 2L)));

        when(roleMapper.selectRoleByEmployee(eq(100L), any())).thenReturn(List.of(new BaseRole()));
        assertTrue(manager.checkRole(100L, "ROLE_ADMIN"));

        when(roleMapper.selectRoleByEmployee(eq(200L), any())).thenReturn(Collections.emptyList());
        assertFalse(manager.checkRole(200L, "ROLE_ADMIN"));
    }

    @Test
    @DisplayName("测试 findRoleIdByEmployeeId 与 findRoleByEmployeeId")
    void testFindRoleIdAndRoleByEmployeeId() {
        BaseRoleResourceRelMapper roleResRelMapper = Mockito.mock(BaseRoleResourceRelMapper.class);
        BaseEmployeeOrgRelManager empOrgRelManager = Mockito.mock(BaseEmployeeOrgRelManager.class);
        BaseRoleMapper roleMapper = Mockito.mock(BaseRoleMapper.class);
        CacheOps cacheOps = Mockito.mock(CacheOps.class);

        BaseRoleManagerImpl manager = Mockito.spy(new BaseRoleManagerImpl(roleResRelMapper, empOrgRelManager));
        ReflectionTestUtils.setField(manager, "baseMapper", roleMapper);
        ReflectionTestUtils.setField(manager, "cacheOps", cacheOps);

        when(cacheOps.get(any(), any(), any(boolean[].class)))
                .thenReturn(new CacheResult<>("k", List.of(1L, 2L)));
        when(empOrgRelManager.findOrgIdByEmployeeId(100L)).thenReturn(List.of(10L));
        doReturn(Set.of(2L, 3L)).when(manager).findCollectByIds(any(), any(), any());

        List<Long> roleIds = manager.findRoleIdByEmployeeId(100L);
        assertNotNull(roleIds);
        assertTrue(roleIds.contains(1L));
        assertTrue(roleIds.contains(2L));
        assertTrue(roleIds.contains(3L));

        BaseRole r1 = new BaseRole();
        r1.setId(1L);
        r1.setState(true);
        BaseRole r2 = new BaseRole();
        r2.setId(2L);
        r2.setState(false); // 禁用状态
        doReturn(List.of(r1, r2)).when(manager).findByIds(any(), any());

        List<BaseRole> activeRoles = manager.findRoleByEmployeeId(100L);
        assertEquals(1, activeRoles.size());
        assertEquals(1L, activeRoles.get(0).getId());
    }

    @Test
    @DisplayName("测试 findResourceIdByEmployeeId 资源集合获取")
    void testFindResourceIdByEmployeeId() {
        BaseRoleResourceRelMapper roleResRelMapper = Mockito.mock(BaseRoleResourceRelMapper.class);
        BaseEmployeeOrgRelManager empOrgRelManager = Mockito.mock(BaseEmployeeOrgRelManager.class);
        BaseRoleMapper roleMapper = Mockito.mock(BaseRoleMapper.class);

        BaseRoleManagerImpl manager = Mockito.spy(new BaseRoleManagerImpl(roleResRelMapper, empOrgRelManager));
        ReflectionTestUtils.setField(manager, "baseMapper", roleMapper);

        // 1. 角色列表为空
        doReturn(Collections.emptyList()).when(manager).findRoleByEmployeeId(100L);
        List<Long> res1 = manager.findResourceIdByEmployeeId(1L, 100L);
        assertTrue(res1.isEmpty());

        // 2. 角色列表非空
        BaseRole role = new BaseRole();
        role.setId(5L);
        role.setState(true);
        doReturn(List.of(role)).when(manager).findRoleByEmployeeId(100L);
        doReturn(Set.of(1001L, 1002L)).when(manager).findCollectByIds(any(), any(), any());

        List<Long> res2 = manager.findResourceIdByEmployeeId(1L, 100L);
        assertEquals(2, res2.size());
        assertTrue(res2.contains(1001L));
        assertTrue(res2.contains(1002L));
    }
}
