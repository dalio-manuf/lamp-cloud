package com.dalio.cloud.generator.utils;

import com.dalio.cloud.generator.rules.NamingStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NamingStrategy 和 ProjectUtils 工具类单元测试
 */
class ProjectUtilsTest {

    // ── NamingStrategy ──────────────────────────────────────────────────────

    @Test
    @DisplayName("underlineToCamel: 空字符串返回空")
    void testUnderlineToCamelBlank() {
        assertEquals("", NamingStrategy.underlineToCamel(null));
        assertEquals("", NamingStrategy.underlineToCamel(""));
        assertEquals("", NamingStrategy.underlineToCamel("  "));
    }

    @Test
    @DisplayName("underlineToCamel: 标准下划线转驼峰")
    void testUnderlineToCamelNormal() {
        assertEquals("baseOperationLog", NamingStrategy.underlineToCamel("base_operation_log"));
        assertEquals("defUser", NamingStrategy.underlineToCamel("def_user"));
        assertEquals("id", NamingStrategy.underlineToCamel("id"));
    }

    @Test
    @DisplayName("underlineToCamel: 全大写字符串先转小写再驼峰")
    void testUnderlineToCamelCapital() {
        // CAPITAL_MODE -> lowercase first, then camel
        assertEquals("baseUser", NamingStrategy.underlineToCamel("BASE_USER"));
    }

    @Test
    @DisplayName("removePrefix: 匹配前缀时去掉前缀")
    void testRemovePrefix() {
        List<String> prefix = Arrays.asList("base_", "def_");
        assertEquals("operation_log", NamingStrategy.removePrefix("base_operation_log", prefix));
        assertEquals("user", NamingStrategy.removePrefix("def_user", prefix));
        // 无匹配时原样返回
        assertEquals("sys_user", NamingStrategy.removePrefix("sys_user", prefix));
    }

    @Test
    @DisplayName("removePrefix: 空表名返回空串")
    void testRemovePrefixBlank() {
        assertEquals("", NamingStrategy.removePrefix("", List.of("def_")));
        assertEquals("", NamingStrategy.removePrefix(null, List.of("def_")));
    }

    @Test
    @DisplayName("removeSuffix: 匹配后缀时去掉后缀")
    void testRemoveSuffix() {
        List<String> suffix = List.of("_log", "_info");
        assertEquals("base_operation", NamingStrategy.removeSuffix("base_operation_log", suffix));
        assertEquals("user", NamingStrategy.removeSuffix("user_info", suffix));
        assertEquals("user_data", NamingStrategy.removeSuffix("user_data", suffix));
    }

    @Test
    @DisplayName("removePrefixAndCamel: 去前缀后再驼峰")
    void testRemovePrefixAndCamel() {
        assertEquals("operationLog", NamingStrategy.removePrefixAndCamel("base_operation_log", List.of("base_")));
    }

    @Test
    @DisplayName("capitalFirst: 首字母大写")
    void testCapitalFirst() {
        assertEquals("User", NamingStrategy.capitalFirst("user"));
        assertEquals("", NamingStrategy.capitalFirst(""));
        assertEquals("", NamingStrategy.capitalFirst(null));
        assertEquals("ABC", NamingStrategy.capitalFirst("ABC"));
    }

    // ── ProjectUtils ────────────────────────────────────────────────────────

    @Test
    @DisplayName("测试 ProjectUtils.download 在内存中生成项目结构压缩包")
    void testProjectUtilsDownload() {
        com.dalio.cloud.generator.vo.save.ProjectGeneratorVO vo = new com.dalio.cloud.generator.vo.save.ProjectGeneratorVO();
        vo.setServiceName("demo");
        vo.setDescription("演示模块");
        vo.setProjectPrefix("lamp");
        vo.setParent("com.dalio.cloud");
        vo.setModuleName("demo");
        vo.setGroupId("com.dalio.cloud");
        vo.setUtilGroupId("com.dalio.cloud");
        vo.setUtilParent("com.dalio.cloud");
        vo.setServerPort(8760);
        vo.setOutputDir("");

        com.dalio.basic.database.properties.DatabaseProperties dbProps = new com.dalio.basic.database.properties.DatabaseProperties();
        dbProps.setMultiTenantType(com.dalio.basic.database.properties.MultiTenantType.NONE);

        // 1. CLOUD 模式下载
        vo.setType(com.dalio.cloud.generator.enumeration.ProjectTypeEnum.CLOUD);
        com.dalio.basic.base.request.DownloadVO dlCloud = ProjectUtils.download(vo, dbProps);
        assertNotNull(dlCloud);
        assertNotNull(dlCloud.getData());
        assertTrue(dlCloud.getData().length > 0);
        assertTrue(dlCloud.getFileName().contains("demo"));

        // 2. BOOT 模式下载
        vo.setType(com.dalio.cloud.generator.enumeration.ProjectTypeEnum.BOOT);
        com.dalio.basic.base.request.DownloadVO dlBoot = ProjectUtils.download(vo, dbProps);
        assertNotNull(dlBoot);
        assertNotNull(dlBoot.getData());
        assertTrue(dlBoot.getData().length > 0);
    }

    @Test
    @DisplayName("测试 ProjectUtils.generator 本地目录结构与增量插槽写入")
    void testProjectUtilsGenerator(@org.junit.jupiter.api.io.TempDir java.nio.file.Path tempDir) throws java.io.IOException {
        // 准备插槽文件
        java.io.File rootPom = new java.io.File(tempDir.toFile(), "pom.xml");
        org.apache.commons.io.FileUtils.writeStringToFile(rootPom,
                "<project>\n    <modules>\n        <!-- @lamp.generator auto insert root.pom.xml -->\n    </modules>\n</project>",
                java.nio.charset.StandardCharsets.UTF_8);

        java.io.File bootServerPom = new java.io.File(tempDir.toFile(), "lamp-support/lamp-boot-server/pom.xml");
        org.apache.commons.io.FileUtils.writeStringToFile(bootServerPom,
                "<project>\n    <dependencies>\n        <!-- @lamp.generator auto insert server.pom.xml -->\n    </dependencies>\n</project>",
                java.nio.charset.StandardCharsets.UTF_8);

        java.io.File docYml = new java.io.File(tempDir.toFile(), "lamp-support/lamp-boot-server/src/main/resources/config/dev/doc.yml");
        org.apache.commons.io.FileUtils.writeStringToFile(docYml,
                "springdoc:\n  groupconfigs:\n    # @lamp.generator auto insert springdoc.groupconfigs\n",
                java.nio.charset.StandardCharsets.UTF_8);

        com.dalio.cloud.generator.vo.save.ProjectGeneratorVO vo = new com.dalio.cloud.generator.vo.save.ProjectGeneratorVO();
        vo.setServiceName("order");
        vo.setDescription("订单服务");
        vo.setAuthor("admin");
        vo.setProjectPrefix("lamp");
        vo.setParent("com.dalio.cloud");
        vo.setModuleName("order");
        vo.setGroupId("com.dalio.cloud");
        vo.setUtilGroupId("com.dalio.cloud");
        vo.setUtilParent("com.dalio.cloud");
        vo.setServerPort(8760);
        vo.setOutputDir(tempDir.toString());

        com.dalio.basic.database.properties.DatabaseProperties dbProps = new com.dalio.basic.database.properties.DatabaseProperties();
        dbProps.setMultiTenantType(com.dalio.basic.database.properties.MultiTenantType.COLUMN);

        // 1. BOOT 模式生成
        vo.setType(com.dalio.cloud.generator.enumeration.ProjectTypeEnum.BOOT);
        ProjectUtils.generator(vo, dbProps);

        // 验证文件生成
        java.io.File entityPom = new java.io.File(tempDir.toFile(), "lamp-order/lamp-order-entity/pom.xml");
        assertTrue(entityPom.exists());

        // 2. CLOUD 模式生成
        vo.setServiceName("inventory");
        vo.setType(com.dalio.cloud.generator.enumeration.ProjectTypeEnum.CLOUD);
        ProjectUtils.generator(vo, dbProps);

        java.io.File cloudServerPom = new java.io.File(tempDir.toFile(), "lamp-inventory/lamp-inventory-server/pom.xml");
        assertTrue(cloudServerPom.exists());
    }
}
