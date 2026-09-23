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

    @Test
    @DisplayName("测试 writeFile")
    void testWriteFile() throws Exception {
        java.io.File tempFile = cn.hutool.core.io.FileUtil.createTempFile("FileInsertUtilTest", ".txt", new java.io.File(System.getProperty("java.io.tmpdir")), true);
        org.apache.commons.io.FileUtils.writeStringToFile(tempFile, "Some text @lamp.generator auto insert testKey -->", java.nio.charset.StandardCharsets.UTF_8);

        Map<String, String> map = new java.util.HashMap<>();
        map.put("testKey", "testValue");
        FileInsertUtil util2 = FileInsertUtil.of(tempFile.getAbsolutePath(), "\t", map);
        util2.writeFile();

        String newCon = org.apache.commons.io.FileUtils.readFileToString(tempFile, java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(newCon.contains("testValue"));

        // Write file missing
        FileInsertUtil util3 = FileInsertUtil.of("missing_path", map);
        Exception e = assertThrows(Exception.class, () -> util3.writeFile());
        assertNotNull(e);

        Exception exception = assertThrows(Exception.class, () -> util3.replaceAll());
        assertNotNull(exception);
    }
}
