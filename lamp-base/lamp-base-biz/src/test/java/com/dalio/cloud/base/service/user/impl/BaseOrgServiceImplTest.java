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
import com.dalio.cloud.base.entity.user.BaseEmployeeOrgRel;
import com.dalio.cloud.base.entity.user.BaseOrg;
import com.dalio.cloud.base.entity.user.BaseOrgRoleRel;
import com.dalio.cloud.base.manager.user.BaseEmployeeOrgRelManager;
import com.dalio.cloud.base.manager.user.BaseOrgManager;
import com.dalio.cloud.base.manager.user.BaseOrgRoleRelManager;
import com.dalio.cloud.base.vo.save.user.BaseOrgRoleRelSaveVO;
import com.dalio.cloud.base.vo.save.user.BaseOrgSaveVO;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 组织服务实现类单元测试 (验证树形填充、删除保护防御、角色关联)
 */
class BaseOrgServiceImplTest {

    @BeforeAll
    static void init() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, BaseOrg.class);
        TableInfoHelper.initTableInfo(assistant, BaseEmployeeOrgRel.class);
        TableInfoHelper.initTableInfo(assistant, BaseOrgRoleRel.class);
    }

    @Test
    @DisplayName("测试 check 组织名称唯一性校验")
    void testCheck() {
        BaseEmployeeOrgRelManager employeeOrgRelManager = Mockito.mock(BaseEmployeeOrgRelManager.class);
        BaseOrgRoleRelManager orgRoleRelManager = Mockito.mock(BaseOrgRoleRelManager.class);
        BaseOrgManager orgManager = Mockito.mock(BaseOrgManager.class);

        BaseOrgServiceImpl service = new BaseOrgServiceImpl(employeeOrgRelManager, orgRoleRelManager);
        ReflectionTestUtils.setField(service, "superManager", orgManager);

        // 1. 空名称抛出异常
        assertThrows(ArgumentException.class, () -> service.check("", 0L, null));

        // 2. 存在重名
        when(orgManager.count(any())).thenReturn(1L);
        assertTrue(service.check("研发部", 0L, null));

        // 3. 不存在重名
        when(orgManager.count(any())).thenReturn(0L);
        assertFalse(service.check("市场部", 0L, null));
    }

    @Test
    @DisplayName("测试 saveBefore 树形层级填充与重名校验")
    void testSaveBefore() {
        BaseEmployeeOrgRelManager employeeOrgRelManager = Mockito.mock(BaseEmployeeOrgRelManager.class);
        BaseOrgRoleRelManager orgRoleRelManager = Mockito.mock(BaseOrgRoleRelManager.class);
        BaseOrgManager orgManager = Mockito.mock(BaseOrgManager.class);

        BaseOrgServiceImpl service = Mockito.spy(new BaseOrgServiceImpl(employeeOrgRelManager, orgRoleRelManager));
        ReflectionTestUtils.setField(service, "superManager", orgManager);

        // 1. 重名抛出异常
        doReturn(true).when(service).check("总部", 0L, null);
        BaseOrgSaveVO dupVO = new BaseOrgSaveVO();
        dupVO.setName("总部");
        dupVO.setParentId(0L);
        assertThrows(ArgumentException.class, () -> service.saveBefore(dupVO));

        // 2. 根节点创建
        doReturn(false).when(service).check("总部", 0L, null);
        BaseOrg rootOrg = service.saveBefore(dupVO);
        assertNotNull(rootOrg);
        assertEquals(0L, rootOrg.getParentId());
        assertEquals(0, rootOrg.getTreeGrade()); // DefValConstants.TREE_GRADE = 0

        // 3. 子节点创建
        BaseOrg parentOrg = new BaseOrg();
        parentOrg.setId(10L);
        parentOrg.setTreeGrade(1);
        parentOrg.setTreePath("/0/");
        when(orgManager.getByIdCache(10L)).thenReturn(parentOrg);

        BaseOrgSaveVO childVO = new BaseOrgSaveVO();
        childVO.setName("研发部");
        childVO.setParentId(10L);
        doReturn(false).when(service).check("研发部", 10L, null);

        BaseOrg childOrg = service.saveBefore(childVO);
        assertNotNull(childOrg);
        assertEquals(10L, childOrg.getParentId());
        assertEquals(2, childOrg.getTreeGrade()); // parent.treeGrade(1) + 1
        assertTrue(childOrg.getTreePath().contains("10"));
    }

    @Test
    @DisplayName("测试 removeByIds 安全防御：存在用户或子组织时禁止删除")
    void testRemoveByIdsDefense() {
        BaseEmployeeOrgRelManager employeeOrgRelManager = Mockito.mock(BaseEmployeeOrgRelManager.class);
        BaseOrgRoleRelManager orgRoleRelManager = Mockito.mock(BaseOrgRoleRelManager.class);
        BaseOrgManager orgManager = Mockito.mock(BaseOrgManager.class);
        CacheOps cacheOps = Mockito.mock(CacheOps.class);

        BaseOrgServiceImpl service = new BaseOrgServiceImpl(employeeOrgRelManager, orgRoleRelManager);
        ReflectionTestUtils.setField(service, "superManager", orgManager);
        ReflectionTestUtils.setField(service, "cacheOps", cacheOps);

        // 1. 空列表直接返回 false
        assertFalse(service.removeByIds(Collections.emptyList()));

        // 2. 存在员工关联，拒绝删除
        when(employeeOrgRelManager.count(any())).thenReturn(3L);
        assertThrows(ArgumentException.class, () -> service.removeByIds(List.of(100L)));

        // 3. 无员工但存在子组织，拒绝删除
        when(employeeOrgRelManager.count(any())).thenReturn(0L);
        when(orgManager.count(any())).thenReturn(2L);
        assertThrows(ArgumentException.class, () -> service.removeByIds(List.of(100L)));

        // 4. 正常删除，清理关系与缓存
        when(orgManager.count(any())).thenReturn(0L);
        when(orgManager.removeByIds(List.of(100L))).thenReturn(true);

        boolean removed = service.removeByIds(List.of(100L));
        assertTrue(removed);
        verify(orgRoleRelManager).deleteByOrg(List.of(100L));
        verify(employeeOrgRelManager).deleteByOrg(List.of(100L));
        verify(cacheOps).del(anyList());
    }

    @Test
    @DisplayName("测试 saveOrgRole 保存组织角色并淘汰缓存")
    void testSaveOrgRole() {
        BaseEmployeeOrgRelManager employeeOrgRelManager = Mockito.mock(BaseEmployeeOrgRelManager.class);
        BaseOrgRoleRelManager orgRoleRelManager = Mockito.mock(BaseOrgRoleRelManager.class);
        BaseOrgManager orgManager = Mockito.mock(BaseOrgManager.class);
        CacheOps cacheOps = Mockito.mock(CacheOps.class);

        BaseOrgServiceImpl service = Mockito.spy(new BaseOrgServiceImpl(employeeOrgRelManager, orgRoleRelManager));
        ReflectionTestUtils.setField(service, "superManager", orgManager);
        ReflectionTestUtils.setField(service, "cacheOps", cacheOps);

        BaseOrgRoleRelSaveVO saveVO = new BaseOrgRoleRelSaveVO();
        saveVO.setOrgId(50L);
        saveVO.setRoleIdList(List.of(1L, 2L));
        saveVO.setFlag(true);

        doReturn(List.of(1L, 2L)).when(service).findOrgRoleByOrgId(50L);

        List<Long> result = service.saveOrgRole(saveVO);
        assertEquals(List.of(1L, 2L), result);
        verify(orgRoleRelManager).remove(any());
        verify(orgRoleRelManager).saveBatch(anyList());
        verify(cacheOps).del(any(com.dalio.basic.model.cache.CacheKey.class));
    }

    @Test
    @DisplayName("测试 updateBefore 树形层级与重名更新")
    void testUpdateBefore() {
        BaseEmployeeOrgRelManager employeeOrgRelManager = Mockito.mock(BaseEmployeeOrgRelManager.class);
        BaseOrgRoleRelManager orgRoleRelManager = Mockito.mock(BaseOrgRoleRelManager.class);
        BaseOrgManager orgManager = Mockito.mock(BaseOrgManager.class);

        BaseOrgServiceImpl service = Mockito.spy(new BaseOrgServiceImpl(employeeOrgRelManager, orgRoleRelManager));
        ReflectionTestUtils.setField(service, "superManager", orgManager);

        com.dalio.cloud.base.vo.update.user.BaseOrgUpdateVO updateVO = new com.dalio.cloud.base.vo.update.user.BaseOrgUpdateVO();
        updateVO.setId(10L);
        updateVO.setName("研发二部");
        updateVO.setParentId(1L);

        doReturn(false).when(service).check("研发二部", 1L, 10L);
        BaseOrg parentOrg = new BaseOrg();
        parentOrg.setId(1L);
        parentOrg.setTreeGrade(1);
        parentOrg.setTreePath("/0/");
        when(orgManager.getByIdCache(1L)).thenReturn(parentOrg);

        BaseOrg updated = service.updateBefore(updateVO);
        assertNotNull(updated);
        assertEquals(1L, updated.getParentId());
        assertEquals(2, updated.getTreeGrade());
    }

    @Test
    @DisplayName("测试 list 查询与 findByIds")
    void testListAndFindByIds() {
        BaseEmployeeOrgRelManager employeeOrgRelManager = Mockito.mock(BaseEmployeeOrgRelManager.class);
        BaseOrgRoleRelManager orgRoleRelManager = Mockito.mock(BaseOrgRoleRelManager.class);
        BaseOrgManager orgManager = Mockito.mock(BaseOrgManager.class);

        BaseOrgServiceImpl service = new BaseOrgServiceImpl(employeeOrgRelManager, orgRoleRelManager);
        ReflectionTestUtils.setField(service, "superManager", orgManager);

        com.dalio.cloud.base.vo.query.user.BaseOrgPageQuery query = new com.dalio.cloud.base.vo.query.user.BaseOrgPageQuery();
        query.setName("测试");
        query.setState(true);

        BaseOrg org = new BaseOrg();
        org.setId(1L);
        org.setName("测试部");
        when(orgManager.list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class))).thenReturn(List.of(org));

        List<?> listResult = service.list(query);
        assertEquals(1, listResult.size());

        when(orgManager.findByIds(any())).thenReturn(java.util.Map.of(1L, org));
        java.util.Map<?, ?> map = service.findByIds(java.util.Set.of(1L));
        assertEquals(1, map.size());
    }

    @Test
    @DisplayName("测试 findDeptByEmployeeId, findOrgByEmployeeId, getDefaultOrg")
    void testFindDeptAndDefaultOrg() {
        BaseEmployeeOrgRelManager employeeOrgRelManager = Mockito.mock(BaseEmployeeOrgRelManager.class);
        BaseOrgRoleRelManager orgRoleRelManager = Mockito.mock(BaseOrgRoleRelManager.class);
        BaseOrgManager orgManager = Mockito.mock(BaseOrgManager.class);

        BaseOrgServiceImpl service = Mockito.spy(new BaseOrgServiceImpl(employeeOrgRelManager, orgRoleRelManager));
        ReflectionTestUtils.setField(service, "superManager", orgManager);

        when(employeeOrgRelManager.findOrgIdByEmployeeId(100L)).thenReturn(List.of(10L, 20L));

        BaseOrg dept = new BaseOrg();
        dept.setId(10L);
        dept.setType(com.dalio.cloud.model.enumeration.base.OrgTypeEnum.DEPT.getCode());
        dept.setTreePath("/1/10/");

        BaseOrg company = new BaseOrg();
        company.setId(20L);
        company.setType(com.dalio.cloud.model.enumeration.base.OrgTypeEnum.COMPANY.getCode());
        company.setTreePath("/20/");

        doReturn(List.of(dept, company)).when(service).findByIds(anyList(), any());

        // findOrgByEmployeeId
        List<BaseOrg> orgs = service.findOrgByEmployeeId(100L);
        assertEquals(2, orgs.size());

        // findDeptByEmployeeId
        List<BaseOrg> depts = service.findDeptByEmployeeId(100L, 1L);
        assertEquals(1, depts.size());
        assertEquals(10L, depts.get(0).getId());

        // getDefaultOrg
        assertNull(service.getDefaultOrg(Collections.emptyList(), null));
        assertEquals(dept, service.getDefaultOrg(List.of(dept, company), 10L));
        assertEquals(dept, service.getDefaultOrg(List.of(dept, company), 999L));
    }

    @Test
    @DisplayName("测试 findCompanyByEmployeeId 与 getCompanyByDeptId")
    void testFindCompanyAndGetCompanyByDeptId() {
        BaseEmployeeOrgRelManager employeeOrgRelManager = Mockito.mock(BaseEmployeeOrgRelManager.class);
        BaseOrgRoleRelManager orgRoleRelManager = Mockito.mock(BaseOrgRoleRelManager.class);
        BaseOrgManager orgManager = Mockito.mock(BaseOrgManager.class);

        BaseOrgServiceImpl service = Mockito.spy(new BaseOrgServiceImpl(employeeOrgRelManager, orgRoleRelManager));
        ReflectionTestUtils.setField(service, "superManager", orgManager);

        when(employeeOrgRelManager.findOrgIdByEmployeeId(100L)).thenReturn(List.of(10L));

        BaseOrg dept = new BaseOrg();
        dept.setId(10L);
        dept.setParentId(1L);
        dept.setType(com.dalio.cloud.model.enumeration.base.OrgTypeEnum.DEPT.getCode());
        dept.setTreePath("/1/10/");

        BaseOrg parentComp = new BaseOrg();
        parentComp.setId(1L);
        parentComp.setType(com.dalio.cloud.model.enumeration.base.OrgTypeEnum.COMPANY.getCode());
        parentComp.setTreePath("/1/");

        doReturn(List.of(dept)).when(service).findByIds(List.of(10L), null);
        when(orgManager.findByIds(anyList(), any())).thenReturn(List.of(parentComp));

        List<BaseOrg> companies = service.findCompanyByEmployeeId(100L);
        assertEquals(1, companies.size());
        assertEquals(1L, companies.get(0).getId());

        // getCompanyByDeptId
        when(orgManager.getByIdCache(10L)).thenReturn(dept);
        when(orgManager.getByIdCache(1L)).thenReturn(parentComp);

        BaseOrg foundComp = service.getCompanyByDeptId(10L);
        assertNotNull(foundComp);
        assertEquals(1L, foundComp.getId());

        // 递归上溯未找到单位时返回 null
        when(orgManager.getByIdCache(99L)).thenReturn(null);
        assertNull(service.getCompanyByDeptId(99L));
    }
}
