package com.dalio.cloud.base.manager.system;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import com.dalio.cloud.base.entity.system.BaseOperationLog;
import com.dalio.cloud.base.manager.system.impl.BaseOperationLogManagerImpl;
import com.dalio.cloud.base.mapper.system.BaseOperationLogExtMapper;
import com.dalio.cloud.base.mapper.system.BaseOperationLogMapper;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 操作日志管理器单元测试 (验证安全防御及日志清理)
 */
class BaseOperationLogManagerTest {

    @BeforeAll
    static void init() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                BaseOperationLog.class
        );
    }

    @Test
    @DisplayName("测试 clearLog 无条件删除安全防御：参数皆空时直接返回0且不调用Mapper")
    void testClearLogSafetyDefense() {
        BaseOperationLogMapper mapper = Mockito.mock(BaseOperationLogMapper.class);
        BaseOperationLogExtMapper extMapper = Mockito.mock(BaseOperationLogExtMapper.class);

        BaseOperationLogManagerImpl manager = new BaseOperationLogManagerImpl(extMapper);
        ReflectionTestUtils.setField(manager, "baseMapper", mapper);

        // 1. clearBeforeTime 为空且 clearBeforeNum 为空
        Long res1 = manager.clearLog(null, null);
        assertEquals(0L, res1);
        verify(mapper, never()).clearLog(any(), any());
        verify(extMapper, never()).clearLog(any(), any());

        // 2. clearBeforeNum <= 0 且 clearBeforeTime 为空
        Long res2 = manager.clearLog(null, 0);
        assertEquals(0L, res2);
        Long res3 = manager.clearLog(null, -5);
        assertEquals(0L, res3);
        verify(mapper, never()).clearLog(any(), any());
        verify(extMapper, never()).clearLog(any(), any());
    }

    @Test
    @DisplayName("测试 clearLog 仅指定时间清理")
    void testClearLogByTimeOnly() {
        BaseOperationLogMapper mapper = Mockito.mock(BaseOperationLogMapper.class);
        BaseOperationLogExtMapper extMapper = Mockito.mock(BaseOperationLogExtMapper.class);

        BaseOperationLogManagerImpl manager = new BaseOperationLogManagerImpl(extMapper);
        ReflectionTestUtils.setField(manager, "baseMapper", mapper);

        LocalDateTime threshold = LocalDateTime.now().minusDays(15);
        when(baseMapperMock(mapper).clearLog(eq(threshold), anyList())).thenReturn(20L);

        Long deleted = manager.clearLog(threshold, null);
        assertEquals(20L, deleted);
        verify(extMapper).clearLog(eq(threshold), eq(Collections.emptyList()));
        verify(mapper).clearLog(eq(threshold), eq(Collections.emptyList()));
    }

    @Test
    @DisplayName("测试 clearLog 指定保留条数清理")
    void testClearLogByNum() {
        BaseOperationLogMapper mapper = Mockito.mock(BaseOperationLogMapper.class);
        BaseOperationLogExtMapper extMapper = Mockito.mock(BaseOperationLogExtMapper.class);

        BaseOperationLogManagerImpl manager = Mockito.spy(new BaseOperationLogManagerImpl(extMapper));
        ReflectionTestUtils.setField(manager, "baseMapper", mapper);

        // 1. 条数不足，无需清理
        Page<BaseOperationLog> emptyPage = new Page<>(1, 50);
        emptyPage.setRecords(Collections.emptyList());
        doReturn(emptyPage).when(manager).page(any(Page.class), any());

        Long resEmpty = manager.clearLog(null, 50);
        assertEquals(0L, resEmpty);
        verify(mapper, never()).clearLog(any(), any());

        // 2. 超过条数，清理
        BaseOperationLog log1 = new BaseOperationLog();
        log1.setId(101L);
        Page<BaseOperationLog> pageWithRecords = new Page<>(1, 50);
        pageWithRecords.setRecords(List.of(log1));
        doReturn(pageWithRecords).when(manager).page(any(Page.class), any());

        when(mapper.clearLog(isNull(), eq(List.of(101L)))).thenReturn(5L);

        Long resClean = manager.clearLog(null, 50);
        assertEquals(5L, resClean);
        verify(extMapper).clearLog(isNull(), eq(List.of(101L)));
        verify(mapper).clearLog(isNull(), eq(List.of(101L)));
    }

    private BaseOperationLogMapper baseMapperMock(BaseOperationLogMapper mapper) {
        return mapper;
    }
}
