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
@Schema(description = "是否-枚举")
public enum BooleanEnum implements BaseEnum {
    /**
     * true
     */
    TRUE(true, 1, "1", "是"),
    /**
     * false
     */
    FALSE(false, 0, "0", "否");
    private final Boolean bool;
    private final int integer;
    private final String str;
    private final String desc;

    @Override
    public String getCode() {
        return this.bool.toString();
    }


    public boolean eq(BooleanEnum val) {
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
