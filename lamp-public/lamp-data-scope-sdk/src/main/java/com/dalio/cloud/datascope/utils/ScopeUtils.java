package com.dalio.cloud.datascope.utils;

import com.dalio.basic.utils.StrPool;
import com.dalio.cloud.common.annotation.DataField;
import com.dalio.cloud.common.annotation.DataScope;
import com.dalio.cloud.datascope.model.DataFieldProperty;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author admin
 * @date 2022/1/9 22:49
 */
@Slf4j
public class ScopeUtils {
    private static final Map<String, List<DataFieldProperty>> DFP_MAP = new ConcurrentHashMap<>();

    private ScopeUtils() {
    }

    private static String getMapper(String msId) {
        if (msId == null) {
            return null;
        }
        int lastIndex = msId.lastIndexOf(".");
        return lastIndex > 0 ? msId.substring(0, lastIndex) : null;
    }

    public static List<DataFieldProperty> buildDataFieldProperty(DataField[] dfs) {
        if (dfs != null && dfs.length > 0) {
            return Arrays.stream(dfs).map(df -> new DataFieldProperty(df.alias())).toList();
        }
        return Collections.emptyList();
    }

    public static List<DataFieldProperty> buildDataScopeProperty(String msId) {
        if (msId == null) {
            return Collections.emptyList();
        }
        List<DataFieldProperty> dfpList = DFP_MAP.get(msId);
        if (dfpList != null) {
            return dfpList;
        }

        String mapperFullPath = getMapper(msId);
        if (mapperFullPath == null) {
            DFP_MAP.put(msId, Collections.emptyList());
            return Collections.emptyList();
        }

        try {
            Class<?> mapperClazz = Class.forName(mapperFullPath);
            Method[] methods = mapperClazz.getMethods();

            for (Method method : methods) {
                DataScope ds = method.getAnnotation(DataScope.class);
                if (ds != null && !ds.ignore()) {
                    List<DataFieldProperty> dfps = buildDataFieldProperty(ds.value());
                    DFP_MAP.put(mapperFullPath + StrPool.DOT + method.getName(), dfps);
                } else {
                    DFP_MAP.putIfAbsent(mapperFullPath + StrPool.DOT + method.getName(), Collections.emptyList());
                }
            }
        } catch (ClassNotFoundException e) {
            log.debug("未能加载Mapper类: {}", mapperFullPath);
        }
        return DFP_MAP.computeIfAbsent(msId, k -> Collections.emptyList());
    }
}
