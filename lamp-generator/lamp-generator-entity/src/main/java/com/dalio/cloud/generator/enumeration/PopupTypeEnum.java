package com.dalio.cloud.generator.enumeration;

import com.dalio.basic.interfaces.BaseEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.stream.Stream;

/**
 * <p>
 * 弹窗方式
 * </p>
 *
 * @author admin
 * @date 2021-11-08
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "弹窗方式")
public enum PopupTypeEnum implements BaseEnum {

    /**
     * 对话框
     */
    MODAL("01", "对话框"),
    /**
     * 抽屉
     */
    DRAWER("02", "抽屉"),
    /**
     * 跳转
     */
    JUMP("03", "跳转");

    private String value;
    private String desc;


    /**
     * 根据当前枚举的name匹配
     */
    public static PopupTypeEnum match(String val, PopupTypeEnum def) {
        return Stream.of(values()).filter(item -> item.name().equalsIgnoreCase(val)).findAny().orElse(def);
    }

    public static PopupTypeEnum get(String val) {
        return match(val, null);
    }

    public boolean eq(PopupTypeEnum val) {
        return this == val;
    }

    @Override
    @Schema(description = "编码", example = "01")
    public String getCode() {
        return this.name();
    }


}
