package com.dalio.cloud.oauth.enumeration;

import com.dalio.basic.interfaces.BaseEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 实体注释中生成的类型枚举
 * 角色
 * </p>
 *
 * @author admin
 * @date 2021-10-21
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "GrantType", description = "授权类型-枚举")
public enum GrantType implements BaseEnum {
    /**
     * 验证码登录
     */
    CAPTCHA("验证码登录"),
    /**
     * 账号(身份证,邮箱,用户名)密码登录
     */
    PASSWORD("账号密码登录"),
    /**
     * 手机登录
     */
    MOBILE("手机登录");

    @Schema(description = "描述")
    private String desc;

    /**
     * 根据当前枚举的name匹配
     */
    public static GrantType match(String val, GrantType def) {
        for (GrantType item : values()) {
            if (item.name().equalsIgnoreCase(val)) {
                return item;
            }
        }
        return def;
    }

    public static GrantType get(String val) {
        return match(val, null);
    }

    public boolean eq(GrantType val) {
        return this == val;
    }

    @Override
    @Schema(description = "编码", allowableValues = "CAPTCHA,PASSWORD,MOBILE", example = "CAPTCHA")
    public String getCode() {
        return this.name();
    }

    @Override
    public String getDesc() {
        return this.desc != null ? this.desc : this.name();
    }
}
