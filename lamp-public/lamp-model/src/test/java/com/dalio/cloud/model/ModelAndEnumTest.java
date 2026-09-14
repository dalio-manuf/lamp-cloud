package com.dalio.cloud.model;

import com.dalio.cloud.model.enumeration.*;
import com.dalio.cloud.model.enumeration.base.*;
import com.dalio.cloud.model.enumeration.system.*;
import com.dalio.cloud.model.vo.result.Option;
import com.dalio.cloud.model.vo.result.ResourceApiVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * lamp-model 模块枚举与通用模型测试
 */
class ModelAndEnumTest {

    @Test
    @DisplayName("测试常用枚举匹配与方法")
    void testEnums() {
        assertTrue(BooleanEnum.TRUE.eq(true));
        assertTrue(BooleanEnum.TRUE.eq(1));
        assertTrue(BooleanEnum.TRUE.eq("1"));
        assertFalse(BooleanEnum.TRUE.eq(false));
        assertEquals("true", BooleanEnum.TRUE.getCode());

        assertEquals(HttpMethod.GET, HttpMethod.get("GET"));
        assertEquals(HttpMethod.POST, HttpMethod.get("POST"));
        assertEquals(HttpMethod.ALL, HttpMethod.get("ALL"));

        assertEquals(OrgTypeEnum.COMPANY, OrgTypeEnum.get("10"));
        assertEquals(OrgTypeEnum.COMPANY, OrgTypeEnum.get("COMPANY"));
        assertEquals(OrgTypeEnum.DEPT, OrgTypeEnum.get("20"));
        assertTrue(OrgTypeEnum.COMPANY.eq(OrgTypeEnum.COMPANY));

        assertEquals(Sex.M, Sex.get("1"));
        assertEquals(Sex.W, Sex.get("2"));

        assertEquals(StateEnum.ENABLE, StateEnum.match("1"));
        assertEquals(StateEnum.DISABLE, StateEnum.match("0"));
        assertTrue(StateEnum.ENABLE.eq(true));
        assertTrue(StateEnum.ENABLE.eq(1));
        assertTrue(StateEnum.ENABLE.eq("1"));

        assertEquals(ActiveStatusEnum.ACTIVATED, ActiveStatusEnum.get("20"));
        assertEquals(UserStatusEnum.NORMAL, UserStatusEnum.get("0"));
        assertEquals(RoleCategoryEnum.FUNCTION, RoleCategoryEnum.get("10"));

        assertEquals(ResourceTypeEnum.MENU, ResourceTypeEnum.get("20"));
        assertEquals(DictClassifyEnum.SYSTEM, DictClassifyEnum.get("10"));
        assertEquals(DefTenantStatusEnum.NORMAL, DefTenantStatusEnum.get("NORMAL"));
        assertEquals(TenantConnectTypeEnum.SYSTEM, TenantConnectTypeEnum.get("SYSTEM"));
    }

    @Test
    @DisplayName("测试 VO 与模型对象读写")
    void testVos() {
        Option option = new Option();
        option.setLabel("label");
        option.setValue("value");
        assertEquals("label", option.getLabel());
        assertEquals("value", option.getValue());

        ResourceApiVO api = new ResourceApiVO();
        api.setUri("/api/test");
        api.setRequestMethod("GET");
        assertEquals("/api/test", api.getUri());
        assertEquals("GET", api.getRequestMethod());
    }
}
