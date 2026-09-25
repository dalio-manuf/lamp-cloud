package com.dalio.cloud.base;

import com.dalio.basic.base.R;
import com.dalio.cloud.base.config.datascope.impl.TestDataScopeProviderImpl;
import com.dalio.cloud.base.satoken.StpInterfaceImpl;
import com.dalio.cloud.datascope.model.DataFieldProperty;
import com.dalio.cloud.gateway.controller.GateController;
import com.dalio.cloud.model.vo.result.Option;
import com.dalio.cloud.oauth.biz.StpInterfaceBiz;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class GateControllerAndStpTest {

    @Test
    @DisplayName("测试 GateController 接口")
    void testGateController() {
        GateController controller = new GateController();
        ReflectionTestUtils.setField(controller, "application", "lamp-boot-server");

        R<Map<String, String>> prefixResult = controller.findOnlineServicePrefix();
        assertNotNull(prefixResult);
        assertEquals("base", prefixResult.getData().get("lamp-boot-server"));

        R<List<Option>> serviceResult = controller.findOnlineService();
        assertNotNull(serviceResult);
        assertEquals(4, serviceResult.getData().size());
        assertEquals("base", serviceResult.getData().get(0).getValue());
    }

    @Test
    @DisplayName("测试 StpInterfaceImpl 委托调用")
    void testStpInterfaceImpl() {
        StpInterfaceBiz mockBiz = mock(StpInterfaceBiz.class);
        StpInterfaceImpl stpInterface = new StpInterfaceImpl(mockBiz);

        List<String> mockPerms = Arrays.asList("sys:user:view", "sys:user:edit");
        when(mockBiz.getPermissionList()).thenReturn(mockPerms);
        assertEquals(mockPerms, stpInterface.getPermissionList(1L, "default"));
        verify(mockBiz).getPermissionList();

        List<String> mockRoles = Arrays.asList("ADMIN", "USER");
        when(mockBiz.getRoleList()).thenReturn(mockRoles);
        assertEquals(mockRoles, stpInterface.getRoleList(1L, "default"));
        verify(mockBiz).getRoleList();
    }

    @Test
    @DisplayName("测试 TestDataScopeProviderImpl 逻辑")
    void testDataScopeProvider() {
        TestDataScopeProviderImpl provider = new TestDataScopeProviderImpl();
        List<DataFieldProperty> properties = new ArrayList<>();
        DataFieldProperty prop = new DataFieldProperty();
        properties.add(prop);

        List<DataFieldProperty> result = provider.findDataFieldProperty(properties);
        assertNotNull(result);
        assertEquals("biz_id", prop.getField());
        assertEquals(List.of(1L, 2L), prop.getValues());
    }
}
