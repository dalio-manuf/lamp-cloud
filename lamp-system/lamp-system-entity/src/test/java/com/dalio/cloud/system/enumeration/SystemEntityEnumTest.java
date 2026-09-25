package com.dalio.cloud.system.enumeration;

import com.dalio.cloud.msg.enumeration.InterfaceExecModeEnum;
import com.dalio.cloud.system.enumeration.system.ClientTypeEnum;
import com.dalio.cloud.system.enumeration.system.LoginStatusEnum;
import com.dalio.cloud.system.enumeration.tenant.ApplicationGrantTypeEnum;
import com.dalio.cloud.system.enumeration.tenant.ResourceOpenWithEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * lamp-system-entity 枚举类型单元测试
 */
class SystemEntityEnumTest {

    @Test
    @DisplayName("测试 InterfaceExecModeEnum")
    void testInterfaceExecModeEnum() {
        for (InterfaceExecModeEnum mode : InterfaceExecModeEnum.values()) {
            assertNotNull(mode.getCode());
            assertNotNull(mode.getValue());
            assertNotNull(mode.getDesc());
            assertEquals(mode, InterfaceExecModeEnum.valueOf(mode.name()));
            assertEquals(mode, InterfaceExecModeEnum.get(mode.name()));
            assertEquals(mode, InterfaceExecModeEnum.match(mode.name(), null));
        }
        assertNull(InterfaceExecModeEnum.get("UNKNOWN"));
        assertEquals(InterfaceExecModeEnum.IMPL_CLASS, InterfaceExecModeEnum.match("UNKNOWN", InterfaceExecModeEnum.IMPL_CLASS));

        InterfaceExecModeEnum mode = InterfaceExecModeEnum.IMPL_CLASS;
        assertFalse(mode.eq((InterfaceExecModeEnum) null));
        assertFalse(mode.eq(mode));
        assertTrue(mode.eq(mode.getCode()));
    }

    @Test
    @DisplayName("测试 ClientTypeEnum")
    void testClientTypeEnum() {
        for (ClientTypeEnum item : ClientTypeEnum.values()) {
            assertNotNull(item.getCode());
            assertNotNull(item.getDesc());
            assertEquals(item, ClientTypeEnum.valueOf(item.name()));
            assertEquals(item, ClientTypeEnum.get(item.name()));
            assertEquals(item, ClientTypeEnum.match(item.name(), null));
            assertTrue(item.eq(item));
        }
        assertNull(ClientTypeEnum.get("UNKNOWN"));
        assertEquals(ClientTypeEnum.LAMP_WEB, ClientTypeEnum.match("UNKNOWN", ClientTypeEnum.LAMP_WEB));
        assertFalse(ClientTypeEnum.LAMP_WEB.eq((ClientTypeEnum) null));
    }

    @Test
    @DisplayName("测试 LoginStatusEnum")
    void testLoginStatusEnum() {
        for (LoginStatusEnum item : LoginStatusEnum.values()) {
            assertNotNull(item.getCode());
            assertNotNull(item.getDesc());
            assertNotNull(item.getExtra());
            assertEquals(item, LoginStatusEnum.valueOf(item.name()));
            assertEquals(item, LoginStatusEnum.get(item.name()));
            assertEquals(item, LoginStatusEnum.get(item.getCode()));
            assertEquals(item, LoginStatusEnum.match(item.name(), null));
            assertTrue(item.eq(item));
        }
        assertNull(LoginStatusEnum.get("UNKNOWN"));
        assertEquals(LoginStatusEnum.SUCCESS, LoginStatusEnum.match("UNKNOWN", LoginStatusEnum.SUCCESS));
        assertFalse(LoginStatusEnum.SUCCESS.eq((LoginStatusEnum) null));
    }

    @Test
    @DisplayName("测试 ApplicationGrantTypeEnum")
    void testApplicationGrantTypeEnum() {
        for (ApplicationGrantTypeEnum item : ApplicationGrantTypeEnum.values()) {
            assertNotNull(item.getCode());
            assertNotNull(item.getDesc());
            assertEquals(item, ApplicationGrantTypeEnum.valueOf(item.name()));
        }
    }

    @Test
    @DisplayName("测试 ResourceOpenWithEnum")
    void testResourceOpenWithEnum() {
        for (ResourceOpenWithEnum item : ResourceOpenWithEnum.values()) {
            assertNotNull(item.getCode());
            assertNotNull(item.getDesc());
            assertEquals(item, ResourceOpenWithEnum.valueOf(item.name()));
        }
        ResourceOpenWithEnum item = ResourceOpenWithEnum.INNER_COMPONENT;
        item.setDesc("自定义描述");
        assertEquals("自定义描述", item.getDesc());
    }
}
