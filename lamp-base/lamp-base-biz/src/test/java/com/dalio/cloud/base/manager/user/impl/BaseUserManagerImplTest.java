package com.dalio.cloud.base.manager.user.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import com.dalio.cloud.base.entity.user.BaseOrg;
import com.dalio.cloud.base.entity.user.BaseOrgRoleRel;
import com.dalio.cloud.base.entity.user.BasePosition;
import com.dalio.cloud.base.manager.user.BaseEmployeeOrgRelManager;
import com.dalio.cloud.base.mapper.user.BaseEmployeeMapper;
import com.dalio.cloud.base.mapper.user.BaseOrgMapper;
import com.dalio.cloud.base.mapper.user.BasePositionMapper;
import com.dalio.cloud.base.service.user.impl.BaseEmployeeOrgRelServiceImpl;
import com.dalio.cloud.base.vo.query.user.BaseEmployeePageQuery;
import com.dalio.cloud.base.vo.result.user.BaseEmployeeResultVO;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * BaseUserManagerImpl 与 BaseEmployeeOrgRelServiceImpl 单元测试
 */
class BaseUserManagerImplTest {

    @BeforeAll
    static void init() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, BaseOrg.class);
        TableInfoHelper.initTableInfo(assistant, BaseOrgRoleRel.class);
        TableInfoHelper.initTableInfo(assistant, BaseEmployee.class);
        TableInfoHelper.initTableInfo(assistant, BasePosition.class);
        TableInfoHelper.initTableInfo(assistant, BaseEmployeeOrgRel.class);
    }

    @Test
    @DisplayName("测试 BaseOrgManagerImpl findByIds 与 cacheKeyBuilder")
    void testBaseOrgManagerImpl() {
        BaseOrgManagerImpl manager = Mockito.spy(new BaseOrgManagerImpl());
        assertNotNull(manager.cacheKeyBuilder());

        // 空入参
        Map<Serializable, Object> emptyMap = manager.findByIds(Collections.emptySet());
        assertTrue(emptyMap.isEmpty());

        // 包含集合参数与单一参数
        BaseOrg org1 = new BaseOrg();
        org1.setId(1L);
        org1.setName("研发部");

        BaseOrg org2 = new BaseOrg();
        org2.setId(2L);
        org2.setName("市场部");

        doReturn(List.of(org1, org2)).when(manager).findByIds(anySet(), any());

        Set<Serializable> params = new java.util.HashSet<>();
        params.add(1L);
        params.add((Serializable) List.of(2L));
        Map<Serializable, Object> resMap = manager.findByIds(params);
        assertEquals(2, resMap.size());
        assertEquals("研发部", resMap.get(1L));
        assertEquals("市场部", resMap.get(2L));
    }

    @Test
    @DisplayName("测试 BaseOrgRoleRelManagerImpl deleteByOrg 与 deleteByRole")
    void testBaseOrgRoleRelManagerImpl() {
        CacheOps cacheOps = mock(CacheOps.class);
        BaseOrgRoleRelManagerImpl manager = Mockito.spy(new BaseOrgRoleRelManagerImpl(cacheOps));

        // deleteByOrg 空校验
        manager.deleteByOrg(null);
        manager.deleteByOrg(Collections.emptyList());
        verify(cacheOps, never()).del(anyList());

        // deleteByOrg 正常逻辑
        doReturn(true).when(manager).remove(any(Wrapper.class));
        manager.deleteByOrg(List.of(10L, 20L));
        verify(manager).remove(any(Wrapper.class));
        verify(cacheOps).del(anyList());

        // deleteByRole 空校验
        manager.deleteByRole(null);
        manager.deleteByRole(Collections.emptyList());

        // deleteByRole 正常逻辑
        BaseOrgRoleRel rel = new BaseOrgRoleRel();
        rel.setOrgId(100L);
        rel.setRoleId(1L);
        doReturn(List.of(rel)).when(manager).list(any(Wrapper.class));
        manager.deleteByRole(List.of(1L));
        verify(cacheOps, times(2)).del(anyList());
    }

    @Test
    @DisplayName("测试 BaseEmployeeManagerImpl 各种查询与 cacheKeyBuilder")
    void testBaseEmployeeManagerImpl() {
        BaseEmployeeMapper mapper = mock(BaseEmployeeMapper.class);
        BaseEmployeeManagerImpl manager = new BaseEmployeeManagerImpl();
        ReflectionTestUtils.setField(manager, "baseMapper", mapper);

        assertNotNull(manager.cacheKeyBuilder());

        // listEmployeeByUserId
        when(mapper.listEmployeeByUserId(100L)).thenReturn(List.of(new BaseEmployeeResultVO()));
        assertEquals(1, manager.listEmployeeByUserId(100L).size());

        // selectPageResultVO
        IPage<BaseEmployee> page = new Page<>(1, 10);
        BaseEmployeePageQuery query = new BaseEmployeePageQuery();
        when(mapper.selectPageResultVO(any(), any(), any())).thenReturn(new Page<>());
        assertNotNull(manager.selectPageResultVO(page, null, query));

        // getEmployeeByUser 空参数
        assertThrows(ArgumentException.class, () -> manager.getEmployeeByUser(null));

        // getEmployeeByUser 正常
        BaseEmployee emp = new BaseEmployee();
        emp.setId(10L);
        emp.setUserId(100L);
        when(mapper.selectOne(any(Wrapper.class))).thenReturn(emp);
        assertEquals(emp, manager.getEmployeeByUser(100L));
    }

    @Test
    @DisplayName("测试 BasePositionManagerImpl findByIds 与 cacheKeyBuilder")
    void testBasePositionManagerImpl() {
        BasePositionManagerImpl manager = Mockito.spy(new BasePositionManagerImpl());
        assertNotNull(manager.cacheKeyBuilder());

        BasePosition pos = new BasePosition();
        pos.setId(1L);
        pos.setName("架构师");
        doReturn(List.of(pos)).when(manager).findByIds(anySet(), any());

        Map<Serializable, Object> map = manager.findByIds(Set.of(1L));
        assertEquals(1, map.size());
        assertEquals("架构师", map.get(1L));
    }

    @Test
    @DisplayName("测试 BaseEmployeeOrgRelServiceImpl findOrgIdListByEmployeeId")
    void testBaseEmployeeOrgRelServiceImpl() {
        BaseEmployeeOrgRelManager relManager = mock(BaseEmployeeOrgRelManager.class);
        BaseEmployeeOrgRelServiceImpl service = new BaseEmployeeOrgRelServiceImpl();
        ReflectionTestUtils.setField(service, "superManager", relManager);

        assertThrows(ArgumentException.class, () -> service.findOrgIdListByEmployeeId(null));

        when(relManager.listObjs(any(Wrapper.class), any())).thenReturn(List.of(1L, 2L));
        List<Long> result = service.findOrgIdListByEmployeeId(10L);
        assertEquals(2, result.size());
    }
}
