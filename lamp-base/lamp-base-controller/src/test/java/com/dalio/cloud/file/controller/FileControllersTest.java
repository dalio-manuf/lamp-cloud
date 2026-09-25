package com.dalio.cloud.file.controller;

import com.dalio.basic.base.R;
import com.dalio.basic.interfaces.echo.EchoService;
import com.dalio.cloud.file.entity.File;
import com.dalio.cloud.file.manager.WebUploader;
import com.dalio.cloud.file.properties.FileServerProperties;
import com.dalio.cloud.file.service.AppendixService;
import com.dalio.cloud.file.service.FileService;
import com.dalio.cloud.file.strategy.FileContext;
import com.dalio.cloud.file.vo.result.FileResultVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 文件相关控制器单元测试
 *
 * @author went
 */
class FileControllersTest {

    @Mock
    private EchoService echoService;

    @Mock
    private FileService fileService;

    @Mock
    private AppendixService appendixService;

    @Mock
    private FileContext fileContext;

    @Mock
    private WebUploader webUploader;

    @InjectMocks
    private FileController fileController;

    @InjectMocks
    private FileAnyoneController fileAnyoneController;

    @InjectMocks
    private AppendixController appendixController;

    private FileChunkController chunkController;
    private FileServerProperties fileServerProperties;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(fileController, "superService", fileService);

        fileServerProperties = new FileServerProperties();
        FileServerProperties.Local local = new FileServerProperties.Local();
        local.setStoragePath("/tmp/upload");
        fileServerProperties.setLocal(local);

        chunkController = new FileChunkController(fileServerProperties, fileContext, fileService, webUploader);
    }

    @Test
    @DisplayName("测试 FileController 基础方法")
    void testFileController() {
        assertNotNull(fileController.getEchoService());
        assertEquals(File.class, fileController.getResultVOClass());
        fileController.handlerQueryParams(null);
    }

    @Test
    @DisplayName("测试 FileAnyoneController 上传、查询与下载接口")
    void testFileAnyoneController() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(fileAnyoneController).build();

        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "abc".getBytes());
        FileResultVO vo = new FileResultVO();
        vo.setId(10L);
        when(fileService.upload(any(), any())).thenReturn(vo);
        when(fileService.findUrlById(any())).thenReturn(Collections.singletonMap(10L, "http://url"));

        mockMvc.perform(multipart("/anyone/file/upload").file(file).param("bizType", "user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(10));

        mockMvc.perform(post("/anyone/file/findUrlById")
                        .contentType("application/json")
                        .content("[10]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.10").value("http://url"));

        mockMvc.perform(get("/anyone/file/download").param("ids", "10,20"))
                .andExpect(status().isOk());
        verify(fileService).download(any(), any(), eq(java.util.Arrays.asList(10L, 20L)));

        mockMvc.perform(get("/anyone/file/down").param("id", "10"))
                .andExpect(status().isOk());
        verify(fileService).download(any(), any(), eq(10L));
    }

    @Test
    @DisplayName("测试 AppendixController 业务附件查询")
    void testAppendixController() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(appendixController).build();

        when(appendixService.listByBizIdAndBizType(anyLong(), any())).thenReturn(Collections.emptyList());
        when(fileService.listByBizIdAndBizType(anyLong(), any())).thenReturn(Collections.emptyList());

        mockMvc.perform(post("/anyone/appendix/listByBizId").param("bizId", "1").param("bizType", "order"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/anyone/appendix/listFileByBizId").param("bizId", "1").param("bizType", "order"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("测试 FileChunkController 分片上传接口")
    void testFileChunkController() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(chunkController).build();

        when(fileContext.md5Check(anyString(), any())).thenReturn(new File());
        when(webUploader.chunkCheck(anyString(), any())).thenReturn(true);
        when(fileContext.chunksMerge(any())).thenReturn(R.success(new File()));

        mockMvc.perform(post("/chunk/md5").param("md5", "md5val"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

        mockMvc.perform(post("/chunk/check")
                        .contentType("application/json")
                        .content("{\"name\":\"chunk1\",\"chunkIndex\":0,\"size\":100}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

        mockMvc.perform(post("/chunk/merge")
                        .contentType("application/json")
                        .content("{\"name\":\"merge.zip\"}"))
                .andExpect(status().isOk());

        // uploadFile 无文件
        mockMvc.perform(post("/chunk/upload"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(-10));

        // uploadFile chunks <= 0
        MockMultipartFile file = new MockMultipartFile("file", "single.txt", "text/plain", "hello".getBytes());
        File savedFile = new File();
        when(fileContext.upload(any(), any())).thenReturn(savedFile);
        when(fileService.save(any())).thenReturn(savedFile);

        mockMvc.perform(multipart("/chunk/upload").file(file).param("chunks", "0").param("md5", "md5abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("single.txt"));

        // uploadFile chunks > 0
        java.io.File target = new java.io.File("/tmp/1");
        when(webUploader.getReadySpace(any(), anyString())).thenReturn(target);
        mockMvc.perform(multipart("/chunk/upload").file(file).param("chunks", "2").param("chunk", "1").param("name", "part"))
                .andExpect(status().isOk());
    }
}
