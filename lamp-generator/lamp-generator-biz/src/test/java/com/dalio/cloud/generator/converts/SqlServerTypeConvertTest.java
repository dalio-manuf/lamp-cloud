package com.dalio.cloud.generator.converts;

import com.baomidou.mybatisplus.annotation.DbType;
import com.dalio.cloud.generator.config.DateType;
import com.dalio.cloud.generator.rules.ColumnType;
import com.dalio.cloud.generator.rules.DbColumnType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SqlServerTypeConvert 单元测试 (验证 SQL Server 类型到 Java 类型的映射正确性)
 */
class SqlServerTypeConvertTest {

    private final SqlServerTypeConvert converter = SqlServerTypeConvert.INSTANCE;

    @Test
    @DisplayName("字符串类型 char/varchar/xml/text -> String")
    void testStringTypes() {
        assertEquals(DbColumnType.STRING, convert("char"));
        assertEquals(DbColumnType.STRING, convert("varchar"));
        assertEquals(DbColumnType.STRING, convert("nvarchar"));
        assertEquals(DbColumnType.STRING, convert("xml"));
        assertEquals(DbColumnType.STRING, convert("text"));
        assertEquals(DbColumnType.STRING, convert("ntext"));
    }

    @Test
    @DisplayName("整数类型 int/bigint -> 对应 Java 类型")
    void testIntegerTypes() {
        assertEquals(DbColumnType.INTEGER, convert("int"));
        assertEquals(DbColumnType.INTEGER, convert("smallint"));
        assertEquals(DbColumnType.LONG, convert("bigint"));
    }

    @Test
    @DisplayName("bool 类型 bit -> Boolean")
    void testBitType() {
        assertEquals(DbColumnType.BOOLEAN, convert("bit"));
    }

    @Test
    @DisplayName("精确数值 decimal/numeric -> Double")
    void testDecimalTypes() {
        assertEquals(DbColumnType.DOUBLE, convert("decimal"));
        assertEquals(DbColumnType.DOUBLE, convert("numeric"));
    }

    @Test
    @DisplayName("货币类型 money/smallmoney -> BigDecimal")
    void testMoneyTypes() {
        assertEquals(DbColumnType.BIG_DECIMAL, convert("money"));
        assertEquals(DbColumnType.BIG_DECIMAL, convert("smallmoney"));
    }

    @Test
    @DisplayName("浮点类型 float/real -> Float")
    void testFloatTypes() {
        assertEquals(DbColumnType.FLOAT, convert("float"));
        assertEquals(DbColumnType.FLOAT, convert("real"));
    }

    @Test
    @DisplayName("二进制类型 binary/varbinary/image -> byte[]")
    void testBinaryTypes() {
        assertEquals(DbColumnType.BYTE_ARRAY, convert("binary"));
        assertEquals(DbColumnType.BYTE_ARRAY, convert("varbinary"));
        assertEquals(DbColumnType.BYTE_ARRAY, convert("image"));
    }

    @Test
    @DisplayName("时间类型 TIME_PACK 模式 -> LocalDate/LocalTime/LocalDateTime")
    void testDateTypeTimePack() {
        assertEquals(DbColumnType.LOCAL_DATE, SqlServerTypeConvert.toDateType(DateType.TIME_PACK, "date"));
        assertEquals(DbColumnType.LOCAL_TIME, SqlServerTypeConvert.toDateType(DateType.TIME_PACK, "time"));
        assertEquals(DbColumnType.LOCAL_DATE_TIME, SqlServerTypeConvert.toDateType(DateType.TIME_PACK, "datetime"));
    }

    @Test
    @DisplayName("时间类型 SQL_PACK 模式 -> java.sql.*")
    void testDateTypeSqlPack() {
        assertEquals(DbColumnType.DATE_SQL, SqlServerTypeConvert.toDateType(DateType.SQL_PACK, "date"));
        assertEquals(DbColumnType.TIME, SqlServerTypeConvert.toDateType(DateType.SQL_PACK, "time"));
        assertEquals(DbColumnType.TIMESTAMP, SqlServerTypeConvert.toDateType(DateType.SQL_PACK, "datetime"));
    }

    @Test
    @DisplayName("时间类型 ONLY_DATE 模式 -> java.util.Date")
    void testDateTypeOnlyDate() {
        assertEquals(DbColumnType.DATE, SqlServerTypeConvert.toDateType(DateType.ONLY_DATE, "date"));
        assertEquals(DbColumnType.DATE, SqlServerTypeConvert.toDateType(DateType.ONLY_DATE, "time"));
    }

    @Test
    @DisplayName("未知类型 -> String 兜底")
    void testUnknownType() {
        assertEquals(DbColumnType.STRING, convert("unknown_type"));
        assertEquals(DbColumnType.STRING, converter.processTypeConvert(DateType.TIME_PACK, null, null, null));
    }

    @Test
    @DisplayName("TypeConverts.getTypeConvert 分发测试")
    void testGetTypeConvert() {
        assertInstanceOf(OracleTypeConvert.class, TypeConverts.getTypeConvert(DbType.ORACLE));
        assertInstanceOf(SqlServerTypeConvert.class, TypeConverts.getTypeConvert(DbType.SQL_SERVER));
        assertInstanceOf(MySqlTypeConvert.class, TypeConverts.getTypeConvert(DbType.MYSQL));
        assertInstanceOf(MySqlTypeConvert.class, TypeConverts.getTypeConvert(DbType.MARIADB));
        assertInstanceOf(MySqlTypeConvert.class, TypeConverts.getTypeConvert(DbType.H2));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private ColumnType convert(String dbType) {
        return converter.processTypeConvert(DateType.TIME_PACK, dbType, null, null);
    }
}
