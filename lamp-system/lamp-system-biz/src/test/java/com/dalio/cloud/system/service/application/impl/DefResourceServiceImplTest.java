package com.dalio.cloud.system.service.application.impl;

import com.dalio.basic.cache.repository.CacheOps;
import com.dalio.basic.exception.ArgumentException;
import com.dalio.basic.exception.BizException;
import com.dalio.basic.model.cache.CacheKey;
import com.dalio.cloud.common.constant.DefValConstants;
import com.dalio.cloud.model.enumeration.system.ResourceTypeEnum;
import com.dalio.cloud.model.vo.result.ResourceApiVO;
import com.dalio.cloud.system.entity.application.DefResource;
import com.dalio.cloud.system.entity.application.DefResourceApi;
import com.dalio.cloud.system.manager.application.DefResourceApiManager;
import com.dalio.cloud.system.manager.application.DefResourceManager;
import com.dalio.cloud.system.vo.result.application.DefResourceResultVO;
import com.dalio.cloud.system.vo.save.application.DefResourceApiSaveVO;
import com.dalio.cloud.system.vo.save.application.DefResourceSaveVO;
import com.dalio.cloud.system.vo.update.application.DefResourceUpdateVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class DefResourceServiceImplTest {

    @Test
    @DisplayName("测试 findAllApi 与 findResource 查询映射")
    void testFindAllApiAndFindResource() {
        DefResourceApiManager apiManager = mock(DefResourceApiManager.class);
        DefResourceManager resourceManager = mock(DefResourceManager.class);

        DefResourceServiceImpl service = new DefResourceServiceImpl(apiManager);
        ReflectionTestUtils.setField(service, "superManager", resourceManager);

        ResourceApiVO apiVO = new ResourceApiVO();
        apiVO.setCode("sys:user:add");
        when(apiManager.findAllApi()).thenReturn(List.of(apiVO));

        List<ResourceApiVO> apis = service.findAllApi();
        assertEquals(1, apis.size());
        assertEquals("sys:user:add", apis.get(0).getCode());

        when(resourceManager.findResourceListByApplicationId(any(), any())).thenReturn(Collections.emptyList());
        List<DefResource> resList = service.findResourceListByApplicationId(List.of(1L), List.of("10"));
        assertNotNull(resList);

        when(resourceManager.findByIdsAndType(any(), any())).thenReturn(Collections.emptyList());
        List<DefResource> typesRes = service.findByIdsAndType(List.of(1L), List.of("10"));
        assertNotNull(typesRes);
    }

    @Test
    @DisplayName("测试 check, checkPath, checkName 重名冲突校验")
    void testChecks() {
        DefResourceApiManager apiManager = mock(DefResourceApiManager.class);
        DefResourceManager resourceManager = mock(DefResourceManager.class);

        DefResourceServiceImpl service = new DefResourceServiceImpl(apiManager);
        ReflectionTestUtils.setField(service, "superManager", resourceManager);

        when(resourceManager.count(any())).thenReturn(1L).thenReturn(0L);

        assertTrue(service.check(1L, "CODE_EXIST"));
        assertFalse(service.checkPath(1L, 10L, "/path"));
        assertFalse(service.checkName(1L, 10L, "用户管理"));
    }

    @Test
    @DisplayName("测试 getResourceById 关联接口组装")
    void testGetResourceById() {
        DefResourceApiManager apiManager = mock(DefResourceApiManager.class);
        DefResourceManager resourceManager = mock(DefResourceManager.class);

        DefResourceServiceImpl service = new DefResourceServiceImpl(apiManager);
        ReflectionTestUtils.setField(service, "superManager", resourceManager);

        // 1. 不存在
        when(resourceManager.getById(999L)).thenReturn(null);
        assertNull(service.getResourceById(999L));

        // 2. 存在
        DefResource res = new DefResource();
        res.setId(100L);
        res.setName("菜单1");
        when(resourceManager.getById(100L)).thenReturn(res);

        DefResourceApi resApi = new DefResourceApi();
        resApi.setResourceId(100L);
        resApi.setName("添加");
        when(apiManager.findByResourceId(100L)).thenReturn(List.of(resApi));

        DefResourceResultVO result = service.getResourceById(100L);
        assertNotNull(result);
        assertEquals("菜单1", result.getName());
        assertEquals(1, result.getResourceApiList().size());
    }

    @Test
    @DisplayName("测试 removeByIdWithCache 级联删除与缓存清理")
    void testRemoveByIdWithCache() {
        DefResourceApiManager apiManager = mock(DefResourceApiManager.class);
        DefResourceManager resourceManager = mock(DefResourceManager.class);
        CacheOps cacheOps = mock(CacheOps.class);

        DefResourceServiceImpl service = new DefResourceServiceImpl(apiManager);
        ReflectionTestUtils.setField(service, "superManager", resourceManager);
        ReflectionTestUtils.setField(service, "cacheOps", cacheOps);

        // 1. 空列表
        assertFalse(service.removeByIdWithCache(Collections.emptyList()));

        // 2. 存在子资源，抛出异常
        when(resourceManager.count(any())).thenReturn(2L);
        assertThrows(RuntimeException.class, () -> service.removeByIdWithCache(List.of(10L)));

        // 3. 正常删除无子资源
        when(resourceManager.count(any())).thenReturn(0L);
        DefResource res = new DefResource();
        res.setId(10L);
        res.setApplicationId(1001L);
        when(resourceManager.listByIds(anyList())).thenReturn(List.of(res));
        when(resourceManager.removeByIds(anyList())).thenReturn(true);

        boolean ok = service.removeByIdWithCache(List.of(10L));
        assertTrue(ok);
        verify(apiManager).removeByResourceId(List.of(10L));
        verify(cacheOps, atLeastOnce()).del(any(CacheKey.class));
    }

    @Test
    @DisplayName("测试 deleteRoleResourceRelByResourceId 空列表防护")
    void testDeleteRoleResourceRelByResourceIdProtection() {
        DefResourceApiManager apiManager = mock(DefResourceApiManager.class);
        DefResourceManager resourceManager = mock(DefResourceManager.class);

        DefResourceServiceImpl service = new DefResourceServiceImpl(apiManager);
        ReflectionTestUtils.setField(service, "superManager", resourceManager);

        service.deleteRoleResourceRelByResourceId(Collections.emptyList());
        verify(resourceManager, never()).deleteRoleResourceRelByResourceId(any());

        service.deleteRoleResourceRelByResourceId(List.of(1L, 2L));
        verify(resourceManager).deleteRoleResourceRelByResourceId(List.of(1L, 2L));
    }

    @Test
    @DisplayName("测试 saveWithCache 菜单校验、非顶级挂载与缓存淘汰")
    void testSaveWithCache() {
        DefResourceApiManager apiManager = mock(DefResourceApiManager.class);
        DefResourceManager resourceManager = mock(DefResourceManager.class);
        CacheOps cacheOps = mock(CacheOps.class);

        DefResourceServiceImpl service = new DefResourceServiceImpl(apiManager);
        ReflectionTestUtils.setField(service, "superManager", resourceManager);
        ReflectionTestUtils.setField(service, "cacheOps", cacheOps);

        // 1. 缺少 path
        DefResourceSaveVO saveVO = new DefResourceSaveVO();
        saveVO.setResourceType(ResourceTypeEnum.MENU.getCode());
        saveVO.setName("系统管理");
        assertThrows(ArgumentException.class, () -> service.saveWithCache(saveVO));

        // 2. 缺少 component
        saveVO.setPath("/sys");
        assertThrows(ArgumentException.class, () -> service.saveWithCache(saveVO));

        // 3. 校验重名与编码重复
        saveVO.setComponent("Layout");
        saveVO.setApplicationId(1L);
        saveVO.setCode("SYS_MANAGE");
        when(resourceManager.count(any())).thenReturn(1L); // checkName 重复
        assertThrows(ArgumentException.class, () -> service.saveWithCache(saveVO));

        // checkName 不重复，check 重复
        when(resourceManager.count(any())).thenReturn(0L).thenReturn(0L).thenReturn(1L);
        assertThrows(ArgumentException.class, () -> service.saveWithCache(saveVO));

        // 4. 非法 JSON metaJson
        when(resourceManager.count(any())).thenReturn(0L);
        saveVO.setMetaJson("not_a_json_format_at_all{{{");
        assertThrows(BizException.class, () -> service.saveWithCache(saveVO));

        // 5. 挂载到非法父节点 (父节点不是 MENU)
        saveVO.setMetaJson("{\"title\": \"系统\"}");
        saveVO.setParentId(100L);
        DefResource parent = new DefResource();
        parent.setId(100L);
        parent.setResourceType(ResourceTypeEnum.BUTTON.getCode());
        when(resourceManager.getById(100L)).thenReturn(parent);
        assertThrows(ArgumentException.class, () -> service.saveWithCache(saveVO));

        // 挂载到隐藏父节点
        parent.setResourceType(ResourceTypeEnum.MENU.getCode());
        parent.setIsHidden(true);
        assertThrows(ArgumentException.class, () -> service.saveWithCache(saveVO));

        // 6. 成功保存根节点
        saveVO.setParentId(0L);
        saveVO.setIsGeneral(false);
        saveVO.setState(true);
        DefResourceApiSaveVO apiSaveVO = new DefResourceApiSaveVO();
        apiSaveVO.setName("按钮");
        apiSaveVO.setUri("/sys/btn");
        saveVO.setResourceApiList(List.of(apiSaveVO));

        when(resourceManager.save(any())).thenReturn(true);
        DefResource saved = service.saveWithCache(saveVO);
        assertNotNull(saved);
        assertEquals(DefValConstants.PARENT_ID, saved.getParentId());
        assertEquals(DefValConstants.TREE_GRADE, saved.getTreeGrade());
        verify(apiManager).saveBatch(anyList());
        verify(cacheOps, atLeastOnce()).del(any(CacheKey.class));

        // 7. 成功保存子节点
        parent.setIsHidden(false);
        parent.setIsGeneral(true);
        parent.setState(true);
        parent.setTreeGrade(1);
        parent.setTreePath("0,");
        when(resourceManager.getById(100L)).thenReturn(parent);
        when(resourceManager.getByIdCache(100L)).thenReturn(parent);

        saveVO.setParentId(100L);
        saveVO.setIsGeneral(true);
        DefResource savedChild = service.saveWithCache(saveVO);
        assertNotNull(savedChild);
        assertEquals(2, savedChild.getTreeGrade());
    }

    @Test
    @DisplayName("测试 updateWithCacheById 校验与级联状态校验")
    void testUpdateWithCacheById() {
        DefResourceApiManager apiManager = mock(DefResourceApiManager.class);
        DefResourceManager resourceManager = mock(DefResourceManager.class);
        CacheOps cacheOps = mock(CacheOps.class);

        DefResourceServiceImpl service = new DefResourceServiceImpl(apiManager);
        ReflectionTestUtils.setField(service, "superManager", resourceManager);
        ReflectionTestUtils.setField(service, "cacheOps", cacheOps);

        DefResourceUpdateVO updateVO = new DefResourceUpdateVO();
        updateVO.setId(200L);
        updateVO.setResourceType(ResourceTypeEnum.MENU.getCode());
        updateVO.setPath("http://external.link.com");
        updateVO.setComponent("IFrame");
        updateVO.setName("外部链接");
        updateVO.setCode("EXT_LINK");
        updateVO.setApplicationId(1L);
        updateVO.setParentId(0L);
        updateVO.setIsGeneral(false);
        updateVO.setState(false);

        // checkName check count 均为 0
        when(resourceManager.count(any())).thenReturn(0L);
        when(resourceManager.updateById(any())).thenReturn(true);

        // update 时，如果当前节点将 isGeneral 改为 false，但有子节点的 isGeneral 仍为 true，需抛出异常
        DefResource child = new DefResource();
        child.setId(201L);
        child.setIsGeneral(true);
        child.setState(true);
        child.setName("子菜单");
        when(resourceManager.findChildrenByParentId(200L)).thenReturn(List.of(child));

        assertThrows(ArgumentException.class, () -> service.updateWithCacheById(updateVO));

        // 子节点 isGeneral 为 false，但 state 为 true，当前 state 改为 false，需抛出异常
        child.setIsGeneral(false);
        assertThrows(ArgumentException.class, () -> service.updateWithCacheById(updateVO));

        // 子节点全为 false，更新成功
        child.setState(false);
        DefResource updated = service.updateWithCacheById(updateVO);
        assertNotNull(updated);
        verify(resourceManager).updateById(any());
        verify(apiManager).removeByResourceId(List.of(200L));
    }

    @Test
    @DisplayName("测试 moveResource 树节点移动与递归调整")
    void testMoveResource() {
        DefResourceApiManager apiManager = mock(DefResourceApiManager.class);
        DefResourceManager resourceManager = mock(DefResourceManager.class);
        CacheOps cacheOps = mock(CacheOps.class);

        DefResourceServiceImpl service = new DefResourceServiceImpl(apiManager);
        ReflectionTestUtils.setField(service, "superManager", resourceManager);
        ReflectionTestUtils.setField(service, "cacheOps", cacheOps);

        // 1. id 为空
        assertThrows(ArgumentException.class, () -> service.moveResource(null, 1L));

        // 2. 资源不存在
        when(resourceManager.getByIdCache(100L)).thenReturn(null);
        assertThrows(ArgumentException.class, () -> service.moveResource(100L, 1L));

        // 3. 根节点移动到根节点 (无变化)
        DefResource current = new DefResource();
        current.setId(100L);
        current.setParentId(0L);
        when(resourceManager.getByIdCache(100L)).thenReturn(current);
        service.moveResource(100L, 0L);
        verify(resourceManager, never()).updateById(any());

        // 4. 自己移动到自己下 (无变化)
        current.setParentId(10L);
        service.moveResource(100L, 10L);
        verify(resourceManager, never()).updateById(any());

        // 5. 移动到自己或者子节点 (报错)
        DefResource child = new DefResource();
        child.setId(101L);
        child.setParentId(100L);
        when(resourceManager.findAllChildrenByParentId(100L)).thenReturn(List.of(child));
        assertThrows(ArgumentException.class, () -> service.moveResource(100L, 100L));
        assertThrows(ArgumentException.class, () -> service.moveResource(100L, 101L));

        // 6. 成功移动到新的父节点
        DefResource newParent = new DefResource();
        newParent.setId(200L);
        newParent.setTreeGrade(1);
        newParent.setTreePath("0,");
        when(resourceManager.getByIdCache(200L)).thenReturn(newParent);

        service.moveResource(100L, 200L);
        verify(resourceManager).updateBatchById(anyList());
        verify(resourceManager).updateById(current);
        verify(resourceManager).delCache(anyList());
        assertEquals(2, current.getTreeGrade());
        assertEquals("0,200/", current.getTreePath());
    }
}
