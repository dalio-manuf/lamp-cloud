package com.dalio.cloud.model.enumeration;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import com.dalio.basic.interfaces.BaseEnum;

/**
 * 是否
 *
 * @author admin
 * @date 2021/4/16 11:26 上午
 */
@Getter
@AllArgsConstructor
@Schema(description = "应用授权枚举")
public enum StateEnum implements BaseEnum {
    /**
     * 启用
     */
    ENABLE(true, 1, "1", "启用"),
    /**
     * 禁用
     */
    DISABLE(false, 0, "0", "禁用");
    private final Boolean bool;
    private final int integer;
    private final String str;
    private final String desc;

    public static StateEnum match(String val, StateEnum... defs) {
        StateEnum def = defs != null && defs.length > 0 ? defs[0] : DISABLE;
        if (val == null) {
            return def;
        }

        for (StateEnum value : StateEnum.values()) {
            if (value.getStr().equals(val)) {
                return value;
            }
        }
        return def;
    }

    @Override
    public String getCode() {
        return this.bool.toString();
    }

    public boolean eq(StateEnum val) {
        return this == val;
    }

    public boolean eq(Integer val) {
        if (val == null) {
            return false;
        }
        return val == this.integer;
    }

    public boolean eq(String val) {
        if (val == null) {
            return false;
        }
        return this.str.equals(val);
    }

    public boolean eq(Boolean val) {
        if (val == null) {
            return false;
        }
        return this.bool.equals(val);
    }
}
