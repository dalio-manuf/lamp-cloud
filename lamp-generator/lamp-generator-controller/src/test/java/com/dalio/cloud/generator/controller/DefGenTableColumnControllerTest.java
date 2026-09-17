package com.dalio.cloud.generator.controller;

import com.dalio.basic.interfaces.echo.EchoService;
import com.dalio.cloud.generator.service.DefGenTableColumnService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DefGenTableColumnControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DefGenTableColumnService defGenTableColumnService;

    @Mock
    private EchoService echoService;

    @InjectMocks
    private DefGenTableColumnController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        org.springframework.test.util.ReflectionTestUtils.setField(controller, "superService", defGenTableColumnService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("测试 syncField 同步字段结构")
    void testSyncField() throws Exception {
        when(defGenTableColumnService.syncField(anyLong(), anyLong())).thenReturn(true);

        mockMvc.perform(post("/defGenTableColumn/syncField")
                        .param("tableId", "1")
                        .param("id", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }
}
