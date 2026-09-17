package com.dalio.cloud.system.facade.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.dalio.basic.base.R;
import com.dalio.basic.cache.redis2.CacheResult;
import com.dalio.basic.cache.repository.CacheOps;
import com.dalio.cloud.model.vo.result.ResourceApiVO;
import com.dalio.cloud.system.service.application.DefResourceService;
import com.dalio.cloud.system.service.tenant.DefUserService;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 单体版 DefResourceFacadeImpl 与 DefUserFacadeImpl 单元测试
 */
class DefFacadeBootImplTest {

    @Test
    @DisplayName("测试 DefResourceFacadeImpl listAllApi 与 URI 裁剪及去重逻辑")
    void testDefResourceFacadeImpl() {
        DefResourceService resourceService = mock(DefResourceService.class);
        CacheOps cacheOps = mock(CacheOps.class);

        DefResourceFacadeImpl facade = new DefResourceFacadeImpl(resourceService, cacheOps);

        ResourceApiVO vo1 = new ResourceApiVO();
        vo1.setUri("/gateway/api/v1/test");
        vo1.setRequestMethod("GET");
        vo1.setCode("USER_VIEW");

        ResourceApiVO vo2 = new ResourceApiVO();
        vo2.setUri("/base/api/v1/users");
        vo2.setRequestMethod("POST");
        vo2.setCode("USER_ADD");

        ResourceApiVO vo3 = new ResourceApiVO();
        vo3.setUri("/base/api/v1/users");
        vo3.setRequestMethod("POST");
        vo3.setCode("USER_EDIT");

        when(resourceService.findAllApi()).thenReturn(List.of(vo1, vo2, vo3));

        when(cacheOps.get(any(com.dalio.basic.model.cache.CacheKey.class), any(Function.class), any(boolean[].class))).thenAnswer(invocation -> {
            Function<Object, Map<String, Set<String>>> loader = invocation.getArgument(1);
            return new CacheResult<>("k", loader.apply(invocation.getArgument(0)));
        });

        Map<String, Set<String>> apiMap = facade.listAllApi();
        assertNotNull(apiMap);
        assertEquals(2, apiMap.size());

        // /gateway 路径保持不变
        assertTrue(apiMap.containsKey("/gateway/api/v1/test###GET"));
        assertEquals(Set.of("USER_VIEW"), apiMap.get("/gateway/api/v1/test###GET"));

        // /base 路径裁剪第一段后保留 /api/v1/users，且 vo2 和 vo3 的 code 合并
        assertTrue(apiMap.containsKey("/api/v1/users###POST"));
        assertEquals(Set.of("USER_ADD", "USER_EDIT"), apiMap.get("/api/v1/users###POST"));
    }

    @Test
    @DisplayName("测试 DefUserFacadeImpl 查询全部用户ID与按ID集合查询")
    void testDefUserFacadeImpl() {
        DefUserService userService = mock(DefUserService.class);
        DefUserFacadeImpl facade = new DefUserFacadeImpl(userService);

        // 1. findAllUserId
        when(userService.findUserIdList(null)).thenReturn(List.of(1L, 2L, 3L));
        R<List<Long>> userIdsR = facade.findAllUserId();
        assertTrue(userIdsR.getIsSuccess());
        assertEquals(List.of(1L, 2L, 3L), userIdsR.getData());

        // 2. findByIds
        Set<Serializable> ids = Set.of(1L, 2L);
        when(userService.findByIds(ids)).thenReturn(Map.of(1L, "admin", 2L, "user"));
        Map<Serializable, Object> resMap = facade.findByIds(ids);
        assertEquals(2, resMap.size());
        assertEquals("admin", resMap.get(1L));
    }
}
