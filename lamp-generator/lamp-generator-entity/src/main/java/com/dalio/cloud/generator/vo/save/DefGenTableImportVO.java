package com.dalio.cloud.generator.vo.save;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author admin
 * @date 2022/3/3 14:59
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = false)
@Builder
@Schema(title = "DefGenTableImportVO", description = "表导入")
public class DefGenTableImportVO {

    private Long dsId;
    private List<String> tableNames;
}
