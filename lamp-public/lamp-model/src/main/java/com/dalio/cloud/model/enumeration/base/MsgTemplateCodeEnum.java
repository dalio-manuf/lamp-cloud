package com.dalio.cloud.model.enumeration.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.dalio.basic.interfaces.BaseEnum;

/**
 * @author admin
 * @version v1.0
 * @date 2022/7/28 8:09 AM
 * @create [2022/7/28 8:09 AM ] [admin] [初始创建]
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "MsgTemplateCodeEnum", description = "消息模板类型-枚举")
public enum MsgTemplateCodeEnum implements BaseEnum {
    // 您的验证码为：${code}，请勿将验证码泄露给他人
    REGISTER_SMS("REGISTER_SMS", "注册短信"),
    // 您的验证码为：${code}，请勿将验证码泄露给他人
    REGISTER_EMAIL("REGISTER_EMAIL", "注册邮件验证码"),
    // 您的验证码为：${code}，请勿将验证码泄露给他人
    MOBILE_LOGIN("MOBILE_LOGIN", "手机登录短信"),
    MOBILE_EDIT("MOBILE_EDIT", "修改手机号"),
    EMAIL_EDIT("EMAIL_EDIT", "修改邮箱"),
    FORGET_PASSWORD("FORGET_PASSWORD", "忘记密码");

    private String value;
    private String desc;

    /**
     * 根据当前枚举的name匹配
     */
    public static MsgTemplateCodeEnum match(String val, MsgTemplateCodeEnum def) {
        for (MsgTemplateCodeEnum item : values()) {
            if (item.name().equalsIgnoreCase(val)) {
                return item;
            }
        }
        return def;
    }

    public static MsgTemplateCodeEnum get(String val) {
        return match(val, null);
    }

    public boolean eq(MsgTemplateCodeEnum val) {
        return this == val;
    }

    @Override
    @Schema(description = "编码", allowableValues = "REGISTER_SMS,REGISTER_EMAIL,MOBILE_LOGIN,MOBILE_EDIT,EMAIL_EDIT,FORGET_PASSWORD", example = "REGISTER_SMS")
    public String getCode() {
        return this.value;
    }
}
