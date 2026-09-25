package com.dalio.cloud.model.enumeration.system;

import com.dalio.basic.interfaces.BaseEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 资源类型
 *
 * @author admin
 * @since 2021/3/12 21:20
 */
@Getter
@AllArgsConstructor
@Schema(title = "ResourceTypeEnum", description = "资源类型-枚举")
public enum ResourceTypeEnum implements BaseEnum {
    /**
     * 菜单
     */
    MENU("20", "菜单"),
    /**
     * 视图
     */
//    VIEW("30", "视图"),
    /**
     * 按钮
     */
    BUTTON("40", "按钮"),
    /**
     * 字段
     */
    FIELD("50", "字段"),

    /**
     * 数据权限
     */
    DATA("60", "数据");

    /**
     * 资源类型
     */
    private final String code;

    /**
     * 资源描述
     */
    private final String desc;

    public static ResourceTypeEnum match(String val, ResourceTypeEnum def) {
        for (ResourceTypeEnum item : values()) {
            if (item.name().equalsIgnoreCase(val) || item.code.equalsIgnoreCase(val)) {
                return item;
            }
        }
        return def;
    }

    public static ResourceTypeEnum get(String val) {
        return match(val, null);
    }

    public boolean eq(ResourceTypeEnum val) {
        return this == val;
    }

    @Override
    public String getDesc() {
        return desc;
    }

    @Override
    @Schema(description = "编码", allowableValues = "20,30,40,50,60", example = "20")
    public String getCode() {
        return this.code;
    }
}
