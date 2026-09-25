package com.dalio.cloud.model.enumeration;

import com.dalio.basic.interfaces.BaseEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * <p>
 * 实体注释中生成的类型枚举
 * 用户
 * </p>
 *
 * @author admin
 * @date 2021-10-09
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Sex", description = "性别-枚举")
public enum Sex implements BaseEnum {

    /**
     * M="男"
     */
    M("1", "男"),
    /**
     * W="女"
     */
    W("2", "女");

    private String code;
    @Schema(description = "描述")
    private String desc;

    /**
     * 根据当前枚举的name匹配
     */
    public static Sex match(String val, Sex def) {
        for (Sex item : values()) {
            if (item.getCode().equalsIgnoreCase(val)) {
                return item;
            }
        }
        return def;
    }

    public static Sex get(String val) {
        return match(val, null);
    }

    public boolean eq(Sex val) {
        return this == val;
    }
}
