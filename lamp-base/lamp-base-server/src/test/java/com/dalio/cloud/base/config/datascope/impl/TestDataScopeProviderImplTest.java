package com.dalio.cloud.base.config.datascope.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.dalio.cloud.datascope.model.DataFieldProperty;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * TestDataScopeProviderImpl 单元测试
 *
 * @author went
 */
class TestDataScopeProviderImplTest {

    @Test
    @DisplayName("测试 findDataFieldProperty 逻辑")
    void testFindDataFieldProperty() {
        TestDataScopeProviderImpl provider = new TestDataScopeProviderImpl();
        List<DataFieldProperty> list = new ArrayList<>();
        DataFieldProperty prop = new DataFieldProperty();
        list.add(prop);

        List<DataFieldProperty> result = provider.findDataFieldProperty(list);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("biz_id", result.get(0).getField());
        assertEquals(2, result.get(0).getValues().size());
    }
}
