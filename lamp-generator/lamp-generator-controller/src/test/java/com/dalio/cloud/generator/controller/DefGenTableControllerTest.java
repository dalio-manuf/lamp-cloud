package com.dalio.cloud.generator.controller;

import com.dalio.basic.base.request.DownloadVO;
import com.dalio.basic.base.request.PageParams;
import com.dalio.basic.interfaces.echo.EchoService;
import com.dalio.cloud.generator.enumeration.TemplateEnum;
import com.dalio.cloud.generator.service.DefGenTableService;
import com.dalio.cloud.generator.vo.query.DefGenTablePageQuery;
import com.dalio.cloud.generator.vo.result.DefGenTableResultVO;
import com.dalio.cloud.generator.vo.save.DefGenTableImportVO;
import com.dalio.cloud.generator.vo.save.DefGenVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DefGenTableControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DefGenTableService defGenTableService;

    @Mock
    private EchoService echoService;

    @InjectMocks
    private DefGenTableController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        org.springframework.test.util.ReflectionTestUtils.setField(controller, "superService", defGenTableService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("测试 importCheck")
    void testImportCheck() throws Exception {
        when(defGenTableService.importCheck(any())).thenReturn(true);
        mockMvc.perform(post("/defGenTable/importCheck")
                        .contentType("application/json")
                        .content("[\"sys_user\"]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("测试 importTable")
    void testImportTable() throws Exception {
        when(defGenTableService.importTable(any(DefGenTableImportVO.class))).thenReturn(true);
        mockMvc.perform(post("/defGenTable/importTable")
                        .contentType("application/json")
                        .content("{\"dsId\":\"1\",\"tableNames\":[\"sys_user\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("测试 syncField")
    void testSyncField() throws Exception {
        mockMvc.perform(post("/defGenTable/syncField")
                        .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
        verify(defGenTableService, times(1)).syncField(anyLong());
    }

    @Test
    @DisplayName("测试 previewCode")
    void testPreviewCode() throws Exception {
        when(defGenTableService.previewCode(anyLong(), any())).thenReturn(new HashMap<>());
        mockMvc.perform(post("/defGenTable/previewCode")
                        .param("id", "1")
                        .param("template", "BACKEND"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("测试 generatorCode")
    void testGeneratorCode() throws Exception {
        mockMvc.perform(post("/defGenTable/generatorCode")
                        .contentType("application/json")
                        .content("{\"ids\":[1],\"template\":\"BACKEND\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
        verify(defGenTableService, times(1)).generatorCode(any(DefGenVO.class));
    }

    @Test
    @DisplayName("测试 downloadZip")
    void testDownloadZip() throws Exception {
        DownloadVO downloadVO = DownloadVO.builder().data(new byte[]{1, 2, 3}).fileName("test.zip").build();

        when(defGenTableService.downloadZip(any(), any())).thenReturn(downloadVO);

        mockMvc.perform(get("/defGenTable/downloadZip")
                        .param("ids", "1")
                        .param("template", "BACKEND"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("测试 getDetail")
    void testGetDetail() throws Exception {
        DefGenTableResultVO vo = new DefGenTableResultVO();
        vo.setId(1L);
        when(defGenTableService.getDetail(anyLong())).thenReturn(vo);

        mockMvc.perform(get("/defGenTable/detail")
                        .param("id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("测试 getFieldTemplate")
    void testGetFieldTemplate() throws Exception {
        when(defGenTableService.getFieldTemplate(any())).thenReturn(new HashMap<>());
        mockMvc.perform(get("/defGenTable/getFieldTemplate")
                        .param("template", "BACKEND"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("测试 getDefFileOverrideStrategy")
    void testGetDefFileOverrideStrategy() throws Exception {
        when(defGenTableService.getDefFileOverrideStrategy()).thenReturn(new HashMap<>());
        mockMvc.perform(get("/defGenTable/getDefFileOverrideStrategy"))
                .andExpect(status().isOk());
    }
}
