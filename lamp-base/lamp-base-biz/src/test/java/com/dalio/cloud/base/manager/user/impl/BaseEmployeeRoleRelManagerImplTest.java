package com.dalio.cloud.base.manager.user.impl;

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
import com.dalio.cloud.base.entity.system.BaseRole;
import com.dalio.cloud.base.entity.user.BaseEmployeeRoleRel;
import com.dalio.cloud.base.manager.system.BaseRoleManager;
import com.dalio.cloud.base.mapper.user.BaseEmployeeRoleRelMapper;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * BaseEmployeeRoleRelManagerImpl 单元测试
 */
class BaseEmployeeRoleRelManagerImplTest {

    @BeforeAll
    static void init() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, BaseEmployeeRoleRel.class);
    }

    @Test
    @DisplayName("测试 removeByEmployeeIds")
    void testRemoveByEmployeeIds() {
        BaseRoleManager roleManager = Mockito.mock(BaseRoleManager.class);
        CacheOps cacheOps = Mockito.mock(CacheOps.class);

        BaseEmployeeRoleRelManagerImpl manager = Mockito.spy(new BaseEmployeeRoleRelManagerImpl(roleManager, cacheOps));

        assertThrows(ArgumentException.class, () -> manager.removeByEmployeeIds(Collections.emptyList()));
        assertThrows(ArgumentException.class, () -> manager.removeByEmployeeIds(null));

        doReturn(true).when(manager).remove(any());

        assertTrue(manager.removeByEmployeeIds(List.of(100L, 200L)));
        verify(cacheOps).del(anyList());
    }

    @Test
    @DisplayName("测试 bindRole 和 unBindRole 绑定/解绑")
    void testBindAndUnBindRole() {
        BaseRoleManager roleManager = Mockito.mock(BaseRoleManager.class);
        CacheOps cacheOps = Mockito.mock(CacheOps.class);

        BaseEmployeeRoleRelManagerImpl manager = Mockito.spy(new BaseEmployeeRoleRelManagerImpl(roleManager, cacheOps));

        // 角色未配置时抛出异常
        when(roleManager.getRoleByCode("ADMIN")).thenReturn(null);
        assertThrows(ArgumentException.class, () -> manager.bindRole(List.of(100L), "ADMIN"));

        BaseRole role = new BaseRole();
        role.setId(5L);
        role.setCode("ADMIN");
        when(roleManager.getRoleByCode("ADMIN")).thenReturn(role);
        doReturn(true).when(manager).saveBatch(anyList());

        assertTrue(manager.bindRole(List.of(100L), "ADMIN"));
        verify(cacheOps).del(anyList());

        // unBindRole 测试
        assertThrows(ArgumentException.class, () -> manager.unBindRole(Collections.emptyList(), "ADMIN"));
        doReturn(true).when(manager).remove(any());
        assertTrue(manager.unBindRole(List.of(100L), "ADMIN"));
    }

    @Test
    @DisplayName("测试 deleteByRole 根据角色清空关联")
    void testDeleteByRole() {
        BaseRoleManager roleManager = Mockito.mock(BaseRoleManager.class);
        CacheOps cacheOps = Mockito.mock(CacheOps.class);

        BaseEmployeeRoleRelManagerImpl manager = Mockito.spy(new BaseEmployeeRoleRelManagerImpl(roleManager, cacheOps));

        // 空角色列表直接跳过
        manager.deleteByRole(Collections.emptyList());
        verify(manager, never()).list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));

        BaseEmployeeRoleRel rel1 = new BaseEmployeeRoleRel();
        rel1.setEmployeeId(100L);
        rel1.setRoleId(1L);

        doReturn(List.of(rel1)).when(manager).list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));
        doReturn(true).when(manager).remove(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));

        manager.deleteByRole(List.of(1L));
        verify(manager).remove(any());
        verify(cacheOps).del(anyList());
    }
}
