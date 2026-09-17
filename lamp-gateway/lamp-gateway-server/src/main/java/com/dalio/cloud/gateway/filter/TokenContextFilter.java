package com.dalio.cloud.gateway.filter;

import cn.dev33.satoken.config.SaTokenConfig;
import cn.dev33.satoken.exception.SaTokenException;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.spring.pathmatch.SaPathPatternParserUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import com.dalio.basic.base.R;
import com.dalio.basic.context.ContextConstants;
import com.dalio.basic.context.ContextUtil;
import com.dalio.basic.exception.BizException;
import com.dalio.basic.exception.UnauthorizedException;
import com.dalio.cloud.common.properties.IgnoreProperties;
import com.dalio.cloud.common.utils.Base64Util;

import java.nio.charset.StandardCharsets;

import static com.dalio.basic.context.ContextConstants.APPLICATION_ID_HEADER;
import static com.dalio.basic.context.ContextConstants.APPLICATION_ID_KEY;
import static com.dalio.basic.context.ContextConstants.CLIENT_ID_HEADER;
import static com.dalio.basic.context.ContextConstants.CLIENT_KEY;
import static com.dalio.basic.context.ContextConstants.CURRENT_COMPANY_ID_HEADER;
import static com.dalio.basic.context.ContextConstants.CURRENT_DEPT_ID_HEADER;
import static com.dalio.basic.context.ContextConstants.CURRENT_TOP_COMPANY_ID_HEADER;
import static com.dalio.basic.context.ContextConstants.EMPLOYEE_ID_HEADER;
import static com.dalio.basic.context.ContextConstants.JWT_KEY_COMPANY_ID;
import static com.dalio.basic.context.ContextConstants.JWT_KEY_DEPT_ID;
import static com.dalio.basic.context.ContextConstants.JWT_KEY_EMPLOYEE_ID;
import static com.dalio.basic.context.ContextConstants.JWT_KEY_TOP_COMPANY_ID;
import static com.dalio.basic.context.ContextConstants.USER_ID_HEADER;

/**
 * 租户与鉴权上下文过滤器
 *
 * @author dalio
 * @date 2019/07/31
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TokenContextFilter implements WebFilter, Ordered {
    protected final SaTokenConfig saTokenConfig;
    private final IgnoreProperties ignoreProperties;

    @Override
    public int getOrder() {
        return OrderedConstant.TOKEN;
    }

    /**
     * 忽略 用户token
     */
    protected boolean isIgnoreToken(ServerHttpRequest request) {
        return ignoreProperties.isIgnoreUser(request.getMethod().name(), request.getPath().toString());
    }

    protected String getHeader(String headerName, ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        if (headers.isEmpty()) {
            return StrUtil.EMPTY;
        }

        String token = headers.getFirst(headerName);
        if (StrUtil.isNotBlank(token)) {
            return token;
        }

        return request.getQueryParams().getFirst(headerName);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        ServerHttpRequest.Builder mutate = request.mutate();

        // 剥离客户端自带的内部信任敏感Header，杜绝伪造与越权
        mutate.headers(h -> {
            h.remove(USER_ID_HEADER);
            h.remove(EMPLOYEE_ID_HEADER);
            h.remove(CURRENT_TOP_COMPANY_ID_HEADER);
            h.remove(CURRENT_COMPANY_ID_HEADER);
            h.remove(CURRENT_DEPT_ID_HEADER);
        });

        ContextUtil.setGrayVersion(getHeader(ContextConstants.GRAY_VERSION, request));

        try {
            // 1. 解码 Authorization 获取客户端ID
            parseClient(request, mutate);

            // 2. 获取应用ID
            parseApplication(request, mutate);

            // 3. 解析用户Token上下文
            parseToken(request, mutate);

        } catch (UnauthorizedException e) {
            log.error("[TokenContextFilter] 认证异常: path={}, msg={}", request.getPath(), e.getMessage());
            return errorResponse(response, e.getMessage(), e.getCode(), HttpStatus.UNAUTHORIZED);
        } catch (BizException e) {
            log.error("[TokenContextFilter] 业务异常: path={}, msg={}", request.getPath(), e.getMessage());
            return errorResponse(response, e.getMessage(), e.getCode(), HttpStatus.BAD_REQUEST);
        } catch (SaTokenException e) {
            log.error("[TokenContextFilter] Sa-Token异常: path={}, msg={}", request.getPath(), e.getMessage());
            return errorResponse(response, e.getMessage(), e.getCode(), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("[TokenContextFilter] 验证token出错: path={}", request.getPath(), e);
            return errorResponse(response, "验证token出错", R.FAIL_CODE, HttpStatus.BAD_REQUEST);
        }

        ServerHttpRequest build = mutate.build();
        return chain.filter(exchange.mutate().request(build).build())
                .doFinally(signalType -> {
                    ContextUtil.remove();
                    MDC.clear();
                });
    }

    private void parseToken(ServerHttpRequest request, ServerHttpRequest.Builder mutate) {
        // 判断接口是否需要忽略token验证
        if (isIgnoreToken(request)) {
            log.debug("当前接口：{}, 忽略用户 Token 解析", request.getPath());
            return;
        }

        String token = getHeader(saTokenConfig.getTokenName(), request);
        if (StrUtil.isBlank(token)) {
            return;
        }

        SaSession tokenSession = StpUtil.getTokenSessionByToken(token);
        log.debug("Token session: {}", tokenSession);

        if (tokenSession != null) {
            Long userId = Convert.toLong(tokenSession.getLoginId(), null);
            Long employeeId = Convert.toLong(tokenSession.get(JWT_KEY_EMPLOYEE_ID), null);
            Long topCompanyId = Convert.toLong(tokenSession.get(JWT_KEY_TOP_COMPANY_ID), null);
            Long companyId = Convert.toLong(tokenSession.get(JWT_KEY_COMPANY_ID), null);
            Long deptId = Convert.toLong(tokenSession.get(JWT_KEY_DEPT_ID), null);

            addHeader(mutate, USER_ID_HEADER, userId);
            addHeader(mutate, EMPLOYEE_ID_HEADER, employeeId);
            addHeader(mutate, CURRENT_TOP_COMPANY_ID_HEADER, topCompanyId);
            addHeader(mutate, CURRENT_COMPANY_ID_HEADER, companyId);
            addHeader(mutate, CURRENT_DEPT_ID_HEADER, deptId);
        }
    }

    private void parseClient(ServerHttpRequest request, ServerHttpRequest.Builder mutate) {
        try {
            String pattern = "/actuator/**";
            if (!SaPathPatternParserUtil.match(pattern, request.getPath().toString())) {
                String base64Authorization = getHeader(CLIENT_KEY, request);
                if (StrUtil.isNotEmpty(base64Authorization)) {
                    String[] client = Base64Util.getClient(base64Authorization);
                    ContextUtil.setClientId(client[0]);
                    addHeader(mutate, CLIENT_ID_HEADER, ContextUtil.getClientId());
                }
            }
        } catch (Exception ignore) {
        }
    }

    private void parseApplication(ServerHttpRequest request, ServerHttpRequest.Builder mutate) {
        String applicationIdStr = getHeader(APPLICATION_ID_KEY, request);
        if (StrUtil.isNotEmpty(applicationIdStr)) {
            ContextUtil.setApplicationId(applicationIdStr);
            addHeader(mutate, APPLICATION_ID_HEADER, ContextUtil.getApplicationId());
            MDC.put(APPLICATION_ID_HEADER, applicationIdStr);
        }
    }

    private void addHeader(ServerHttpRequest.Builder mutate, String name, Object value) {
        if (value == null) {
            return;
        }
        String valueStr = value.toString();
        String valueEncode = URLUtil.encode(valueStr);
        mutate.headers(h -> h.set(name, valueEncode));
    }

    protected Mono<Void> errorResponse(ServerHttpResponse response, String errMsg, int errCode, HttpStatus httpStatus) {
        R<?> tokenError = R.fail(errCode, errMsg);
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        response.setStatusCode(httpStatus);
        DataBuffer dataBuffer = response.bufferFactory().wrap(cn.hutool.json.JSONUtil.toJsonStr(tokenError).getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(dataBuffer));
    }

}
