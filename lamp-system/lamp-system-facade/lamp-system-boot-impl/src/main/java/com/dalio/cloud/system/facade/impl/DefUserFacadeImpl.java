package com.dalio.cloud.system.facade.impl;

import com.dalio.basic.base.R;
import com.dalio.cloud.model.constant.EchoApi;
import com.dalio.cloud.system.facade.DefUserFacade;
import com.dalio.cloud.system.service.tenant.DefUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author admin
 * @since 2024/9/20 23:33
 */
@Service(EchoApi.DEF_USER_ID_CLASS)
@RequiredArgsConstructor
public class DefUserFacadeImpl implements DefUserFacade {
    private final DefUserService defUserService;

    @Override
    public R<List<Long>> findAllUserId() {
        return R.success(defUserService.findUserIdList(null));
    }

    @Override
    public Map<Serializable, Object> findByIds(Set<Serializable> ids) {
        return defUserService.findByIds(ids);
    }

}
