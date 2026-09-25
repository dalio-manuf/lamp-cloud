package com.dalio.cloud.base.satoken;

import cn.dev33.satoken.stp.StpInterface;
import com.dalio.cloud.oauth.biz.StpInterfaceBiz;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sa-Token 权限实现（单体版，委托至 StpInterfaceBiz）
 *
 * @author dalio
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {
    private final StpInterfaceBiz stpInterfaceBiz;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return stpInterfaceBiz.getPermissionList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return stpInterfaceBiz.getRoleList();
    }
}
