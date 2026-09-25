package com.dalio.cloud.msg.vo.result;

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
 * 表单查询方法返回值VO
 * 消息接收人
 * </p>
 *
 * @author admin
 * @date 2022-07-10 11:41:17
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Builder
@Schema(title = "ExtendMsgRecipientResultVO", description = "消息接收人")
public class ExtendMsgRecipientResultVO extends Entity<Long> implements Serializable, EchoVO {

    private static final long serialVersionUID = 1L;
    @Builder.Default

    private final Map<String, Object> echoMap = MapUtil.newHashMap();

    @Schema(description = "ID")
    private Long id;

    /**
     * 任务ID;
     * <p>
     * #extend_msg
     */
    @Schema(description = "任务ID")
    private Long msgId;
    /**
     * 接收人;
     * 可能是手机号、邮箱、用户ID等
     */
    @Schema(description = "接收人")
    private String recipient;


    @Schema(description = "扩展信息")
    private String ext;

}
