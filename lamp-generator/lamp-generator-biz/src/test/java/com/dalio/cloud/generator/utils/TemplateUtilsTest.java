package com.dalio.cloud.generator.utils;

import com.dalio.cloud.generator.enumeration.PopupTypeEnum;
import com.dalio.cloud.generator.enumeration.TemplateEnum;
import com.dalio.cloud.generator.enumeration.TplEnum;
import freemarker.template.Template;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TemplateUtilsTest {

    @Test
    @DisplayName("测试 TemplateUtils.getTemplate 获取 Freemarker 模板")
    void testGetTemplate() throws IOException {
        Template tpl = TemplateUtils.getTemplate(GenCodeConstant.TEMPLATE_ENTITY_JAVA);
        assertNotNull(tpl);
        assertEquals("templates/backend/java/entity.java.ftl", tpl.getName());
    }

    @Test
    @DisplayName("测试 TemplateUtils.getTemplateList 各种前端与后端分支")
    void testGetTemplateList() {
        // 1. BACKEND
        List<String> backendList = TemplateUtils.getTemplateList(TemplateEnum.BACKEND, TplEnum.SIMPLE, PopupTypeEnum.MODAL);
        assertTrue(backendList.contains(GenCodeConstant.TEMPLATE_CONTROLLER));
        assertTrue(backendList.contains(GenCodeConstant.TEMPLATE_SERVICE));
        assertTrue(backendList.contains(GenCodeConstant.TEMPLATE_SERVICE_IMPL));
        assertTrue(backendList.contains(GenCodeConstant.TEMPLATE_ENTITY_JAVA));

        // 2. WEB_PLUS - TREE
        List<String> webPlusTree = TemplateUtils.getTemplateList(TemplateEnum.WEB_PLUS, TplEnum.TREE, PopupTypeEnum.MODAL);
        assertTrue(webPlusTree.contains(GenCodeConstant.TEMPLATE_WEB_PRO_TREE_INDEX));
        assertTrue(webPlusTree.contains(GenCodeConstant.TEMPLATE_WEB_PRO_TREE_EDIT));
        assertTrue(webPlusTree.contains(GenCodeConstant.TEMPLATE_WEB_PRO_TREE_TREE));

        // 3. WEB_PLUS - MAIN_SUB (JUMP vs MODAL)
        List<String> webPlusMainJump = TemplateUtils.getTemplateList(TemplateEnum.WEB_PLUS, TplEnum.MAIN_SUB, PopupTypeEnum.JUMP);
        assertTrue(webPlusMainJump.contains(GenCodeConstant.TEMPLATE_WEB_PRO_MAIN_JUMP_EDIT));
        assertTrue(webPlusMainJump.contains(GenCodeConstant.TEMPLATE_WEB_PRO_MAIN_INDEX));
        assertTrue(webPlusMainJump.contains(GenCodeConstant.TEMPLATE_WEB_PRO_MAIN_SUB_INDEX));

        List<String> webPlusMainModal = TemplateUtils.getTemplateList(TemplateEnum.WEB_PLUS, TplEnum.MAIN_SUB, PopupTypeEnum.MODAL);
        assertTrue(webPlusMainModal.contains(GenCodeConstant.TEMPLATE_WEB_PRO_MAIN_EDIT));

        // 4. WEB_PLUS - SIMPLE (JUMP vs MODAL)
        List<String> webPlusSimpleJump = TemplateUtils.getTemplateList(TemplateEnum.WEB_PLUS, TplEnum.SIMPLE, PopupTypeEnum.JUMP);
        assertTrue(webPlusSimpleJump.contains(GenCodeConstant.TEMPLATE_WEB_PRO_SIMPLE_JUMP_EDIT));

        List<String> webPlusSimpleModal = TemplateUtils.getTemplateList(TemplateEnum.WEB_PLUS, TplEnum.SIMPLE, PopupTypeEnum.MODAL);
        assertTrue(webPlusSimpleModal.contains(GenCodeConstant.TEMPLATE_WEB_PRO_SIMPLE_EDIT));

        // 5. WEB_SOYBEAN (TREE, MAIN_SUB, SIMPLE)
        List<String> soyTree = TemplateUtils.getTemplateList(TemplateEnum.WEB_SOYBEAN, TplEnum.TREE, PopupTypeEnum.MODAL);
        assertTrue(soyTree.contains(GenCodeConstant.TEMPLATE_WEB_SOYBEAN_TREE_INDEX));
        assertTrue(soyTree.contains(GenCodeConstant.TEMPLATE_WEB_SOYBEAN_TREE_CRUD));

        List<String> soyMain = TemplateUtils.getTemplateList(TemplateEnum.WEB_SOYBEAN, TplEnum.MAIN_SUB, PopupTypeEnum.MODAL);
        assertTrue(soyMain.contains(GenCodeConstant.TEMPLATE_WEB_SOYBEAN_MAIN_INDEX));

        List<String> soySimpleJump = TemplateUtils.getTemplateList(TemplateEnum.WEB_SOYBEAN, TplEnum.SIMPLE, PopupTypeEnum.JUMP);
        assertTrue(soySimpleJump.contains(GenCodeConstant.TEMPLATE_WEB_SOYBEAN_SIMPLE_JUMP_EDIT));

        List<String> soySimpleModal = TemplateUtils.getTemplateList(TemplateEnum.WEB_SOYBEAN, TplEnum.SIMPLE, PopupTypeEnum.MODAL);
        assertTrue(soySimpleModal.contains(GenCodeConstant.TEMPLATE_WEB_SOYBEAN_SIMPLE_INDEX));

        // 6. WEB_VBEN5 (TREE, MAIN_SUB, SIMPLE)
        List<String> vben5Tree = TemplateUtils.getTemplateList(TemplateEnum.WEB_VBEN5, TplEnum.TREE, PopupTypeEnum.MODAL);
        assertTrue(vben5Tree.contains(GenCodeConstant.TEMPLATE_WEB_VBEN5_TREE_INDEX));

        List<String> vben5Main = TemplateUtils.getTemplateList(TemplateEnum.WEB_VBEN5, TplEnum.MAIN_SUB, PopupTypeEnum.MODAL);
        assertTrue(vben5Main.contains(GenCodeConstant.TEMPLATE_WEB_VBEN5_MAIN_INDEX));
        assertTrue(vben5Main.contains(GenCodeConstant.TEMPLATE_WEB_VBEN5_MAIN_SUB_INDEX));

        List<String> vben5SimpleJump = TemplateUtils.getTemplateList(TemplateEnum.WEB_VBEN5, TplEnum.SIMPLE, PopupTypeEnum.JUMP);
        assertTrue(vben5SimpleJump.contains(GenCodeConstant.TEMPLATE_WEB_VBEN5_SIMPLE_JUMP_EDIT));

        List<String> vben5SimpleModal = TemplateUtils.getTemplateList(TemplateEnum.WEB_VBEN5, TplEnum.SIMPLE, PopupTypeEnum.MODAL);
        assertTrue(vben5SimpleModal.contains(GenCodeConstant.TEMPLATE_WEB_VBEN5_SIMPLE_INDEX));
    }

    @Test
    @DisplayName("测试 TemplateUtils.getSubTemplateList")
    void testGetSubTemplateList() {
        List<String> subBackend = TemplateUtils.getSubTemplateList(TemplateEnum.BACKEND);
        assertTrue(subBackend.contains(GenCodeConstant.TEMPLATE_MANAGER));
        assertTrue(subBackend.contains(GenCodeConstant.TEMPLATE_ENTITY_JAVA));

        List<String> subOther = TemplateUtils.getSubTemplateList(TemplateEnum.WEB_PLUS);
        assertTrue(subOther.isEmpty());
    }
}
