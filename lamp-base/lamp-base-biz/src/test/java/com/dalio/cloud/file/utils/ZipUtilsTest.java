package com.dalio.cloud.file.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

class ZipUtilsTest {

    @Test
    @DisplayName("测试 zipFilesByInputStream 写入条目")
    void testZipFilesByInputStream() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            String content = "Hello Lamp Cloud!";
            ByteArrayInputStream bais = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            ZipUtils.zipFilesByInputStream(zos, "test.txt", bais);
        }
        byte[] zipBytes = baos.toByteArray();
        assertTrue(zipBytes.length > 0);
    }

    @Test
    @DisplayName("测试 unZipFiles 正常解压与多层目录解压")
    void testUnZipFilesNormal(@TempDir Path tempDir) throws IOException {
        // 创建一个包含子目录和文件的 zip
        File zipFile = tempDir.resolve("test.zip").toFile();
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            // 目录条目
            zos.putNextEntry(new ZipEntry("subdir/"));
            zos.closeEntry();

            // 文件条目
            zos.putNextEntry(new ZipEntry("subdir/hello.txt"));
            zos.write("World".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            // 根文件条目
            zos.putNextEntry(new ZipEntry("root.txt"));
            zos.write("Root".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        Path destDir = tempDir.resolve("extracted");
        ZipUtils.unZipFiles(zipFile, destDir.toString());

        File rootFile = destDir.resolve("root.txt").toFile();
        File subFile = destDir.resolve("subdir/hello.txt").toFile();

        assertTrue(rootFile.exists());
        assertTrue(subFile.exists());
        assertEquals("Root", Files.readString(rootFile.toPath()));
        assertEquals("World", Files.readString(subFile.toPath()));
    }

    @Test
    @DisplayName("测试 unZipFiles Zip Slip 路径穿越防御")
    void testUnZipFilesZipSlipProtection(@TempDir Path tempDir) throws IOException {
        File zipFile = tempDir.resolve("evil.zip").toFile();
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            // 恶意的父级跳转路径
            zos.putNextEntry(new ZipEntry("../../evil.txt"));
            zos.write("Hacked".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        Path destDir = tempDir.resolve("safe_dir");
        Files.createDirectories(destDir);

        assertThrows(IOException.class, () -> ZipUtils.unZipFiles(zipFile, destDir.toString()));
    }

    @Test
    @DisplayName("测试 zipFilesByInputStream HttpServletResponse 浏览器下载响应头编码")
    void testZipFilesResponseHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // 1. Firefox UA
        request.addHeader("USER-AGENT", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:80.0) Gecko/20100101 Firefox/80.0");
        Map<String, String> emptyMap = Collections.emptyMap();

        assertDoesNotThrow(() -> {
            ZipUtils.zipFilesByInputStream(emptyMap, 100L, "测试文件.zip", request, response);
        });
        assertTrue(response.getHeader("Content-Disposition").contains("fileName="));

        // 2. Chrome / Other UA
        MockHttpServletRequest chromeRequest = new MockHttpServletRequest();
        MockHttpServletResponse chromeResponse = new MockHttpServletResponse();
        chromeRequest.addHeader("USER-AGENT", "Mozilla/5.0 Chrome/120.0");

        assertDoesNotThrow(() -> {
            ZipUtils.zipFilesByInputStream(emptyMap, null, "test special !@#$.zip", chromeRequest, chromeResponse);
        });
        assertNotNull(chromeResponse.getHeader("Content-Disposition"));
    }

    @Test
    @DisplayName("测试 private zipFiles 多层文件夹与文件递归压缩")
    void testZipFilesRecursive(@TempDir Path tempDir) throws IOException {
        Path subDir = tempDir.resolve("sub_folder");
        Files.createDirectories(subDir);
        Files.writeString(subDir.resolve("child.txt"), "child content");
        Files.writeString(tempDir.resolve("top.txt"), "top content");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            org.springframework.test.util.ReflectionTestUtils.invokeMethod(
                    ZipUtils.class, "zipFiles", zos, "backup", tempDir.toFile().listFiles());
        }
        assertTrue(baos.toByteArray().length > 0);

        // 测试 null / empty 入参分支
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos2)) {
            org.springframework.test.util.ReflectionTestUtils.invokeMethod(
                    ZipUtils.class, "zipFiles", zos, "empty", (Object) new File[0]);
        }
    }

    @Test
    @DisplayName("测试 private downloadFile 流复制")
    void testDownloadFile() {
        byte[] data = "Sample download bytes".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        org.springframework.test.util.ReflectionTestUtils.invokeMethod(
                ZipUtils.class, "downloadFile", bais, baos);
        assertArrayEquals(data, baos.toByteArray());
    }

    @Test
    @DisplayName("测试 zipFilesByInputStream 单文件超时与多文件打包异常分支")
    void testZipFilesDownloadBranches() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // 1. 单文件网络异常分支 (触发 BizException)
        Map<String, String> singleMap = Map.of("err.txt", "http://127.0.0.1:54321/not-reachable");
        assertThrows(com.dalio.basic.exception.BizException.class, () -> {
            ZipUtils.zipFilesByInputStream(singleMap, 100L, "test.zip", request, response);
        });

        // 2. 多文件打包循环异常分支 (捕捉并继续)
        Map<String, String> multiMap = Map.of(
                "f1.txt", "http://127.0.0.1:54321/f1",
                "f2.txt", "http://127.0.0.1:54321/f2"
        );
        MockHttpServletResponse multiResponse = new MockHttpServletResponse();
        assertDoesNotThrow(() -> {
            ZipUtils.zipFilesByInputStream(multiMap, 200L, "multi.zip", request, multiResponse);
        });
    }
}
