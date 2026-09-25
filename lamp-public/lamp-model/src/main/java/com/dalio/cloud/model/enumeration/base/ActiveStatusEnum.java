package com.dalio.cloud.model.enumeration.base;

import com.dalio.basic.interfaces.BaseEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 激活状态
 *
 * @author admin
 * @date 2018/12/29
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Schema(title = "ActiveStatusEnum", description = "激活状态-枚举")
public enum ActiveStatusEnum implements BaseEnum {
    /**
     * 未激活
     */
    NOT_ACTIVE("10", "未激活"),
    /**
     * 已激活
     */
    ACTIVATED("20", "已激活");

    @Schema(description = "状态")
    private String code;
    @Schema(description = "描述")
    private String desc;

    public static ActiveStatusEnum match(String val, ActiveStatusEnum def) {
        for (ActiveStatusEnum item : values()) {
            if (item.getCode().equalsIgnoreCase(val)) {
                return item;
            }
        }
        return def;
    }

    public static ActiveStatusEnum get(String val) {
        return match(val, null);
    }

    public boolean eq(ActiveStatusEnum val) {
        return this == val;
    }

    @Override
    @Schema(description = "编码", allowableValues = "10,20", example = "10")
    public String getCode() {
        return code;
    }
}
