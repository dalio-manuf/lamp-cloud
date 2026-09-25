package com.dalio.cloud.file.utils;

import com.dalio.cloud.model.enumeration.base.FileType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FileTypeUtil 单元测试
 */
class FileTypeUtilTest {

    @Test
    @DisplayName("测试 getFileType 根据 Content-Type 识别文件类型")
    void testGetFileType() {
        assertEquals(FileType.OTHER, FileTypeUtil.getFileType(null));
        assertEquals(FileType.OTHER, FileTypeUtil.getFileType(""));
        assertEquals(FileType.OTHER, FileTypeUtil.getFileType("application/octet-stream"));

        assertEquals(FileType.IMAGE, FileTypeUtil.getFileType("image/png"));
        assertEquals(FileType.IMAGE, FileTypeUtil.getFileType("image/jpeg"));

        assertEquals(FileType.VIDEO, FileTypeUtil.getFileType("video/mp4"));
        assertEquals(FileType.AUDIO, FileTypeUtil.getFileType("audio/mpeg"));

        assertEquals(FileType.DOC, FileTypeUtil.getFileType("text/plain"));
        assertEquals(FileType.DOC, FileTypeUtil.getFileType("application/pdf"));
        assertEquals(FileType.DOC, FileTypeUtil.getFileType("application/msword"));
        assertEquals(FileType.DOC, FileTypeUtil.getFileType("application/vnd.ms-excel"));
    }

    @Test
    @DisplayName("测试 getUploadPathPrefix 与 getRelativePath 路径计算")
    void testPaths() {
        String uploadPrefix = FileTypeUtil.getUploadPathPrefix("/data/upload");
        assertNotNull(uploadPrefix);
        assertTrue(uploadPrefix.startsWith("/data/upload"));

        String base = "/data/upload";
        String fullPath = "/data/upload/2026/09/file1.png";
        String rel = FileTypeUtil.getRelativePath(base, fullPath);
        assertNotNull(rel);
    }
}
