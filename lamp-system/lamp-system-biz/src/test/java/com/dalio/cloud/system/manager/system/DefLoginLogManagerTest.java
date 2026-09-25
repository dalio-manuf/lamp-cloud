package com.dalio.cloud.system.manager.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dalio.cloud.system.entity.system.DefLoginLog;
import com.dalio.cloud.system.manager.system.impl.DefLoginLogManagerImpl;
import com.dalio.cloud.system.mapper.system.DefLoginLogMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 登录日志管理器单元测试 (验证日志清理安全防御机制)
 */
class DefLoginLogManagerTest {

    @org.junit.jupiter.api.BeforeAll
    static void init() {
        com.baomidou.mybatisplus.core.metadata.TableInfoHelper.initTableInfo(
                new org.apache.ibatis.builder.MapperBuilderAssistant(new com.baomidou.mybatisplus.core.MybatisConfiguration(), ""),
                DefLoginLog.class
        );
    }

    @Test
    @DisplayName("测试 clearLog 无条件清理安全防御：返回 0 且绝不调用 Mapper 删除全表")
    void testClearLogSafetyDefense() {
        DefLoginLogMapper mapper = Mockito.mock(DefLoginLogMapper.class);
        DefLoginLogManagerImpl manager = new DefLoginLogManagerImpl();
        ReflectionTestUtils.setField(manager, "baseMapper", mapper);

        // 1. clearBeforeTime 为空且 clearBeforeNum 为空
        Long result1 = manager.clearLog(null, null);
        assertEquals(0L, result1);
        verify(mapper, never()).clearLog(any(), any(), any());

        // 2. clearBeforeNum <= 0 且 clearBeforeTime 为空
        Long result2 = manager.clearLog(null, 0);
        assertEquals(0L, result2);
        Long result3 = manager.clearLog(null, -10);
        assertEquals(0L, result3);
        verify(mapper, never()).clearLog(any(), any(), any());
    }

    @Test
    @DisplayName("测试 clearLog 指定时间清理")
    void testClearLogByTime() {
        DefLoginLogMapper mapper = Mockito.mock(DefLoginLogMapper.class);
        DefLoginLogManagerImpl manager = new DefLoginLogManagerImpl();
        ReflectionTestUtils.setField(manager, "baseMapper", mapper);

        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        when(mapper.clearLog(eq(threshold), isNull(), isNull())).thenReturn(15L);

        Long deletedCount = manager.clearLog(threshold, null);
        assertEquals(15L, deletedCount);
        verify(mapper).clearLog(eq(threshold), isNull(), isNull());
    }

    @Test
    @DisplayName("测试 clearLog 指定保留条数清理")
    void testClearLogByNum() {
        DefLoginLogMapper mapper = Mockito.mock(DefLoginLogMapper.class);
        DefLoginLogManagerImpl manager = Mockito.spy(new DefLoginLogManagerImpl());
        ReflectionTestUtils.setField(manager, "baseMapper", mapper);

        // 1. 总日志条数未达到保留数量，无需清理
        Page<DefLoginLog> emptyPage = new Page<>(100, 1, false);
        emptyPage.setRecords(Collections.emptyList());
        doReturn(emptyPage).when(manager).page(any(Page.class), any());

        Long resultNoClean = manager.clearLog(null, 100);
        assertEquals(0L, resultNoClean);
        verify(mapper, never()).clearLog(any(), any(), any());

        // 2. 超过保留条数，通过 cutoffId 清理
        DefLoginLog logItem = new DefLoginLog();
        logItem.setId(5000L);
        Page<DefLoginLog> pageWithCutoff = new Page<>(100, 1, false);
        pageWithCutoff.setRecords(List.of(logItem));
        doReturn(pageWithCutoff).when(manager).page(any(Page.class), any());

        when(mapper.clearLog(isNull(), eq(5000L), isNull())).thenReturn(42L);

        Long deletedCount = manager.clearLog(null, 100);
        assertEquals(42L, deletedCount);
        verify(mapper).clearLog(isNull(), eq(5000L), isNull());
    }
}
