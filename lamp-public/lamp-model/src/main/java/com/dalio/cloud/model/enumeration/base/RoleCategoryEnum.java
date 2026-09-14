package com.dalio.cloud.model.enumeration.base;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.dalio.basic.interfaces.BaseEnum;

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
@Schema(description = "角色类别-枚举")
public enum RoleCategoryEnum implements BaseEnum {

    /**
     * 功能角色
     */
    FUNCTION("10", "功能角色"),
    /**
     * 桌面角色
     */
    DESKTOP("20", "桌面角色"),
    /**
     * 数据角色
     */
    DATA_SCOPE("30", "数据角色");

    @Schema(description = "描述")
    private String code;

    private String desc;


    /**
     * 根据当前枚举的name匹配
     */
    public static RoleCategoryEnum match(String val, RoleCategoryEnum def) {
        for (RoleCategoryEnum item : values()) {
            if (item.name().equalsIgnoreCase(val) || item.code.equalsIgnoreCase(val)) {
                return item;
            }
        }
        return def;
    }

    public static RoleCategoryEnum get(String val) {
        return match(val, null);
    }

    public boolean eq(RoleCategoryEnum val) {
        return this == val;
    }

    @Override
    @Schema(description = "编码", allowableValues = "FUNCTION,DESKTOP,DATA_SCOPE", example = "FUNCTION")
    public String getCode() {
        return this.code;
    }

}
