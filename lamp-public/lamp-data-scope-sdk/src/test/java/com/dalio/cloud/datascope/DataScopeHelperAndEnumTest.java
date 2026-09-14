package com.dalio.cloud.datascope;

import com.dalio.cloud.datascope.model.DataFieldProperty;
import com.dalio.cloud.datascope.model.DataScopeEnum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DataScopeHelper 与 DataScopeEnum 单元测试
 */
class DataScopeHelperAndEnumTest {

    @AfterEach
    void tearDown() {
        DataScopeHelper.clearDataScope();
    }

    @Test
    @DisplayName("测试 DataScopeHelper 本地线程变量设置与清理")
    void testDataScopeHelper() {
        assertNull(DataScopeHelper.getLocalDataScope());

        DataScopeHelper.startDataScope();
        assertNull(DataScopeHelper.getLocalDataScope());

        DataScopeHelper.startDataScope("t1", "t2");
        List<DataFieldProperty> localDataScope = DataScopeHelper.getLocalDataScope();
        assertNotNull(localDataScope);
        assertEquals(2, localDataScope.size());
        assertEquals("t1", localDataScope.get(0).getAlias());
        assertEquals("t2", localDataScope.get(1).getAlias());

        DataScopeHelper.clearDataScope();
        assertNull(DataScopeHelper.getLocalDataScope());
    }

    @Test
    @DisplayName("测试 DataScopeEnum 枚举匹配与方法")
    void testDataScopeEnum() {
        assertEquals(DataScopeEnum.ALL, DataScopeEnum.get("ALL"));
        assertEquals(DataScopeEnum.ALL, DataScopeEnum.get("01"));
        assertEquals(DataScopeEnum.SELF, DataScopeEnum.match("06", null));
        assertEquals(DataScopeEnum.CUSTOM, DataScopeEnum.match("CUSTOM", null));
        assertNull(DataScopeEnum.get(null));
        assertNull(DataScopeEnum.get("NON_EXISTENT"));
        assertEquals(DataScopeEnum.ALL, DataScopeEnum.match("NON_EXISTENT", DataScopeEnum.ALL));

        assertTrue(DataScopeEnum.ALL.eq(DataScopeEnum.ALL));
        assertFalse(DataScopeEnum.ALL.eq(DataScopeEnum.SELF));
        assertEquals("01", DataScopeEnum.ALL.getCode());
        assertEquals("01", DataScopeEnum.ALL.getVal());
        assertEquals("全部", DataScopeEnum.ALL.getDesc());
    }
}
