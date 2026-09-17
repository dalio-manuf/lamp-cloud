package com.dalio.cloud.file.strategy.impl.fastdfs;

import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.AppendFileStorageClient;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import com.dalio.basic.base.R;
import com.dalio.cloud.file.domain.FileDeleteBO;
import com.dalio.cloud.file.domain.FileGetUrlBO;
import com.dalio.cloud.file.dto.chunk.FileChunksMergeDTO;
import com.dalio.cloud.file.entity.File;
import com.dalio.cloud.file.mapper.FileMapper;
import com.dalio.cloud.file.properties.FileServerProperties;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * FastDfsFileStrategyImpl 与 FastDfsFileChunkStrategyImpl 单元测试
 */
class FastDfsStrategyTest {

    @Test
    @DisplayName("测试 FastDfsFileStrategyImpl 上传、删除与查询URL")
    void testFastDfsFileStrategyImpl() throws Exception {
        FileServerProperties properties = new FileServerProperties();
        properties.getFastDfs().setUrlPrefix("http://dfs.example.com/");
        FastFileStorageClient storageClient = mock(FastFileStorageClient.class);
        FileMapper fileMapper = mock(FileMapper.class);

        FastDfsFileStrategyImpl strategy = new FastDfsFileStrategyImpl(properties, storageClient, fileMapper);

        // 1. uploadFile
        File file = new File();
        file.setSuffix("png");
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.png", "image/png", "content".getBytes());

        StorePath storePath = new StorePath("group1", "M00/00/00/test.png");
        when(storageClient.uploadFile(any(InputStream.class), eq(7L), eq("png"), isNull())).thenReturn(storePath);

        strategy.uploadFile(file, multipartFile, "group1");
        assertEquals("group1", file.getBucket());
        assertEquals("M00/00/00/test.png", file.getPath());
        assertEquals("http://dfs.example.com/group1/M00/00/00/test.png", file.getUrl());

        // 2. delete
        FileDeleteBO deleteBO = FileDeleteBO.builder().bucket("group1").path("M00/00/00/test.png").build();
        assertTrue(strategy.delete(deleteBO));
        verify(storageClient).deleteFile("group1", "M00/00/00/test.png");

        // 3. findUrl
        FileGetUrlBO urlBO = FileGetUrlBO.builder().bucket("group1").path("M00/00/00/test.png").build();
        Map<String, String> urlMap = strategy.findUrl(List.of(urlBO));
        assertEquals("http://dfs.example.com/group1/M00/00/00/test.png", urlMap.get("M00/00/00/test.png"));
    }

    @Test
    @DisplayName("测试 FastDfsFileChunkStrategyImpl copyFile 与 merge 分片逻辑")
    void testFastDfsFileChunkStrategyImpl(@TempDir Path tempDir) throws Exception {
        FileServerProperties properties = new FileServerProperties();
        properties.getFastDfs().setUrlPrefix("http://dfs.example.com/");
        FileMapper fileMapper = mock(FileMapper.class);
        AppendFileStorageClient storageClient = mock(AppendFileStorageClient.class);

        FastDfsFileChunkStrategyImpl chunkStrategy = new FastDfsFileChunkStrategyImpl(fileMapper, properties, storageClient);

        // 1. copyFile 无操作
        assertDoesNotThrow(() -> chunkStrategy.copyFile(new File()));

        // 2. merge 逻辑
        Path part1 = tempDir.resolve("chunk1.tmp");
        Path part2 = tempDir.resolve("chunk2.tmp");
        Files.writeString(part1, "part1");
        Files.writeString(part2, "part2");

        List<java.io.File> fileList = List.of(part1.toFile(), part2.toFile());
        FileChunksMergeDTO info = new FileChunksMergeDTO();
        info.setExt("txt");

        StorePath storePath = new StorePath("group1", "M00/00/00/chunk_merged.txt");
        when(storageClient.uploadAppenderFile(isNull(), any(InputStream.class), eq(5L), eq("txt"))).thenReturn(storePath);

        R<File> result = chunkStrategy.merge(fileList, "/path", "chunk_merged.txt", info);
        assertTrue(result.getIsSuccess());
        assertNotNull(result.getData());
        assertEquals("http://dfs.example.com/group1/M00/00/00/chunk_merged.txt", result.getData().getUrl());
        verify(storageClient).appendFile(eq("group1"), eq("M00/00/00/chunk_merged.txt"), any(InputStream.class), eq(5L));

        // 3. merge 失败逻辑
        R<File> emptyResult = chunkStrategy.merge(Collections.emptyList(), "/path", "fail.txt", info);
        assertFalse(emptyResult.getIsSuccess());
    }
}
