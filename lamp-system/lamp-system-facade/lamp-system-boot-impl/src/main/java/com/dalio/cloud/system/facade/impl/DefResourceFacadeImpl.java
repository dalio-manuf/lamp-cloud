package com.dalio.cloud.system.facade.impl;

import cn.hutool.core.util.StrUtil;
import com.dalio.basic.cache.redis2.CacheResult;
import com.dalio.basic.cache.repository.CacheOps;
import com.dalio.basic.model.cache.CacheKey;
import com.dalio.cloud.common.cache.tenant.application.AllResourceApiCacheKeyBuilder;
import com.dalio.cloud.model.vo.result.ResourceApiVO;
import com.dalio.cloud.system.facade.DefResourceFacade;
import com.dalio.cloud.system.service.application.DefResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author admin
 * @since 2024/9/21 22:21
 */
@Service
@RequiredArgsConstructor
public class DefResourceFacadeImpl implements DefResourceFacade {
    private final DefResourceService defResourceService;
    private final CacheOps cacheOps;

    @Override
    public Map<String, Set<String>> listAllApi() {
        CacheKey cacheKey = AllResourceApiCacheKeyBuilder.builder();
        CacheResult<Map<String, Set<String>>> result = cacheOps.get(cacheKey, (k) -> findAllApi());
        return result.getValue();
    }

    private Map<String, Set<String>> findAllApi() {
        // 查询系统中配置的URI和权限关系
        List<ResourceApiVO> list = defResourceService.findAllApi();
        return list.stream()
                // 这里单体版和微服务版不同
                .peek(item -> {
                    String uri = item.getUri();
                    if (!StrUtil.startWithAny(uri, "/gateway")) {
                        uri = StrUtil.subSuf(uri, StrUtil.indexOf(uri, '/', 1));
                    }
                    item.setUri(uri);
                })
                .collect(Collectors.toMap(
                        item -> item.getUri() + "###" + item.getRequestMethod(),
                        resourceApiVO -> {
                            Set<String> codes = new HashSet<>();
                            codes.add(resourceApiVO.getCode());
                            return codes;
                        },
                        (existingCodes, newCodes) -> {
                            existingCodes.addAll(newCodes);
                            return existingCodes;
                        },
                        LinkedHashMap::new
                ));
    }
}
