package com.dalio.cloud.generator.converts;

import com.dalio.cloud.generator.config.DateType;
import com.dalio.cloud.generator.rules.ColumnType;
import com.dalio.cloud.generator.rules.DbColumnType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OracleTypeConvert 单元测试 (验证 Oracle 类型到 Java 类型的映射正确性)
 */
class OracleTypeConvertTest {

    private final OracleTypeConvert converter = OracleTypeConvert.INSTANCE;

    @Test
    @DisplayName("字符串类型 char/clob -> String")
    void testStringTypes() {
        assertEquals(DbColumnType.STRING, convert("char", null, null));
        assertEquals(DbColumnType.STRING, convert("varchar2", null, null));
        assertEquals(DbColumnType.STRING, convert("clob", null, null));
        assertEquals(DbColumnType.STRING, convert("nchar", null, null));
    }

    @Test
    @DisplayName("number 类型 size=null -> Long")
    void testNumberSizeNull() {
        assertEquals(DbColumnType.LONG, convert("number", null, null));
    }

    @Test
    @DisplayName("number 类型 size=0, 2-10 -> Integer")
    void testNumberSizeInteger() {
        assertEquals(DbColumnType.INTEGER, convert("number", 0L, null));
        assertEquals(DbColumnType.INTEGER, convert("number", 2L, null));
        assertEquals(DbColumnType.INTEGER, convert("number", 10L, null));
    }

    @Test
    @DisplayName("number 类型 size=1 -> Boolean")
    void testNumberSizeBoolean() {
        assertEquals(DbColumnType.BOOLEAN, convert("number", 1L, null));
    }

    @Test
    @DisplayName("number 类型 size=11-19 -> Long")
    void testNumberSizeLong() {
        assertEquals(DbColumnType.LONG, convert("number", 11L, null));
        assertEquals(DbColumnType.LONG, convert("number", 19L, null));
    }

    @Test
    @DisplayName("number 类型 size>19 或 digit>0 -> BigDecimal")
    void testNumberSizeBigDecimal() {
        assertEquals(DbColumnType.BIG_DECIMAL, convert("number", 20L, null));
        assertEquals(DbColumnType.BIG_DECIMAL, convert("number", 5L, 2));
    }

    @Test
    @DisplayName("时间类型 date/timestamp -> 各时间类型")
    void testDateTypes() {
        assertEquals(DbColumnType.DATE, OracleTypeConvert.toDateType(DateType.ONLY_DATE, "date"));
        assertEquals(DbColumnType.TIMESTAMP, OracleTypeConvert.toDateType(DateType.SQL_PACK, "date"));
        assertEquals(DbColumnType.LOCAL_DATE_TIME, OracleTypeConvert.toDateType(DateType.TIME_PACK, "date"));
        assertEquals(DbColumnType.LOCAL_DATE_TIME, OracleTypeConvert.toDateType(DateType.TIME_PACK, "timestamp"));
    }

    @Test
    @DisplayName("二进制类型 blob/binary/raw -> 对应 Java 类型")
    void testBinaryTypes() {
        assertEquals(DbColumnType.BLOB, convert("blob", null, null));
        assertEquals(DbColumnType.BYTE_ARRAY, convert("binary", null, null));
        assertEquals(DbColumnType.BYTE_ARRAY, convert("raw", null, null));
    }

    @Test
    @DisplayName("float -> Float")
    void testFloatType() {
        assertEquals(DbColumnType.FLOAT, convert("float", null, null));
    }

    @Test
    @DisplayName("未知类型 -> String 兜底")
    void testUnknownType() {
        assertEquals(DbColumnType.STRING, convert("unknown_type", null, null));
        assertEquals(DbColumnType.STRING, converter.processTypeConvert(DateType.TIME_PACK, null, null, null));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private ColumnType convert(String dbType, Long size, Integer digit) {
        return converter.processTypeConvert(DateType.TIME_PACK, dbType, size, digit);
    }
}
