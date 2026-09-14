package com.dalio.cloud.generator.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FileInsertUtil 单元测试
 */
class FileInsertUtilTest {

    @Test
    @DisplayName("getSeparator 返回系统行分隔符")
    void testGetSeparator() {
        String sep = FileInsertUtil.getSeparator();
        assertEquals(System.lineSeparator(), sep);
    }

    @Test
    @DisplayName("repeatTab(0) 返回空串，repeatTab(2) 返回两个 Tab")
    void testRepeatTab() {
        assertEquals("", FileInsertUtil.repeatTab(0));
        assertEquals("\t", FileInsertUtil.repeatTab(1));
        assertEquals("\t\t", FileInsertUtil.repeatTab(2));
        // 无参重载等同于 repeatTab(1)
        assertEquals("\t", FileInsertUtil.repeatTab());
    }

    @Test
    @DisplayName("of(path, map) 工厂方法创建实例不为空")
    void testOfFactory() {
        FileInsertUtil util = FileInsertUtil.of("/any.java", Map.of("key", "value"));
        assertNotNull(util);
    }

    @Test
    @DisplayName("of(path, prefix, map) 工厂方法创建实例不为空")
    void testOfFactoryWithPrefix() {
        FileInsertUtil util = FileInsertUtil.of("/any.java", "    ", Map.of("key", "value"));
        assertNotNull(util);
    }

    @Test
    @DisplayName("SLOT_PATTERN 能匹配标准插槽注释")
    void testSlotPatternMatch() {
        String line1 = "// @lamp.generator auto insert EchoDictType";
        String line2 = "// @lamp.generator auto insert EchoRef -->";
        assertTrue(FileInsertUtil.SLOT_PATTERN.matcher(line1).find());
        assertTrue(FileInsertUtil.SLOT_PATTERN.matcher(line2).find());
    }

    @Test
    @DisplayName("SLOT_PATTERN 不匹配普通注释")
    void testSlotPatternNoMatch() {
        assertFalse(FileInsertUtil.SLOT_PATTERN.matcher("// normal comment").find());
        assertFalse(FileInsertUtil.SLOT_PATTERN.matcher("String s = \"value\";").find());
    }
}
