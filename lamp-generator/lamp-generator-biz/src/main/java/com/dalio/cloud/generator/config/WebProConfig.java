package com.dalio.cloud.generator.config;

import com.dalio.cloud.generator.enumeration.TplEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * web pro 配置
 *
 * @author admin
 * @date 2022/3/23 22:31
 */
@Data
@NoArgsConstructor
public class WebProConfig {
    /**
     * 格式化菜单文件名称
     */
    private String formatMenuName = "{}管理";

    /**
     * 前端生成页面样式模板
     */
    private TplEnum tpl = TplEnum.SIMPLE;

}
