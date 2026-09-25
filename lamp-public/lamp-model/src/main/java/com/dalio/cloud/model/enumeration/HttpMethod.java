package com.dalio.cloud.model.enumeration;

import com.dalio.basic.interfaces.BaseEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


/**
 * HTTP方法枚举
 *
 * @author admin
 */
@Getter
@Schema(title = "HttpMethod", description = "HTTP方法-枚举")
@AllArgsConstructor
@NoArgsConstructor
public enum HttpMethod implements BaseEnum {
    /**
     * ALL
     */
    ALL("ALL", "default"),
    /**
     * GET:GET
     */
    GET("GET", "processing"),
    /**
     * POST:POST
     */
    POST("POST", "success"),
    /**
     * PUT:PUT
     */
    PUT("PUT", "warning"),
    /**
     * DELETE:DELETE
     */
    DELETE("DELETE", "error"),
    /**
     * PATCH:PATCH
     */
    PATCH("PATCH", "default"),
    /**
     * TRACE:TRACE
     */
    TRACE("TRACE", "default"),
    /**
     * HEAD:HEAD
     */
    HEAD("HEAD", "default"),
    /**
     * OPTIONS:OPTIONS
     */
    OPTIONS("OPTIONS", "default");
    @Schema(description = "描述")
    private String desc;
    private String extra;

    public static HttpMethod match(String val, HttpMethod def) {
        for (HttpMethod item : values()) {
            if (item.name().equalsIgnoreCase(val)) {
                return item;
            }
        }
        return def;
    }

    public static HttpMethod get(String val) {
        return match(val, null);
    }

    public boolean eq(HttpMethod val) {
        return this == val;
    }

    @Override
    @Schema(description = "编码", allowableValues = "GET,POST,PUT,DELETE,PATCH,TRACE,HEAD,OPTIONS", example = "GET")
    public String getCode() {
        return this.name();
    }

    @Override
    public String getExtra() {
        return this.extra;
    }
}
