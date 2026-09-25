package com.dalio.cloud.system.vo.result.application;


import cn.hutool.core.map.MapUtil;
import com.dalio.basic.base.entity.Entity;
import com.dalio.basic.interfaces.echo.EchoVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

/**
 * <p>
 * 实体类
 * 用户的默认应用
 * </p>
 *
 * @author admin
 * @since 2022-03-06
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
@Builder
@Schema(description = "用户的默认应用")
public class DefUserApplicationResultVO extends Entity<Long> implements Serializable, EchoVO {

    private static final long serialVersionUID = 1L;
    @Builder.Default
    private final Map<String, Object> echoMap = MapUtil.newHashMap();

    @Schema(description = "主键")
    private Long id;

    /**
     * 所属用户ID
     */
    @Schema(description = "所属用户ID")

    private Long userId;
    /**
     * 所属应用ID
     */
    @Schema(description = "所属应用ID")

    private Long applicationId;
}
