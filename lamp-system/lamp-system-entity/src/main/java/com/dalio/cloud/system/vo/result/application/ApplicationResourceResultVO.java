package com.dalio.cloud.system.vo.result.application;

import com.dalio.cloud.system.entity.application.DefApplication;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author admin
 * @version 4.0
 * @date 2021/9/27 11:37 下午
 * @create [2021/9/27 11:37 下午 ] [admin] [初始创建]
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
@Builder
@Schema(description = "应用资源返回")
public class ApplicationResourceResultVO implements Serializable {
    private DefApplication defApplication;
    private Collection<DefResourceResultVO> resourceList;
}
