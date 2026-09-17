package com.dalio.cloud.file.manager;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.dalio.cloud.file.dto.chunk.FileUploadDTO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * WebUploader 单元测试
 *
 * @author went
 */
class WebUploaderTest {

    @Test
    @DisplayName("测试 chunkCheck 分片检测")
    void testChunkCheck(@TempDir Path tempDir) throws IOException {
        WebUploader wu = new WebUploader();

        // 不存在的文件
        assertFalse(wu.chunkCheck(tempDir.resolve("non-existent").toString(), 10L));

        // 存在且大小匹配
        Path file = tempDir.resolve("part1");
        Files.write(file, new byte[]{1, 2, 3});
        assertTrue(wu.chunkCheck(file.toString(), 3L));

        // 存在但大小不匹配
        assertFalse(wu.chunkCheck(file.toString(), 5L));
    }

    @Test
    @DisplayName("测试 getReadySpace 空间分配")
    void testGetReadySpace(@TempDir Path tempDir) {
        WebUploader wu = new WebUploader();
        FileUploadDTO dto = new FileUploadDTO();

        // chunks <= 0
        dto.setChunks(0);
        assertNull(wu.getReadySpace(dto, tempDir.toString()));

        // chunks > 0
        dto.setChunks(2);
        dto.setChunk(1);
        dto.setName("test.zip");
        dto.setType("application/zip");
        dto.setLastModifiedDate("2026-09-16");
        dto.setSize(2048L);

        File target = wu.getReadySpace(dto, tempDir.toString());
        assertNotNull(target);
        assertEquals("1", target.getName());

        // 再次调用覆盖已存在 tmp 文件分支
        File target2 = wu.getReadySpace(dto, tempDir.toString());
        assertNotNull(target2);

        assertNull(wu.getErrorMsg());
    }
}
