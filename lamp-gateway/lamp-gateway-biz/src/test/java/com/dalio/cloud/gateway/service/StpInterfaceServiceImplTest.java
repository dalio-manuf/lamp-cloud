package com.dalio.cloud.gateway.service;

import com.dalio.cloud.oauth.biz.StpInterfaceBiz;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StpInterfaceServiceImplTest {

    @Mock
    private StpInterfaceBiz stpInterfaceBiz;

    @InjectMocks
    private StpInterfaceServiceImpl stpInterfaceService;

    @Test
    @DisplayName("测试获取权限列表")
    void testGetPermissionList() {
        List<String> mockPerms = Arrays.asList("sys:user:view", "sys:user:add");
        when(stpInterfaceBiz.getPermissionList()).thenReturn(mockPerms);

        List<String> result = stpInterfaceService.getPermissionList(1L, "default");
        assertEquals(mockPerms, result);
        verify(stpInterfaceBiz).getPermissionList();
    }

    @Test
    @DisplayName("测试获取角色列表")
    void testGetRoleList() {
        List<String> mockRoles = Arrays.asList("ADMIN", "MANAGER");
        when(stpInterfaceBiz.getRoleList()).thenReturn(mockRoles);

        List<String> result = stpInterfaceService.getRoleList(1L, "default");
        assertEquals(mockRoles, result);
        verify(stpInterfaceBiz).getRoleList();
    }
}
