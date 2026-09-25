package com.dalio.cloud.file.entity;

import com.dalio.cloud.file.mapper.AppendixMapper;
import com.dalio.cloud.file.service.AppendixService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class AppendixEntityTest {

    @Test
    @DisplayName("测试 Appendix 实体构造、Getter/Setter、Builder、toString、equals")
    void testAppendixEntity() {
        LocalDateTime now = LocalDateTime.now();
        Appendix appendix1 = Appendix.builder()
                .id(1L)
                .bizId(100L)
                .bizType("AVATAR")
                .createdBy(10L)
                .createdTime(now)
                .build();

        assertEquals(1L, appendix1.getId());
        assertEquals(100L, appendix1.getBizId());
        assertEquals("AVATAR", appendix1.getBizType());
        assertEquals(10L, appendix1.getCreatedBy());
        assertEquals(now, appendix1.getCreatedTime());

        Appendix appendix2 = new Appendix();
        appendix2.setId(1L);
        appendix2.setBizId(100L);
        appendix2.setBizType("AVATAR");
        appendix2.setCreatedBy(10L);
        appendix2.setCreatedTime(now);

        assertEquals(appendix1, appendix2);
        assertEquals(appendix1.hashCode(), appendix2.hashCode());
        assertTrue(appendix1.toString().contains("bizId=100"));

        Appendix appendix3 = new Appendix(100L, "BANNER");
        assertEquals(100L, appendix3.getBizId());
        assertEquals("BANNER", appendix3.getBizType());
    }

    @Test
    @DisplayName("测试 AppendixBizKey 与 Mapper 接口契约")
    void testBizKeyAndMapper() {
        AppendixService.AppendixBizKey key1 = AppendixService.AppendixBizKey.builder()
                .bizId(1L)
                .bizType("DOC")
                .build();
        AppendixService.AppendixBizKey key2 = new AppendixService.AppendixBizKey(1L, "DOC");

        assertEquals(key1, key2);
        assertEquals(key1.hashCode(), key2.hashCode());
        assertEquals(1L, key1.getBizId());
        assertEquals("DOC", key1.getBizType());
        assertTrue(key1.toString().contains("bizId=1"));

        assertTrue(AppendixMapper.class.isInterface());
        assertNotNull(AppendixMapper.class.getAnnotations());
    }
}
