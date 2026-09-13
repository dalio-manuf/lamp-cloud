package com.dalio.cloud.common.cache.common;


import com.dalio.basic.model.cache.CacheKeyBuilder;
import com.dalio.cloud.common.cache.CacheKeyTable;

/**
 * 参数 KEY
 * {tenant}:LOGIN_LOG_BROWSER -> long
 * <p>
 * #c_login_log
 *
 * @author admin
 * @date 2020/9/20 6:45 下午
 */
public class LoginLogBrowserCacheKeyBuilder implements CacheKeyBuilder {
    @Override
    public String getTable() {
        return CacheKeyTable.LOGIN_LOG_BROWSER;
    }

}
