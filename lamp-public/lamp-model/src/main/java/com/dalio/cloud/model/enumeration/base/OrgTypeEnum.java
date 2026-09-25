package com.dalio.cloud.model.enumeration.base;

import com.dalio.basic.interfaces.BaseEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 实体注释中生成的类型枚举
 * 机构类型
 * </p>
 *
 * @author admin
 * @date 2021-11-08
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "机构类型-枚举")
public enum OrgTypeEnum implements BaseEnum {

    /**
     * 单位
     */
    COMPANY("10", "单位"),
    /**
     * 部门
     */
    DEPT("20", "部门");

    private String code;
    private String desc;


    /**
     * 根据当前枚举的name匹配
     */
    public static OrgTypeEnum match(String val, OrgTypeEnum def) {
        for (OrgTypeEnum item : values()) {
            if (item.name().equalsIgnoreCase(val) || item.code.equalsIgnoreCase(val)) {
                return item;
            }
        }
        return def;
    }

    public static OrgTypeEnum get(String val) {
        return match(val, null);
    }

    public boolean eq(OrgTypeEnum val) {
        return this == val;
    }

    @Override
    @Schema(description = "编码", allowableValues = "10,20", example = "10")
    public String getCode() {
        return this.code;
    }


}
