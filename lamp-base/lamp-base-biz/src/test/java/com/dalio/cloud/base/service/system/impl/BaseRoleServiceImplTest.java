package com.dalio.cloud.base.service.system.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import com.dalio.basic.cache.repository.CacheOps;
import com.dalio.basic.exception.BizException;
import com.dalio.cloud.base.entity.system.BaseRole;
import com.dalio.cloud.base.entity.system.BaseRoleResourceRel;
import com.dalio.cloud.base.entity.user.BaseEmployeeOrgRel;
import com.dalio.cloud.base.entity.user.BaseEmployeeRoleRel;
import com.dalio.cloud.base.manager.system.BaseRoleManager;
import com.dalio.cloud.base.manager.system.BaseRoleResourceRelManager;
import com.dalio.cloud.base.manager.user.BaseEmployeeRoleRelManager;
import com.dalio.cloud.base.manager.user.BaseOrgRoleRelManager;
import com.dalio.cloud.base.vo.save.system.BaseRoleResourceRelSaveVO;
import com.dalio.cloud.base.vo.save.system.RoleEmployeeSaveVO;
import com.dalio.cloud.model.enumeration.base.RoleCategoryEnum;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * BaseRoleServiceImpl 单元测试 (验证角色 CRUD、权限校验与缓存维护)
 */
class BaseRoleServiceImplTest {

    @BeforeAll
    static void init() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, BaseRole.class);
        TableInfoHelper.initTableInfo(assistant, BaseRoleResourceRel.class);
        TableInfoHelper.initTableInfo(assistant, BaseEmployeeRoleRel.class);
        TableInfoHelper.initTableInfo(assistant, BaseEmployeeOrgRel.class);
    }

    @Test
    @DisplayName("测试 check 角色编码唯一性校验")
    void testCheck() {
        BaseEmployeeRoleRelManager empRoleRelManager = Mockito.mock(BaseEmployeeRoleRelManager.class);
        BaseRoleResourceRelManager roleResRelManager = Mockito.mock(BaseRoleResourceRelManager.class);
        BaseOrgRoleRelManager orgRoleRelManager = Mockito.mock(BaseOrgRoleRelManager.class);
        BaseRoleManager roleManager = Mockito.mock(BaseRoleManager.class);

        BaseRoleServiceImpl service = new BaseRoleServiceImpl(empRoleRelManager, roleResRelManager, orgRoleRelManager);
        ReflectionTestUtils.setField(service, "superManager", roleManager);

        // 存在重复编码
        when(roleManager.count(any())).thenReturn(1L);
        assertTrue(service.check("ROLE_ADMIN", 1L));

        // 不存在重复编码
        when(roleManager.count(any())).thenReturn(0L);
        assertFalse(service.check("ROLE_USER", null));
    }

    @Test
    @DisplayName("测试 removeByIds 内置角色禁止删除防御")
    void testRemoveByIdsReadonlyDefense() {
        BaseEmployeeRoleRelManager empRoleRelManager = Mockito.mock(BaseEmployeeRoleRelManager.class);
        BaseRoleResourceRelManager roleResRelManager = Mockito.mock(BaseRoleResourceRelManager.class);
        BaseOrgRoleRelManager orgRoleRelManager = Mockito.mock(BaseOrgRoleRelManager.class);
        BaseRoleManager roleManager = Mockito.mock(BaseRoleManager.class);

        BaseRoleServiceImpl service = new BaseRoleServiceImpl(empRoleRelManager, roleResRelManager, orgRoleRelManager);
        ReflectionTestUtils.setField(service, "superManager", roleManager);

        // 1. 空列表直接返回 true
        assertTrue(service.removeByIds(Collections.emptyList()));

        // 2. 含有只读角色时抛出异常
        BaseRole readonlyRole = new BaseRole();
        readonlyRole.setId(1L);
        readonlyRole.setReadonly(true);
        when(roleManager.listByIds(List.of(1L))).thenReturn(List.of(readonlyRole));
        assertThrows(BizException.class, () -> service.removeByIds(List.of(1L)));

        // 3. 正常角色可以删除
        BaseRole normalRole = new BaseRole();
        normalRole.setId(2L);
        normalRole.setReadonly(false);
        when(roleManager.listByIds(List.of(2L))).thenReturn(List.of(normalRole));
        when(roleManager.removeByIds(List.of(2L))).thenReturn(true);
        assertTrue(service.removeByIds(List.of(2L)));
        verify(empRoleRelManager).deleteByRole(List.of(2L));
        verify(orgRoleRelManager).deleteByRole(List.of(2L));
        verify(roleResRelManager).deleteByRole(List.of(2L));
    }

    @Test
    @DisplayName("测试 saveRoleEmployee 员工-角色关联保存")
    void testSaveRoleEmployee() {
        BaseEmployeeRoleRelManager empRoleRelManager = Mockito.mock(BaseEmployeeRoleRelManager.class);
        BaseRoleResourceRelManager roleResRelManager = Mockito.mock(BaseRoleResourceRelManager.class);
        BaseOrgRoleRelManager orgRoleRelManager = Mockito.mock(BaseOrgRoleRelManager.class);
        BaseRoleManager roleManager = Mockito.mock(BaseRoleManager.class);
        CacheOps cacheOps = Mockito.mock(CacheOps.class);

        BaseRoleServiceImpl service = Mockito.spy(new BaseRoleServiceImpl(empRoleRelManager, roleResRelManager, orgRoleRelManager));
        ReflectionTestUtils.setField(service, "superManager", roleManager);
        ReflectionTestUtils.setField(service, "cacheOps", cacheOps);

        RoleEmployeeSaveVO saveVO = new RoleEmployeeSaveVO();
        saveVO.setRoleId(10L);
        saveVO.setEmployeeIdList(Arrays.asList(100L, 200L));
        saveVO.setFlag(true);

        doReturn(List.of(100L, 200L)).when(service).findEmployeeIdByRoleId(10L);

        List<Long> result = service.saveRoleEmployee(saveVO);
        assertEquals(List.of(100L, 200L), result);
        verify(empRoleRelManager).remove(any());
        verify(empRoleRelManager).saveBatch(anyList());
        verify(cacheOps).del(any(com.dalio.basic.model.cache.CacheKey[].class));
    }

    @Test
    @DisplayName("测试 saveRoleEmployee flag=false 时仅清空不新增")
    void testSaveRoleEmployeeRemoveOnly() {
        BaseEmployeeRoleRelManager empRoleRelManager = Mockito.mock(BaseEmployeeRoleRelManager.class);
        BaseRoleResourceRelManager roleResRelManager = Mockito.mock(BaseRoleResourceRelManager.class);
        BaseOrgRoleRelManager orgRoleRelManager = Mockito.mock(BaseOrgRoleRelManager.class);
        BaseRoleManager roleManager = Mockito.mock(BaseRoleManager.class);
        CacheOps cacheOps = Mockito.mock(CacheOps.class);

        BaseRoleServiceImpl service = Mockito.spy(new BaseRoleServiceImpl(empRoleRelManager, roleResRelManager, orgRoleRelManager));
        ReflectionTestUtils.setField(service, "superManager", roleManager);
        ReflectionTestUtils.setField(service, "cacheOps", cacheOps);

        RoleEmployeeSaveVO saveVO = new RoleEmployeeSaveVO();
        saveVO.setRoleId(10L);
        saveVO.setEmployeeIdList(Arrays.asList(100L, 200L));
        saveVO.setFlag(false);

        doReturn(Collections.emptyList()).when(service).findEmployeeIdByRoleId(10L);

        service.saveRoleEmployee(saveVO);
        verify(empRoleRelManager).remove(any());
        verify(empRoleRelManager, never()).saveBatch(anyList());
    }

    @Test
    @DisplayName("测试 saveRoleResource 角色资源保存与缓存淘汰")
    void testSaveRoleResource() {
        BaseEmployeeRoleRelManager empRoleRelManager = Mockito.mock(BaseEmployeeRoleRelManager.class);
        BaseRoleResourceRelManager roleResRelManager = Mockito.mock(BaseRoleResourceRelManager.class);
        BaseOrgRoleRelManager orgRoleRelManager = Mockito.mock(BaseOrgRoleRelManager.class);
        BaseRoleManager roleManager = Mockito.mock(BaseRoleManager.class);
        CacheOps cacheOps = Mockito.mock(CacheOps.class);

        BaseRoleServiceImpl service = new BaseRoleServiceImpl(empRoleRelManager, roleResRelManager, orgRoleRelManager);
        ReflectionTestUtils.setField(service, "superManager", roleManager);
        ReflectionTestUtils.setField(service, "cacheOps", cacheOps);

        // 1. 空资源映射
        BaseRoleResourceRelSaveVO emptyVO = new BaseRoleResourceRelSaveVO();
        emptyVO.setRoleId(5L);
        emptyVO.setApplicationResourceMap(Collections.emptyMap());
        assertFalse(service.saveRoleResource(emptyVO));

        // 2. 有资源映射
        BaseRoleResourceRelSaveVO saveVO = new BaseRoleResourceRelSaveVO();
        saveVO.setRoleId(5L);
        saveVO.setApplicationResourceMap(Map.of(1L, List.of(10L, 20L)));

        when(roleResRelManager.saveBatch(anyList())).thenReturn(true);
        assertTrue(service.saveRoleResource(saveVO));
        verify(roleResRelManager).remove(any());
        verify(roleResRelManager).saveBatch(anyList());
        verify(cacheOps).del(anyList());
    }

    @Test
    @DisplayName("测试 findResourceIdByRoleId 资源映射查询（含 null category 默认值）")
    void testFindResourceIdByRoleId() {
        BaseEmployeeRoleRelManager empRoleRelManager = Mockito.mock(BaseEmployeeRoleRelManager.class);
        BaseRoleResourceRelManager roleResRelManager = Mockito.mock(BaseRoleResourceRelManager.class);
        BaseOrgRoleRelManager orgRoleRelManager = Mockito.mock(BaseOrgRoleRelManager.class);
        BaseRoleManager roleManager = Mockito.mock(BaseRoleManager.class);

        BaseRoleServiceImpl service = new BaseRoleServiceImpl(empRoleRelManager, roleResRelManager, orgRoleRelManager);
        ReflectionTestUtils.setField(service, "superManager", roleManager);

        when(roleResRelManager.findByRoleIdAndCategory(eq(10L), any())).thenReturn(Collections.emptyList());

        // null category 自动使用 FUNCTION
        Map<Long, ?> result = service.findResourceIdByRoleId(10L, null);
        assertNotNull(result);
        verify(roleResRelManager).findByRoleIdAndCategory(eq(10L), eq(RoleCategoryEnum.FUNCTION.getCode()));

        // 指定 category
        service.findResourceIdByRoleId(10L, RoleCategoryEnum.DATA);
        verify(roleResRelManager).findByRoleIdAndCategory(eq(10L), eq(RoleCategoryEnum.DATA.getCode()));
    }

    @Test
    @DisplayName("测试 checkRole 和 findRoleCodeByEmployeeId 委托")
    void testCheckAndFindRoleCodes() {
        BaseEmployeeRoleRelManager empRoleRelManager = Mockito.mock(BaseEmployeeRoleRelManager.class);
        BaseRoleResourceRelManager roleResRelManager = Mockito.mock(BaseRoleResourceRelManager.class);
        BaseOrgRoleRelManager orgRoleRelManager = Mockito.mock(BaseOrgRoleRelManager.class);
        BaseRoleManager roleManager = Mockito.mock(BaseRoleManager.class);

        BaseRoleServiceImpl service = new BaseRoleServiceImpl(empRoleRelManager, roleResRelManager, orgRoleRelManager);
        ReflectionTestUtils.setField(service, "superManager", roleManager);

        when(roleManager.checkRole(100L, "ROLE_ADMIN")).thenReturn(true);
        when(roleManager.checkRole(200L, "ROLE_ADMIN")).thenReturn(false);

        assertTrue(service.checkRole(100L, "ROLE_ADMIN"));
        assertFalse(service.checkRole(200L, "ROLE_ADMIN"));

        BaseRole role1 = new BaseRole();
        role1.setCode("ROLE_ADMIN");
        BaseRole role2 = new BaseRole();
        role2.setCode("ROLE_USER");
        when(roleManager.findRoleByEmployeeId(100L)).thenReturn(List.of(role1, role2));

        List<String> codes = service.findRoleCodeByEmployeeId(100L);
        assertEquals(2, codes.size());
        assertTrue(codes.contains("ROLE_ADMIN"));
        assertTrue(codes.contains("ROLE_USER"));
    }
}
