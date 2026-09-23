package com.dalio.cloud.datascope.utils;

import com.dalio.cloud.common.annotation.DataField;
import com.dalio.cloud.common.annotation.DataScope;
import com.dalio.cloud.datascope.model.DataFieldProperty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ScopeUtils 单元测试
 */
class ScopeUtilsTest {

    @Test
    @DisplayName("测试 buildDataFieldProperty 处理空与非空")
    void testBuildDataFieldProperty() {
        assertTrue(ScopeUtils.buildDataFieldProperty(null).isEmpty());
        assertTrue(ScopeUtils.buildDataFieldProperty(new DataField[0]).isEmpty());
    }

    @Test
    @DisplayName("测试 buildDataScopeProperty 各种路径与缓存")
    void testBuildDataScopeProperty() {
        // null 与无包名
        assertTrue(ScopeUtils.buildDataScopeProperty(null).isEmpty());
        assertTrue(ScopeUtils.buildDataScopeProperty("NoPackageMapper").isEmpty());

        // 类不存在
        assertTrue(ScopeUtils.buildDataScopeProperty("com.dalio.notexist.FakeMapper.method").isEmpty());

        // 真实存在的方法
        String msIdWithScope = MockMapper.class.getName() + ".selectWithScope";
        List<DataFieldProperty> props = ScopeUtils.buildDataScopeProperty(msIdWithScope);
        assertNotNull(props);
        assertEquals(2, props.size());
        assertEquals("u", props.get(0).getAlias());
        assertEquals("o", props.get(1).getAlias());

        // 缓存命中
        List<DataFieldProperty> cachedProps = ScopeUtils.buildDataScopeProperty(msIdWithScope);
        assertSame(props, cachedProps);

        // 忽略的接口
        String msIdIgnored = MockMapper.class.getName() + ".selectIgnored";
        List<DataFieldProperty> ignoredProps = ScopeUtils.buildDataScopeProperty(msIdIgnored);
        assertTrue(ignoredProps.isEmpty());

        // 未配置注解的接口
        String msIdNone = MockMapper.class.getName() + ".selectWithoutScope";
        List<DataFieldProperty> noneProps = ScopeUtils.buildDataScopeProperty(msIdNone);
        assertTrue(noneProps.isEmpty());
    }

    interface MockMapper {
        @DataScope({
                @DataField(alias = "u"),
                @DataField(alias = "o")
        })
        void selectWithScope();

        @DataScope(ignore = true)
        void selectIgnored();

        void selectWithoutScope();
    }
}
