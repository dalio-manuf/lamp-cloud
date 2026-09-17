package com.dalio.cloud.base.service.user.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import com.dalio.basic.exception.ArgumentException;
import com.dalio.cloud.base.entity.user.BasePosition;
import com.dalio.cloud.base.manager.user.BasePositionManager;
import com.dalio.cloud.base.vo.save.user.BasePositionSaveVO;
import com.dalio.cloud.base.vo.update.user.BasePositionUpdateVO;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * BasePositionServiceImpl 单元测试
 */
class BasePositionServiceImplTest {

    @BeforeAll
    static void init() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, BasePosition.class);
    }

    @Test
    @DisplayName("测试 findByIds, check, saveBefore, updateBefore")
    void testPositionService() {
        BasePositionManager positionManager = mock(BasePositionManager.class);
        BasePositionServiceImpl service = Mockito.spy(new BasePositionServiceImpl());
        ReflectionTestUtils.setField(service, "superManager", positionManager);

        // 1. findByIds
        BasePosition p1 = new BasePosition();
        p1.setId(10L);
        p1.setName("架构师");
        when(positionManager.findByIds(any())).thenReturn(Map.of(10L, p1));
        Map<?, ?> map = service.findByIds(Set.of(10L));
        assertEquals(1, map.size());

        // 2. check
        assertThrows(ArgumentException.class, () -> service.check("", 1L, null));
        when(positionManager.count(any())).thenReturn(1L);
        assertTrue(service.check("架构师", 1L, null));

        when(positionManager.count(any())).thenReturn(0L);
        assertFalse(service.check("前端工程师", 1L, 10L));

        // 3. saveBefore 重名抛异常 / 正常放行
        doReturn(true).when(service).check("重复岗位", 1L, null);
        BasePositionSaveVO saveVO = new BasePositionSaveVO();
        saveVO.setName("重复岗位");
        saveVO.setOrgId(1L);
        assertThrows(ArgumentException.class, () -> service.saveBefore(saveVO));

        doReturn(false).when(service).check("新岗位", 1L, null);
        saveVO.setName("新岗位");
        BasePosition saved = service.saveBefore(saveVO);
        assertNotNull(saved);
        assertEquals("新岗位", saved.getName());

        // 4. updateBefore
        BasePositionUpdateVO updateVO = new BasePositionUpdateVO();
        updateVO.setId(10L);
        updateVO.setName("新岗位");
        updateVO.setOrgId(1L);
        BasePosition updated = service.updateBefore(updateVO);
        assertNotNull(updated);
    }
}
