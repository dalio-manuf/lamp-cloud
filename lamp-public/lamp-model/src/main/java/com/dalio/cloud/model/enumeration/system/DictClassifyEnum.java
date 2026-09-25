package com.dalio.cloud.model.enumeration.system;

import com.dalio.basic.interfaces.BaseEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;


/**
 * 字典分类
 * 必须和数据字典【EchoDictType.System.DICT_CLASSIFY】 保持一致
 *
 * @author admin
 * @date 2021/3/15 3:34 下午
 */
@Getter
@Schema(title = "DictClassifyEnum", description = "字典分类-枚举")
public enum DictClassifyEnum implements BaseEnum {

    /**
     * 系统字典
     */
    SYSTEM("10", "系统字典"),
    ENUM("30", "枚举字典"),

    /**
     * 业务字典
     */
    BUSINESS("20", "业务字典");

    private final String code;
    private final String desc;

    DictClassifyEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 根据当前枚举的name匹配
     */
    public static DictClassifyEnum match(String val, DictClassifyEnum def) {
        for (DictClassifyEnum item : values()) {
            if (item.name().equalsIgnoreCase(val) || item.code.equalsIgnoreCase(val)) {
                return item;
            }
        }
        return def;
    }

    public static DictClassifyEnum get(String val) {
        return match(val, null);
    }

    public boolean eq(DictClassifyEnum val) {
        return this == val;
    }

    @Override
    @Schema(description = "编码", allowableValues = "10,20", example = "20")
    public String getCode() {
        return this.code;
    }

}
