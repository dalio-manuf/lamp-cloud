package com.dalio.cloud.model.vo;

import cn.hutool.core.collection.CollUtil;
import lombok.Data;
import lombok.experimental.Accessors;
import com.dalio.basic.context.ContextUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author admin
 * @version v1.0
 * @date 2022/7/29 10:04 PM
 * @create [2022/7/29 10:04 PM ] [admin] [初始创建]
 */
@Data
@Accessors(chain = true)
public class BaseEventVO {
    private Map<String, String> map;

    /**
     * 将线程变量副 暂存到map
     * 适用于：异步调用前
     *
     * @return com.dalio.cloud.model.vo.BaseEventVO
     * @author admin
     * @date 2022/7/29 11:12 PM
     * @create [2022/7/29 11:12 PM ] [admin] [初始创建]
     */
    public BaseEventVO copy() {
        if (map == null) {
            map = new HashMap<>();
        }
        map.clear();
        Map<String, String> localMap = ContextUtil.getLocalMap();
        if (localMap != null) {
            map.putAll(localMap);
        }
        return this;
    }

    /**
     * 将map写入线程变量
     * 适用于：异步执行一开始
     *
     * @return com.dalio.cloud.model.vo.BaseEventVO
     * @author admin
     * @date 2022/7/29 11:12 PM
     * @create [2022/7/29 11:12 PM ] [admin] [初始创建]
     */
    public BaseEventVO write() {
        if (CollUtil.isNotEmpty(map)) {
            ContextUtil.setLocalMap(map);
        }
        return this;
    }

}
