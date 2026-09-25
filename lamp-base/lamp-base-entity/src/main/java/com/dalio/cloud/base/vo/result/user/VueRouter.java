package com.dalio.cloud.base.vo.result.user;

import cn.hutool.core.map.MapUtil;
import com.dalio.basic.annotation.echo.Echo;
import com.dalio.basic.base.entity.Entity;
import com.dalio.basic.base.entity.TreeEntity;
import com.dalio.basic.interfaces.echo.EchoVO;
import com.dalio.cloud.model.constant.EchoApi;
import com.dalio.cloud.model.constant.EchoDictType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 构建 Vue路由
 *
 * @author admin
 * @date 2019-10-20 15:17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties({TreeEntity.LABEL, TreeEntity.SORT_VALUE, TreeEntity.PARENT_ID, Entity.UPDATED_BY, Entity.UPDATED_TIME, Entity.CREATED_TIME, Entity.CREATED_BY, Entity.ID_FIELD})
public class VueRouter extends TreeEntity<VueRouter, Long> implements EchoVO {

    private static final long serialVersionUID = -3327478146308500708L;
    private final Map<String, Object> echoMap = MapUtil.newHashMap();
    @Schema(description = "路径")
    private String path;
    @Schema(description = "网址")
    private String link;
    @Schema(description = "菜单名称")
    private String name;
    @Schema(description = "组件")
    private String component;
    @Schema(description = "重定向")
    private String redirect;
    @Schema(description = "元数据")
    private RouterMeta meta;
    @Schema(description = "类型")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.System.RESOURCE_TYPE)
    private String resourceType;
    @Schema(description = "打开方式")
    @Echo(api = EchoApi.DICTIONARY_ITEM_FEIGN_CLASS, dictType = EchoDictType.System.RESOURCE_OPEN_WITH)
    private String openWith;

    @JsonIgnore
    private Boolean isHidden;
    @JsonIgnore
    private String metaJson;
    @JsonIgnore
    private String icon;
}
