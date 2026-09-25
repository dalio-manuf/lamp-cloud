package com.dalio.cloud.gateway.rule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.dalio.basic.context.ContextConstants;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 基于客户端版本号的灰度负载均衡器
 * 支持按 gray_version 筛选多个匹配微服务实例并进行均匀随机负载分发
 *
 * @author dalio
 * @date 2021年07月13日08:35:39
 */
@Slf4j
@AllArgsConstructor
public class GrayVersionLoadBalancer implements GrayscaleLoadBalancer {
    private final DiscoveryClient discoveryClient;

    /**
     * 根据 serviceId 筛选可用服务实例
     *
     * @param serviceId 服务ID
     * @param request   当前请求
     * @return 选中的服务实例
     */
    @Override
    public ServiceInstance choose(String serviceId, ServerHttpRequest request) {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);

        // 注册中心无可用实例，抛出 404 异常
        if (CollUtil.isEmpty(instances)) {
            log.warn("Nacos 中未找到可用的服务实例: {}", serviceId);
            throw new NotFoundException("Nacos 中未找到可用的服务实例: " + serviceId);
        }

        // 获取请求中的 gray_version 请求头；若无灰度版本则随机负载
        String grayVersion = request.getHeaders().getFirst(ContextConstants.GRAY_VERSION);
        if (StrUtil.isBlank(grayVersion)) {
            return instances.get(ThreadLocalRandom.current().nextInt(instances.size()));
        }

        // 筛选匹配灰度版本的微服务实例列表
        List<ServiceInstance> matchedInstances = instances.stream()
                .filter(instance -> grayVersion.equalsIgnoreCase(instance.getMetadata().get(ContextConstants.GRAY_VERSION)))
                .toList();

        if (CollUtil.isNotEmpty(matchedInstances)) {
            ServiceInstance chosen = matchedInstances.get(ThreadLocalRandom.current().nextInt(matchedInstances.size()));
            log.debug("灰度匹配成功，版本参数：{} 选中实例：{}", grayVersion, chosen);
            return chosen;
        }

        // 未匹配到对应灰度版本时降级为所有实例中随机分配
        return instances.get(ThreadLocalRandom.current().nextInt(instances.size()));
    }
}

