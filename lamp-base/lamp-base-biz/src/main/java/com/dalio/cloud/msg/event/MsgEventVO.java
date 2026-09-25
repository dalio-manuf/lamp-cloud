package com.dalio.cloud.msg.event;

import com.dalio.cloud.model.vo.BaseEventVO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author admin
 * @version v1.0
 * @date 2022/7/29 10:08 PM
 * @create [2022/7/29 10:08 PM ] [admin] [初始创建]
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class MsgEventVO extends BaseEventVO {
    Long msgId;
}
