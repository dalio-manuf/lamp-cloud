package com.dalio.cloud.generator.controller;

import com.dalio.basic.base.request.DownloadVO;
import com.dalio.cloud.generator.service.DefGenTableService;
import com.dalio.cloud.generator.vo.save.ProjectGeneratorVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DefGenProjectControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DefGenTableService defGenTableService;

    @InjectMocks
    private DefGenProjectController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("测试 getDef 获取默认配置")
    void testGetDef() throws Exception {
        ProjectGeneratorVO vo = new ProjectGeneratorVO();
        vo.setAuthor("TestAuthor");
        when(defGenTableService.getDef()).thenReturn(vo);

        mockMvc.perform(post("/defGenProject/getDef"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.author").value("TestAuthor"));
    }

    @Test
    @DisplayName("测试 getProperties 获取系统属性")
    void testGetProperties() throws Exception {
        mockMvc.perform(post("/defGenProject/anno/getProperties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("测试 download 下载项目")
    void testDownload() throws Exception {
        DownloadVO downloadVO = DownloadVO.builder().data(new byte[]{1, 2, 3}).fileName("test.zip").build();

        when(defGenTableService.download(any())).thenReturn(downloadVO);

        ProjectGeneratorVO vo = new ProjectGeneratorVO();
        vo.setAuthor("Test");
        vo.setType(com.dalio.cloud.generator.enumeration.ProjectTypeEnum.CLOUD);
        vo.setOutputDir("test");
        vo.setProjectPrefix("test");
        vo.setServiceName("test");
        vo.setModuleName("test");
        vo.setParent("com.test");
        vo.setGroupId("com.test");
        vo.setUtilParent("com.test");
        vo.setUtilGroupId("com.test");
        vo.setVersion("1.0.0");
        vo.setDescription("Test");
        vo.setServerPort(8080);
        vo.setSeata(false);

        mockMvc.perform(post("/defGenProject/download")
                        .flashAttr("projectGeneratorVO", vo))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("测试 generator 生成项目")
    void testGenerator() throws Exception {
        mockMvc.perform(post("/defGenProject/generator")
                        .contentType("application/json")
                        .content("{\"author\":\"Test\",\"type\":\"CLOUD\",\"outputDir\":\"test\",\"projectPrefix\":\"test\",\"serviceName\":\"test\",\"moduleName\":\"test\",\"parent\":\"com.test\",\"groupId\":\"com.test\",\"utilParent\":\"com.test\",\"utilGroupId\":\"com.test\",\"version\":\"1.0.0\",\"description\":\"Test\",\"serverPort\":8080,\"seata\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));

        verify(defGenTableService, times(1)).generator(any());
    }
}
