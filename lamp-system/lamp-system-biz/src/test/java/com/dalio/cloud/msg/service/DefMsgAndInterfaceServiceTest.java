package com.dalio.cloud.msg.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.dalio.basic.exception.ArgumentException;
import com.dalio.basic.exception.BizException;
import com.dalio.cloud.msg.entity.DefInterface;
import com.dalio.cloud.msg.entity.DefInterfaceProperty;
import com.dalio.cloud.msg.entity.DefMsgTemplate;
import com.dalio.cloud.msg.enumeration.InterfaceExecModeEnum;
import com.dalio.cloud.msg.manager.DefInterfaceManager;
import com.dalio.cloud.msg.manager.DefInterfacePropertyManager;
import com.dalio.cloud.msg.manager.DefMsgTemplateManager;
import com.dalio.cloud.msg.service.impl.DefInterfacePropertyServiceImpl;
import com.dalio.cloud.msg.service.impl.DefInterfaceServiceImpl;
import com.dalio.cloud.msg.service.impl.DefMsgTemplateServiceImpl;
import com.dalio.cloud.msg.vo.save.DefInterfacePropertyBatchSaveVO;
import com.dalio.cloud.msg.vo.save.DefInterfacePropertySaveVO;
import com.dalio.cloud.msg.vo.save.DefInterfaceSaveVO;
import com.dalio.cloud.msg.vo.save.DefMsgTemplateSaveVO;
import com.dalio.cloud.msg.vo.update.DefInterfacePropertyUpdateVO;
import com.dalio.cloud.msg.vo.update.DefInterfaceUpdateVO;
import com.dalio.cloud.msg.vo.update.DefMsgTemplateUpdateVO;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DefMsgAndInterfaceServiceTest {

    @BeforeAll
    static void init() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, DefMsgTemplate.class);
        TableInfoHelper.initTableInfo(assistant, DefInterface.class);
        TableInfoHelper.initTableInfo(assistant, DefInterfaceProperty.class);
    }

    @Test
    @DisplayName("测试 DefMsgTemplateServiceImpl 占位符解析与唯一性校验")
    void testMsgTemplate() {
        DefMsgTemplateManager manager = mock(DefMsgTemplateManager.class);
        DefMsgTemplateServiceImpl service = new DefMsgTemplateServiceImpl();
        ReflectionTestUtils.setField(service, "superManager", manager);

        // 1. getByCode
        when(manager.getByCode("TPL_01")).thenReturn(new DefMsgTemplate());
        assertNotNull(service.getByCode("TPL_01"));

        // 2. check
        assertThrows(ArgumentException.class, () -> service.check("", 1L));
        when(manager.count(any())).thenReturn(1L);
        assertTrue(service.check("TPL_01", 1L));

        // 3. saveBefore 冲突校验
        DefMsgTemplateSaveVO saveVO = new DefMsgTemplateSaveVO();
        saveVO.setCode("TPL_01");
        saveVO.setTitle("验证码: ${code}");
        saveVO.setContent("请在 ${expire} 分钟内使用验证码 ${code}");

        assertThrows(ArgumentException.class, () -> ReflectionTestUtils.invokeMethod(service, "saveBefore", saveVO));

        // 正常 saveBefore 且解析 ${code}, ${expire}
        when(manager.count(any())).thenReturn(0L);
        DefMsgTemplate entity = ReflectionTestUtils.invokeMethod(service, "saveBefore", saveVO);
        assertNotNull(entity);
        assertTrue(saveVO.getParam().contains("code"));
        assertTrue(saveVO.getParam().contains("expire"));

        // 4. updateBefore
        DefMsgTemplateUpdateVO updateVO = new DefMsgTemplateUpdateVO();
        updateVO.setId(10L);
        updateVO.setCode("TPL_02");
        updateVO.setTitle("通知: ${title}");
        updateVO.setContent("内容: ${content}");
        DefMsgTemplate updated = ReflectionTestUtils.invokeMethod(service, "updateBefore", updateVO);
        assertNotNull(updated);
        assertTrue(updateVO.getParam().contains("title"));
    }

    @Test
    @DisplayName("测试 DefInterfaceServiceImpl 执行模式校验与级联删除")
    void testInterfaceService() {
        DefInterfaceManager manager = mock(DefInterfaceManager.class);
        DefInterfacePropertyManager propertyManager = mock(DefInterfacePropertyManager.class);

        DefInterfaceServiceImpl service = new DefInterfaceServiceImpl(propertyManager);
        ReflectionTestUtils.setField(service, "superManager", manager);

        // 1. check
        assertThrows(ArgumentException.class, () -> service.check("", 1L));
        when(manager.count(any())).thenReturn(0L);
        assertFalse(service.check("INT_01", 1L));

        // 2. saveBefore - IMPL_CLASS 模式缺少类名
        DefInterfaceSaveVO saveVO = new DefInterfaceSaveVO();
        saveVO.setCode("INT_01");
        saveVO.setExecMode(InterfaceExecModeEnum.IMPL_CLASS.getCode());
        assertThrows(ArgumentException.class, () -> ReflectionTestUtils.invokeMethod(service, "saveBefore", saveVO));

        saveVO.setImplClass("com.dalio.cloud.SmsService");
        DefInterface entity = ReflectionTestUtils.invokeMethod(service, "saveBefore", saveVO);
        assertNotNull(entity);

        // SCRIPT 模式缺少脚本
        saveVO.setExecMode(InterfaceExecModeEnum.SCRIPT.getCode());
        assertThrows(ArgumentException.class, () -> ReflectionTestUtils.invokeMethod(service, "saveBefore", saveVO));
        saveVO.setScript("print('hello')");
        assertNotNull(ReflectionTestUtils.invokeMethod(service, "saveBefore", saveVO));

        // 3. updateBefore
        DefInterfaceUpdateVO updateVO = new DefInterfaceUpdateVO();
        updateVO.setId(10L);
        updateVO.setCode("INT_02");
        updateVO.setExecMode(InterfaceExecModeEnum.SCRIPT.getCode());
        updateVO.setScript("return 1;");
        assertNotNull(ReflectionTestUtils.invokeMethod(service, "updateBefore", updateVO));

        // 4. removeByIds
        when(manager.removeByIds(anyCollection())).thenReturn(true);
        assertTrue(service.removeByIds(List.of(10L)));
        verify(propertyManager).remove(any(com.baomidou.mybatisplus.core.conditions.Wrapper.class));
    }

    @Test
    @DisplayName("测试 DefInterfacePropertyServiceImpl 批量保存与参数冲突校验")
    void testInterfacePropertyService() {
        DefInterfacePropertyManager manager = mock(DefInterfacePropertyManager.class);
        DefInterfacePropertyServiceImpl service = new DefInterfacePropertyServiceImpl();
        ReflectionTestUtils.setField(service, "superManager", manager);

        // 1. listByInterfaceId
        when(manager.listByInterfaceId(10L)).thenReturn(Map.of("apiKey", "123"));
        assertEquals(1, service.listByInterfaceId(10L).size());

        // 2. batchSave - 键重复抛异常
        DefInterfacePropertyBatchSaveVO batchVO = new DefInterfacePropertyBatchSaveVO();
        DefInterfacePropertySaveVO saveVO1 = new DefInterfacePropertySaveVO();
        saveVO1.setKey("dupKey");
        DefInterfacePropertySaveVO saveVO2 = new DefInterfacePropertySaveVO();
        saveVO2.setKey("dupKey");

        batchVO.setInsertRecords(List.of(saveVO1, saveVO2));
        batchVO.setUpdateRecords(List.of());
        batchVO.setRemoveRecords(List.of());
        batchVO.setPendingRecords(List.of());

        assertThrows(BizException.class, () -> service.batchSave(batchVO));

        // 3. 正常批处理保存与删除
        saveVO2.setKey("key2");
        DefInterfacePropertyUpdateVO updateVO1 = new DefInterfacePropertyUpdateVO();
        updateVO1.setId(100L);
        updateVO1.setKey("upKey1");

        DefInterfacePropertyUpdateVO removeVO1 = new DefInterfacePropertyUpdateVO();
        removeVO1.setId(200L);

        DefInterfacePropertyUpdateVO pendingVO1 = new DefInterfacePropertyUpdateVO();
        pendingVO1.setId(300L);

        batchVO.setInsertRecords(List.of(saveVO1, saveVO2));
        batchVO.setUpdateRecords(List.of(updateVO1));
        batchVO.setRemoveRecords(List.of(removeVO1));
        batchVO.setPendingRecords(List.of(pendingVO1));

        Boolean res = service.batchSave(batchVO);
        assertTrue(res);
        verify(manager, atLeast(2)).removeByIds(anyCollection());
        verify(manager).updateBatchById(anyList());
        verify(manager).saveBatch(anyList());
    }
}
