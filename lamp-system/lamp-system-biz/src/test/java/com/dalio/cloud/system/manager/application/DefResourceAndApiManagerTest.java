package com.dalio.cloud.system.manager.application;

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
import com.dalio.cloud.model.enumeration.system.ResourceTypeEnum;
import com.dalio.cloud.model.vo.result.ResourceApiVO;
import com.dalio.cloud.system.entity.application.DefResource;
import com.dalio.cloud.system.entity.application.DefResourceApi;
import com.dalio.cloud.system.manager.application.impl.DefResourceApiManagerImpl;
import com.dalio.cloud.system.manager.application.impl.DefResourceManagerImpl;
import com.dalio.cloud.system.mapper.application.DefResourceApiMapper;
import com.dalio.cloud.system.mapper.application.DefResourceMapper;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DefResourceAndApiManagerTest {

    @BeforeAll
    static void init() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, DefResource.class);
        TableInfoHelper.initTableInfo(assistant, DefResourceApi.class);
    }

    @Test
    @DisplayName("测试 DefResourceManagerImpl 查询与参数校验")
    void testResourceManager() {
        DefResourceMapper mapper = mock(DefResourceMapper.class);
        DefResourceManagerImpl manager = Mockito.spy(new DefResourceManagerImpl());
        ReflectionTestUtils.setField(manager, "baseMapper", mapper);

        assertNotNull(ReflectionTestUtils.invokeMethod(manager, "cacheKeyBuilder"));

        // 1. findResourceListByApplicationId 空 applicationIdList
        DefResource r1 = new DefResource();
        r1.setId(1L);
        r1.setState(true);
        r1.setSortValue(1);
        r1.setResourceType(ResourceTypeEnum.MENU.getCode());
        doReturn(List.of(r1)).when(manager).list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));
        assertEquals(1, manager.findResourceListByApplicationId(Collections.emptyList(), null).size());

        // 2. findResourceListByApplicationId 非空
        doReturn(List.of(1L)).when(manager).listObjs(any(), any());
        doReturn(List.of(r1)).when(manager).findByIds(anyCollection(), any());
        List<DefResource> resList = manager.findResourceListByApplicationId(List.of(100L), Set.of(ResourceTypeEnum.MENU.getCode()));
        assertEquals(1, resList.size());

        // 3. 参数校验异常
        assertThrows(ArgumentException.class, () -> manager.findByApplicationId(Collections.emptyList()));
        assertThrows(ArgumentException.class, () -> manager.findChildrenByParentId(null));
        assertThrows(ArgumentException.class, () -> manager.findAllChildrenByParentId(null));

        // 4. 正常查询
        assertEquals(1, manager.findByApplicationId(List.of(100L)).size());
        assertEquals(1, manager.findChildrenByParentId(10L).size());
        assertEquals(1, manager.findAllChildrenByParentId(10L).size());

        // 5. deleteRoleResourceRelByResourceId
        when(mapper.deleteRoleResourceRelByResourceId(anyList())).thenReturn(5);
        assertEquals(5, manager.deleteRoleResourceRelByResourceId(List.of(1L)));
    }

    @Test
    @DisplayName("测试 DefResourceApiManagerImpl findAllApi 与 removeByResourceId")
    void testResourceApiManager() {
        DefResourceApiMapper mapper = mock(DefResourceApiMapper.class);
        CacheOps cacheOps = mock(CacheOps.class);

        DefResourceApiManagerImpl apiManager = Mockito.spy(new DefResourceApiManagerImpl());
        ReflectionTestUtils.setField(apiManager, "baseMapper", mapper);
        ReflectionTestUtils.setField(apiManager, "cacheOps", cacheOps);

        assertNotNull(ReflectionTestUtils.invokeMethod(apiManager, "cacheKeyBuilder"));

        // 1. findAllApi
        when(mapper.findAllApi()).thenReturn(List.of(new ResourceApiVO()));
        assertEquals(1, apiManager.findAllApi().size());

        // 2. findByResourceId
        DefResourceApi api = new DefResourceApi();
        api.setId(10L);
        api.setResourceId(1L);
        doReturn(List.of(api)).when(apiManager).list(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));
        assertEquals(1, apiManager.findByResourceId(1L).size());

        // 3. removeByResourceId
        doReturn(List.of(10L)).when(apiManager).listObjs(any(), any());
        doReturn(true).when(apiManager).remove(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));

        apiManager.removeByResourceId(List.of(1L));
        verify(cacheOps, atLeast(2)).del(any(com.dalio.basic.model.cache.CacheKey[].class));
    }
}
