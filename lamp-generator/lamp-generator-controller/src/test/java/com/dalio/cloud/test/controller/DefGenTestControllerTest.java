package com.dalio.cloud.test.controller;

import com.dalio.basic.interfaces.echo.EchoService;
import com.dalio.cloud.model.entity.system.SysUser;
import com.dalio.cloud.test.entity.DefGenTestTree;
import com.dalio.cloud.test.service.DefGenTestSimpleService;
import com.dalio.cloud.test.service.DefGenTestTreeService;
import com.dalio.cloud.test.vo.query.DefGenTestTreePageQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DefGenTestControllerTest {

    @Mock
    private EchoService echoService;

    @Mock
    private DefGenTestSimpleService simpleService;

    @Mock
    private DefGenTestTreeService treeService;

    private DefGenTestSimpleController simpleController;
    private DefGenTestTreeController treeController;

    private MockMvc treeMockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        simpleController = new DefGenTestSimpleController(echoService);
        ReflectionTestUtils.setField(simpleController, "superService", simpleService);

        treeController = new DefGenTestTreeController(echoService);
        ReflectionTestUtils.setField(treeController, "superService", treeService);

        treeMockMvc = MockMvcBuilders.standaloneSetup(treeController).build();
    }

    @Test
    @DisplayName("测试 DefGenTestSimpleController getEchoService")
    void testSimpleController() {
        assertNotNull(simpleController.getEchoService());
        assertSame(echoService, simpleController.getEchoService());
    }

    @Test
    @DisplayName("测试 DefGenTestTreeController getEchoService 与 tree 接口")
    void testTreeController() throws Exception {
        assertNotNull(treeController.getEchoService());
        assertSame(echoService, treeController.getEchoService());

        DefGenTestTree treeNode = new DefGenTestTree();
        treeNode.setId(1L);
        treeNode.setLabel("根节点");
        when(treeService.findTree(any(DefGenTestTreePageQuery.class))).thenReturn(List.of(treeNode));

        treeMockMvc.perform(post("/defGenTestTree/tree")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].label").value("根节点"));

        // 测试 /anyone/test
        SysUser user = new SysUser();
        user.setId(100L);
        user.setUsername("testuser");
        assertNotNull(treeController.test(user));
    }
}
