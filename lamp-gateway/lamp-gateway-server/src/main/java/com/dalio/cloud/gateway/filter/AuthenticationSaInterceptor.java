package com.dalio.cloud.gateway.filter;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.error.SaErrorCode;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.SaTokenException;
import cn.dev33.satoken.exception.StopMatchException;
import cn.dev33.satoken.reactor.context.SaReactorHolder;
import cn.dev33.satoken.reactor.context.SaReactorSyncHolder;
import cn.dev33.satoken.router.SaHttpMethod;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.spring.pathmatch.SaPathPatternParserUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.dalio.basic.base.R;
import com.dalio.cloud.common.properties.IgnoreProperties;
import com.dalio.cloud.model.vo.result.ResourceApiVO;
import com.dalio.cloud.system.facade.DefResourceFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Sa-Token 全局认证与鉴权过滤器
 *
 * @author dalio
 * @since 2024/8/6 16:33
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AuthenticationSaInterceptor implements WebFilter, Ordered {
    private final DefResourceFacade defResourceFacade;
    private final IgnoreProperties ignoreProperties;

    @Override
    public int getOrder() {
        return OrderedConstant.AUTHENTICATION;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // 写入 WebFilterChain 对象
        exchange.getAttributes().put(SaReactorHolder.EXCHANGE_KEY, chain);

        // ---------- 全局认证与鉴权处理
        try {
            // 写入全局上下文 (同步)
            SaReactorSyncHolder.setContext(exchange);

            Map<String, Set<String>> anyUser = ignoreProperties.buildAnyUser();
            // 验证登录状态：排除配置了无需登录的白名单接口
            SaRouter
                    .match("/**")
                    .notMatch(r -> {
                        String path = SaHolder.getRequest().getRequestPath();
                        String method = SaHolder.getRequest().getMethod();
                        for (Map.Entry<String, Set<String>> map : anyUser.entrySet()) {
                            String key = map.getKey();
                            Set<String> value = map.getValue();
                            if (StrUtil.equalsAny(key, method, SaHttpMethod.ALL.name())) {
                                for (String ignore : value) {
                                    if (StrUtil.equals(ignore, path) || SaPathPatternParserUtil.match(ignore, path)) {
                                        return true;
                                    }
                                }
                            }
                        }
                        return false;
                    })
                    .check(r -> StpUtil.checkLogin());

            // 接口鉴权逻辑（若开启了鉴权）
            if (Boolean.TRUE.equals(ignoreProperties.getAuthEnabled())) {
                Map<String, Set<String>> anyone = ignoreProperties.buildAnyone();
                Map<String, Set<String>> allApi = defResourceFacade.listAllApi();

                allApi.forEach((api, auth) -> {
                    List<String> list = StrUtil.split(api, "###");
                    if (list.size() < 2) {
                        return;
                    }
                    String uri = list.get(0);
                    String requestMethod = list.get(1);
                    SaRouter.match(uri).matchMethod(requestMethod)
                            .notMatch(r -> {
                                String path = SaHolder.getRequest().getRequestPath();
                                String method = SaHolder.getRequest().getMethod();
                                for (Map.Entry<String, Set<String>> map : anyone.entrySet()) {
                                    String key = map.getKey();
                                    Set<String> value = map.getValue();
                                    if (StrUtil.equalsAny(key, method, SaHttpMethod.ALL.name())) {
                                        for (String ignore : value) {
                                            if (StrUtil.equals(ignore, path) || SaPathPatternParserUtil.match(ignore, path)) {
                                                return true;
                                            }
                                        }
                                    }
                                }
                                return false;
                            })
                            .check(r -> StpUtil.checkPermissionOr(auth.toArray(String[]::new)));
                });

                if (!ignoreProperties.getNotConfigUriAllow()) {
                    String path = SaHolder.getRequest().getRequestPath();
                    String method = SaHolder.getRequest().getMethod();
                    ResourceApiVO resourceApi = new ResourceApiVO();
                    resourceApi.setUri(path);
                    resourceApi.setRequestMethod(method);

                    if (!ignoreProperties.isIgnoreAnyone(method, path)) {
                        boolean flag = false;
                        for (Map.Entry<String, Set<String>> map : allApi.entrySet()) {
                            List<String> list = StrUtil.split(map.getKey(), "###");
                            if (list.size() < 2) {
                                continue;
                            }
                            String uri = list.get(0);
                            String requestMethod = list.get(1);

                            if (StrUtil.equalsAny(requestMethod, method, SaHttpMethod.ALL.name())) {
                                if (StrUtil.equals(uri, path) || SaPathPatternParserUtil.match(uri, path)) {
                                    flag = true;
                                    break;
                                }
                            }
                        }

                        if (!flag) {
                            throw new NotPermissionException(resourceApi.getUri(), StpUtil.TYPE).setCode(SaErrorCode.CODE_11051);
                        }
                    }
                }
            }

        } catch (StopMatchException e) {
            // StopMatchException 异常代表：匹配终止，正常放行进入后续环节
            log.debug("Sa-Token 匹配流程终止: {}", e.getMessage());
        } catch (SaTokenException e) {
            String result = e.getMessage();
            log.warn("[AuthenticationSaInterceptor] Sa-Token 鉴权失败: code={}, msg={}", e.getCode(), result);
            ServerHttpResponse response = exchange.getResponse();
            R<?> tokenError = R.fail(e.getCode(), result);
            response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            DataBuffer dataBuffer = response.bufferFactory().wrap(cn.hutool.json.JSONUtil.toJsonStr(tokenError).getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(dataBuffer));
        } catch (Throwable e) {
            log.error("[AuthenticationSaInterceptor] 鉴权处理异常: ", e);
            String result = e.getMessage();
            ServerHttpResponse response = exchange.getResponse();
            R<?> tokenError = R.fail(result);
            response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            DataBuffer dataBuffer = response.bufferFactory().wrap(cn.hutool.json.JSONUtil.toJsonStr(tokenError).getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(dataBuffer));
        } finally {
            // 清除同步上下文
            SaReactorSyncHolder.clearContext();
        }

        // ---------- 写入全局上下文并继续执行响应式链
        SaReactorSyncHolder.setContext(exchange);
        return chain.filter(exchange).contextWrite(ctx -> ctx.put(SaReactorHolder.EXCHANGE_KEY, exchange))
                .doFinally(r -> SaReactorSyncHolder.clearContext());
    }
}
