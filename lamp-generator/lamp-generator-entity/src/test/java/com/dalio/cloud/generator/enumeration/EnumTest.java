package com.dalio.cloud.generator.enumeration;

import com.dalio.cloud.test.enumeration.DefGenTestSimpleType2Enum;
import com.dalio.cloud.test.enumeration.DefGenTestTreeType2Enum;
import com.dalio.cloud.test.enumeration.ProductType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnumTest {

    @Test
    void testComponentEnum() {
        for (ComponentEnum e : ComponentEnum.values()) {
            assertNotNull(e.getValue());
            assertNotNull(e.getDesc());
            assertTrue(e.eq(e));
        }
        assertNotNull(ComponentEnum.valueOf(ComponentEnum.values()[0].name()));
        assertNotNull(ComponentEnum.match(ComponentEnum.values()[0].name(), null));
        assertNull(ComponentEnum.match("NOT_EXIST", null));
        assertNotNull(ComponentEnum.get(ComponentEnum.values()[0].name()));
        assertNull(ComponentEnum.get("NOT_EXIST"));
    }

    @Test
    void testEntitySuperClassEnum() {
        for (EntitySuperClassEnum e : EntitySuperClassEnum.values()) {
            assertNotNull(e.getCode());
            assertNotNull(e.getDesc());
            assertTrue(e.eq(e));
        }
        assertNotNull(EntitySuperClassEnum.valueOf(EntitySuperClassEnum.values()[0].name()));
    }

    @Test
    void testFileOverrideStrategyEnum() {
        for (FileOverrideStrategyEnum e : FileOverrideStrategyEnum.values()) {
            assertNotNull(e.getCode());
            assertNotNull(e.getDesc());
            assertTrue(e.eq(e));
        }
        assertNotNull(FileOverrideStrategyEnum.valueOf(FileOverrideStrategyEnum.values()[0].name()));
    }

    @Test
    void testGenTypeEnum() {
        for (GenTypeEnum e : GenTypeEnum.values()) {
            assertNotNull(e.getCode());
            assertNotNull(e.getDesc());
            assertTrue(e.eq(e));
        }
        assertNotNull(GenTypeEnum.valueOf(GenTypeEnum.values()[0].name()));
        assertNotNull(GenTypeEnum.match(GenTypeEnum.values()[0].name(), null));
        assertNull(GenTypeEnum.match("NOT_EXIST", null));
        assertNotNull(GenTypeEnum.get(GenTypeEnum.values()[0].name()));
        assertNull(GenTypeEnum.get("NOT_EXIST"));
    }

    @Test
    void testPopupTypeEnum() {
        for (PopupTypeEnum e : PopupTypeEnum.values()) {
            assertNotNull(e.getCode());
            assertNotNull(e.getDesc());
            assertNotNull(e.getValue());
            assertTrue(e.eq(e));
        }
        assertNotNull(PopupTypeEnum.valueOf(PopupTypeEnum.values()[0].name()));
        assertNotNull(PopupTypeEnum.match(PopupTypeEnum.values()[0].name(), null));
        assertNull(PopupTypeEnum.match("NOT_EXIST", null));
        assertNotNull(PopupTypeEnum.get(PopupTypeEnum.values()[0].name()));
        assertNull(PopupTypeEnum.get("NOT_EXIST"));
    }

    @Test
    void testProjectTypeEnum() {
        for (ProjectTypeEnum e : ProjectTypeEnum.values()) {
            assertNotNull(e.getCode());
            assertNotNull(e.getDesc());
            assertTrue(e.eq(e));
        }
        assertNotNull(ProjectTypeEnum.valueOf(ProjectTypeEnum.values()[0].name()));
    }

    @Test
    void testSoyComponentEnum() {
        for (SoyComponentEnum e : SoyComponentEnum.values()) {
            assertNotNull(e.getCode());
            assertNotNull(e.getDesc());
            assertTrue(e.eq(e));
        }
        assertNotNull(SoyComponentEnum.valueOf(SoyComponentEnum.values()[0].name()));
        assertNotNull(SoyComponentEnum.match(SoyComponentEnum.values()[0].name(), null));
        assertNull(SoyComponentEnum.match("NOT_EXIST", null));
        assertNotNull(SoyComponentEnum.get(SoyComponentEnum.values()[0].name()));
        assertNull(SoyComponentEnum.get("NOT_EXIST"));
    }

    @Test
    void testSqlConditionEnum() {
        for (SqlConditionEnum e : SqlConditionEnum.values()) {
            assertNotNull(e.getCode());
            assertNotNull(e.getDesc());
            assertNotNull(e.getValue());
            assertTrue(e.eq(e));
        }
        assertNotNull(SqlConditionEnum.valueOf(SqlConditionEnum.values()[0].name()));
        assertNotNull(SqlConditionEnum.match(SqlConditionEnum.values()[0].name(), null));
        assertNull(SqlConditionEnum.match("NOT_EXIST", null));
        assertNotNull(SqlConditionEnum.get(SqlConditionEnum.values()[0].name()));
        assertNull(SqlConditionEnum.get("NOT_EXIST"));
    }

    @Test
    void testSuperClassEnum() {
        for (SuperClassEnum e : SuperClassEnum.values()) {
            assertNotNull(e.getCode());
            assertNotNull(e.getDesc());
            assertTrue(e.eq(e));
        }
        assertNotNull(SuperClassEnum.valueOf(SuperClassEnum.values()[0].name()));
    }

    @Test
    void testTemplateEnum() {
        for (TemplateEnum e : TemplateEnum.values()) {
            assertNotNull(e.getCode());
            assertNotNull(e.getDesc());
            assertNotNull(e.getValue());
            assertTrue(e.eq(e));
        }
        assertNotNull(TemplateEnum.valueOf(TemplateEnum.values()[0].name()));
        assertNotNull(TemplateEnum.match(TemplateEnum.values()[0].name(), null));
        assertNull(TemplateEnum.match("NOT_EXIST", null));
        assertNotNull(TemplateEnum.get(TemplateEnum.values()[0].name()));
        assertNull(TemplateEnum.get("NOT_EXIST"));
    }

    @Test
    void testTplEnum() {
        for (TplEnum e : TplEnum.values()) {
            assertNotNull(e.getCode());
            assertNotNull(e.getDesc());
            assertNotNull(e.getValue());
            assertTrue(e.eq(e));
        }
        assertNotNull(TplEnum.valueOf(TplEnum.values()[0].name()));
        assertNotNull(TplEnum.match(TplEnum.values()[0].name(), null));
        assertNull(TplEnum.match("NOT_EXIST", null));
        assertNotNull(TplEnum.get(TplEnum.values()[0].name()));
        assertNull(TplEnum.get("NOT_EXIST"));
    }

    @Test
    void testVxeComponentEnum() {
        for (VxeComponentEnum e : VxeComponentEnum.values()) {
            assertNotNull(e.getCode());
            assertNotNull(e.getDesc());
            assertNotNull(e.getValue());
            assertTrue(e.eq(e));
        }
        assertNotNull(VxeComponentEnum.valueOf(VxeComponentEnum.values()[0].name()));
        assertNotNull(VxeComponentEnum.match(VxeComponentEnum.values()[0].name(), null));
        assertNull(VxeComponentEnum.match("NOT_EXIST", null));
        assertNotNull(VxeComponentEnum.get(VxeComponentEnum.values()[0].name()));
        assertNull(VxeComponentEnum.get("NOT_EXIST"));
    }

    @Test
    void testDefGenTestSimpleType2Enum() {
        for (DefGenTestSimpleType2Enum e : DefGenTestSimpleType2Enum.values()) {
            assertNotNull(e.getCode());
            assertNotNull(e.getDesc());
            assertNotNull(e.getValue());
            assertTrue(e.eq(e));
        }
        assertNotNull(DefGenTestSimpleType2Enum.valueOf(DefGenTestSimpleType2Enum.ORDINARY.name()));
        assertEquals(DefGenTestSimpleType2Enum.ORDINARY, DefGenTestSimpleType2Enum.match("ORDINARY", null));
        assertEquals(DefGenTestSimpleType2Enum.GIFT, DefGenTestSimpleType2Enum.get("GIFT"));
        assertNull(DefGenTestSimpleType2Enum.match("NOT_EXIST", null));
        assertNull(DefGenTestSimpleType2Enum.get("NOT_EXIST"));
    }

    @Test
    void testDefGenTestTreeType2Enum() {
        for (DefGenTestTreeType2Enum e : DefGenTestTreeType2Enum.values()) {
            assertNotNull(e.getCode());
            assertNotNull(e.getDesc());
            assertNotNull(e.getValue());
            assertTrue(e.eq(e));
        }
        assertNotNull(DefGenTestTreeType2Enum.valueOf(DefGenTestTreeType2Enum.ORDINARY.name()));
        assertEquals(DefGenTestTreeType2Enum.ORDINARY, DefGenTestTreeType2Enum.match("ORDINARY", null));
        assertEquals(DefGenTestTreeType2Enum.GIFT, DefGenTestTreeType2Enum.get("GIFT"));
        assertNull(DefGenTestTreeType2Enum.match("NOT_EXIST", null));
        assertNull(DefGenTestTreeType2Enum.get("NOT_EXIST"));
    }

    @Test
    void testProductType() {
        for (ProductType e : ProductType.values()) {
            assertNotNull(e.getCode());
            assertNotNull(e.getDesc());
            assertNotNull(e.getValue());
            assertTrue(e.eq(e));
        }
        assertNotNull(ProductType.valueOf(ProductType.ORDINARY.name()));
        assertEquals(ProductType.ORDINARY, ProductType.match("ORDINARY", null));
        assertEquals(ProductType.GIFT, ProductType.get("GIFT"));
        assertNull(ProductType.match("NOT_EXIST", null));
        assertNull(ProductType.get("NOT_EXIST"));
    }
}
