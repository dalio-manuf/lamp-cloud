package com.dalio.cloud.gateway.service;

import cn.dev33.satoken.stp.StpInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.dalio.cloud.oauth.biz.StpInterfaceBiz;

import java.util.List;

/**
 * Sa-Token 权限网关实现（委托至 StpInterfaceBiz）
 *
 * @author dalio
 * @since 2024/8/6 21:46
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceServiceImpl implements StpInterface {
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

