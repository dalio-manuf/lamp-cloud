package com.dalio.cloud.msg.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.dalio.basic.base.request.PageParams;
import com.dalio.basic.exception.ArgumentException;
import com.dalio.cloud.msg.entity.ExtendNotice;
import com.dalio.cloud.msg.manager.ExtendNoticeManager;
import com.dalio.cloud.msg.vo.query.ExtendNoticePageQuery;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ExtendNoticeServiceImpl 单元测试
 */
class ExtendNoticeServiceImplTest {

    @BeforeAll
    static void init() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, ExtendNotice.class);
    }

    @Test
    @DisplayName("测试 ExtendNoticeServiceImpl 标记已读、删除与分页方法")
    void testExtendNoticeServiceImplMethods() {
        ExtendNoticeManager noticeManager = mock(ExtendNoticeManager.class);
        ExtendNoticeServiceImpl service = new ExtendNoticeServiceImpl();
        ReflectionTestUtils.setField(service, "superManager", noticeManager);

        // 1. page 方法
        assertNull(service.page(null, new PageParams<ExtendNoticePageQuery>()));

        // 2. mark 空参数
        assertTrue(service.mark(Collections.emptyList(), 1L));
        assertTrue(service.mark(List.of(100L), null));

        // 3. mark 正常逻辑
        when(noticeManager.update(any(Wrapper.class))).thenReturn(true);
        assertTrue(service.mark(List.of(100L, 200L), 1L));
        verify(noticeManager).update(any(Wrapper.class));

        // 4. deleteMyNotice 空参数异常
        assertThrows(ArgumentException.class, () -> service.deleteMyNotice(null));
        assertThrows(ArgumentException.class, () -> service.deleteMyNotice(Collections.emptyList()));

        // 5. deleteMyNotice 正常删除
        when(noticeManager.removeByIds(anyList())).thenReturn(true);
        assertTrue(service.deleteMyNotice(List.of(100L, 200L)));
        verify(noticeManager).removeByIds(List.of(100L, 200L));
    }
}
