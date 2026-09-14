package com.dalio.cloud.generator.converts;

import com.dalio.cloud.generator.config.DateType;
import com.dalio.cloud.generator.rules.ColumnType;
import com.dalio.cloud.generator.rules.DbColumnType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MySqlTypeConvert 单元测试 (验证 MySQL 类型到 Java 类型的映射正确性)
 */
class MySqlTypeConvertTest {

    private final MySqlTypeConvert converter = MySqlTypeConvert.INSTANCE;

    @Test
    @DisplayName("字符串类型 varchar/text/json -> String")
    void testStringTypes() {
        assertEquals(DbColumnType.STRING, convert("varchar"));
        assertEquals(DbColumnType.STRING, convert("text"));
        assertEquals(DbColumnType.STRING, convert("longtext"));
        assertEquals(DbColumnType.STRING, convert("json"));
        assertEquals(DbColumnType.STRING, convert("enum"));
        assertEquals(DbColumnType.STRING, convert("char"));
    }

    @Test
    @DisplayName("整数类型 int/bigint/tinyint -> 对应 Java 类型")
    void testIntegerTypes() {
        assertEquals(DbColumnType.INTEGER, convert("int"));
        assertEquals(DbColumnType.INTEGER, convert("int(11)"));
        assertEquals(DbColumnType.LONG, convert("bigint"));
        assertEquals(DbColumnType.LONG, convert("bigint(20)"));
        assertEquals(DbColumnType.BOOLEAN, convert("tinyint(1)"));
        assertEquals(DbColumnType.BOOLEAN, convert("bit(1)"));
        assertEquals(DbColumnType.BOOLEAN, convert("bit"));
    }

    @Test
    @DisplayName("小数类型 decimal/float/double -> 对应 Java 类型")
    void testDecimalTypes() {
        assertEquals(DbColumnType.BIG_DECIMAL, convert("decimal"));
        assertEquals(DbColumnType.FLOAT, convert("float"));
        assertEquals(DbColumnType.DOUBLE, convert("double"));
    }

    @Test
    @DisplayName("二进制类型 blob/clob/binary -> 对应 Java 类型")
    void testBinaryTypes() {
        assertEquals(DbColumnType.BLOB, convert("blob"));
        assertEquals(DbColumnType.CLOB, convert("clob"));
        assertEquals(DbColumnType.BYTE_ARRAY, convert("binary"));
        assertEquals(DbColumnType.BYTE_ARRAY, convert("varbinary"));
    }

    @Test
    @DisplayName("时间类型 TIME_PACK 模式 -> LocalDate/LocalTime/LocalDateTime")
    void testDateTypeTimePack() {
        assertEquals(DbColumnType.LOCAL_DATE, toDateType(DateType.TIME_PACK, "date"));
        assertEquals(DbColumnType.LOCAL_TIME, toDateType(DateType.TIME_PACK, "time"));
        assertEquals(DbColumnType.LOCAL_DATE_TIME, toDateType(DateType.TIME_PACK, "datetime"));
        assertEquals(DbColumnType.YEAR, toDateType(DateType.TIME_PACK, "year"));
    }

    @Test
    @DisplayName("时间类型 ONLY_DATE 模式 -> java.util.Date")
    void testDateTypeOnlyDate() {
        assertEquals(DbColumnType.DATE, toDateType(DateType.ONLY_DATE, "datetime"));
        assertEquals(DbColumnType.DATE, toDateType(DateType.ONLY_DATE, "date"));
    }

    @Test
    @DisplayName("时间类型 SQL_PACK 模式 -> java.sql.*")
    void testDateTypeSqlPack() {
        assertEquals(DbColumnType.DATE_SQL, toDateType(DateType.SQL_PACK, "date"));
        assertEquals(DbColumnType.TIME, toDateType(DateType.SQL_PACK, "time"));
        assertEquals(DbColumnType.TIMESTAMP, toDateType(DateType.SQL_PACK, "datetime"));
        assertEquals(DbColumnType.DATE_SQL, toDateType(DateType.SQL_PACK, "year"));
    }

    @Test
    @DisplayName("null 类型 -> String 兜底")
    void testNullType() {
        assertEquals(DbColumnType.STRING, converter.processTypeConvert(DateType.TIME_PACK, null, null, null));
        // toDateType null
        assertEquals(DbColumnType.STRING, MySqlTypeConvert.toDateType(DateType.TIME_PACK, null));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private ColumnType convert(String dbType) {
        return converter.processTypeConvert(DateType.TIME_PACK, dbType, null, null);
    }

    private ColumnType toDateType(DateType dt, String type) {
        return MySqlTypeConvert.toDateType(dt, type);
    }
}
