package com.dalio.cloud.model.enumeration.system;

import com.dalio.basic.interfaces.BaseEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


/**
 * <p>
 * 实体注释中生成的类型枚举
 * 企业
 * </p>
 *
 * @author admin
 * @date 2021-10-27
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "TenantConnectTypeEnum", description = "数据源链接类型-枚举")
public enum TenantConnectTypeEnum implements BaseEnum {

    /**
     * SYSTEM="系统内置"
     */
    SYSTEM("系统内置数据源"),
    /**
     * CUSTOM="自定义数据源"
     */
    CUSTOM("自定义数据源");

    @Schema(description = "描述")
    private String desc;


    /**
     * 根据当前枚举的name匹配
     */
    public static TenantConnectTypeEnum match(String val, TenantConnectTypeEnum def) {
        for (TenantConnectTypeEnum item : values()) {
            if (item.name().equalsIgnoreCase(val)) {
                return item;
            }
        }
        return def;
    }

    public static TenantConnectTypeEnum get(String val) {
        return match(val, null);
    }

    public boolean eq(TenantConnectTypeEnum val) {
        return this == val;
    }

    @Override
    @Schema(description = "编码", allowableValues = "SYSTEM,CUSTOM", example = "SYSTEM")
    public String getCode() {
        return this.name();
    }

}
