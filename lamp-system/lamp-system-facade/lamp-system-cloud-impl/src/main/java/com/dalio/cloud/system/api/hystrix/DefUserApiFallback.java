package com.dalio.cloud.system.api.hystrix;

import com.dalio.basic.base.R;
import com.dalio.cloud.model.entity.system.SysUser;
import com.dalio.cloud.model.vo.result.UserQuery;
import com.dalio.cloud.system.api.DefUserApi;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户API熔断
 *
 * @author admin
 * @date 2019/07/23
 */
@Component
public class DefUserApiFallback implements DefUserApi {
    @Override
    public R<List<Long>> findAllUserId() {
        return R.timeout();
    }

    @Override
    public Map<Serializable, Object> findByIds(Set<Serializable> ids) {
        return Map.of();
    }

    @Override
    public R<SysUser> getById(UserQuery userQuery) {
        return R.timeout();
    }
}
