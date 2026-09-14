package com.dalio.cloud.generator.converts;

import com.dalio.cloud.generator.config.DateType;
import com.dalio.cloud.generator.rules.ColumnType;
import com.dalio.cloud.generator.rules.DbColumnType;

import java.util.regex.Pattern;

import static com.dalio.cloud.generator.converts.TypeConverts.contains;
import static com.dalio.cloud.generator.converts.TypeConverts.containsAny;
import static com.dalio.cloud.generator.rules.DbColumnType.BIG_DECIMAL;
import static com.dalio.cloud.generator.rules.DbColumnType.BLOB;
import static com.dalio.cloud.generator.rules.DbColumnType.BOOLEAN;
import static com.dalio.cloud.generator.rules.DbColumnType.BYTE_ARRAY;
import static com.dalio.cloud.generator.rules.DbColumnType.CLOB;
import static com.dalio.cloud.generator.rules.DbColumnType.DOUBLE;
import static com.dalio.cloud.generator.rules.DbColumnType.FLOAT;
import static com.dalio.cloud.generator.rules.DbColumnType.INTEGER;
import static com.dalio.cloud.generator.rules.DbColumnType.LONG;
import static com.dalio.cloud.generator.rules.DbColumnType.STRING;

/**
 * @author admin
 * @date 2022/3/13 23:02
 */
public class MySqlTypeConvert implements ITypeConvert {
    public static final MySqlTypeConvert INSTANCE = new MySqlTypeConvert();
    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\(\\d+\\)");

    /**
     * 转换为日期类型
     *
     * @param dt   日期类型
     * @param type 类型
     * @return 返回对应的列类型
     */
    public static ColumnType toDateType(DateType dt, String type) {
        if (type == null) {
            return STRING;
        }
        String dateType = DIGIT_PATTERN.matcher(type).replaceAll("");
        switch (dt) {
            case ONLY_DATE -> {
                return DbColumnType.DATE;
            }
            case SQL_PACK -> {
                return switch (dateType) {
                    case "date", "year" -> DbColumnType.DATE_SQL;
                    case "time" -> DbColumnType.TIME;
                    default -> DbColumnType.TIMESTAMP;
                };
            }
            case TIME_PACK -> {
                return switch (dateType) {
                    case "date" -> DbColumnType.LOCAL_DATE;
                    case "time" -> DbColumnType.LOCAL_TIME;
                    case "year" -> DbColumnType.YEAR;
                    default -> DbColumnType.LOCAL_DATE_TIME;
                };
            }
            default -> {
                return STRING;
            }
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public ColumnType processTypeConvert(DateType datetype, String fieldType, Long size, Integer digit) {
        return TypeConverts.use(fieldType)
                .test(containsAny("longtext", "char", "text", "json", "enum").then(STRING))
                .test(contains("bigint").then(LONG))
                .test(containsAny("tinyint(1)", "bit(1)").then(BOOLEAN))
                .test(contains("bit").then(BOOLEAN))
                .test(contains("int").then(INTEGER))
                .test(contains("decimal").then(BIG_DECIMAL))
                .test(contains("clob").then(CLOB))
                .test(contains("blob").then(BLOB))
                .test(contains("binary").then(BYTE_ARRAY))
                .test(contains("float").then(FLOAT))
                .test(contains("double").then(DOUBLE))
                .test(containsAny("date", "time", "year")
                        .then(t -> toDateType(datetype, t)))
                .or(STRING);
    }
}
