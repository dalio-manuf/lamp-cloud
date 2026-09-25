package com.dalio.cloud.model.vo.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <p>
 * 实体类
 * 业务附件
 * </p>
 *
 * @author admin
 * @date 2021-06-30
 * @create [2021-06-30] [admin] [初始创建]
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
@Builder
@Schema(title = "AppendixResultVO", description = "业务附件")
public class AppendixResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 业务id
     */
    @Schema(description = "业务id")
    private Long bizId;
    /**
     * 业务类型
     */
    @Schema(description = "业务类型")
    private String bizType;


    @Schema(description = "主键")
    private Long id;

}
