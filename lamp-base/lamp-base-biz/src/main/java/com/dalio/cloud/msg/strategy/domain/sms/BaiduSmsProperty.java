package com.dalio.cloud.msg.strategy.domain.sms;

import cn.hutool.core.util.StrUtil;
import com.dalio.basic.utils.ArgumentAssert;
import com.dalio.cloud.msg.strategy.domain.BaseProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author admin
 * @date 2022/7/10 0010 18:56
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BaiduSmsProperty extends BaseProperty {
    private static final String DEF_END_POINT = "http://smsv3.bj.baidubce.com";
    /**
     * accessKeyId
     */
    private String accessKeyId;
    /**
     * secretKey
     */
    private String secretKey;
    /**
     * 域名
     */
    private String endPoint;

    @Override
    public boolean initAndValid() {
        super.initAndValid();
        if (StrUtil.isEmpty(endPoint)) {
            endPoint = DEF_END_POINT;
        }
        ArgumentAssert.notEmpty(accessKeyId, "accessKeyId 不能为空");
        ArgumentAssert.notEmpty(secretKey, "secretKey 不能为空");
        return true;
    }
}
