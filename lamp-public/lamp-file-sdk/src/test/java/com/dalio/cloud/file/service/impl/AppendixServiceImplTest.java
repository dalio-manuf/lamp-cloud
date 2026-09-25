package com.dalio.cloud.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dalio.basic.base.entity.SuperEntity;
import com.dalio.basic.interfaces.echo.EchoVO;
import com.dalio.cloud.file.entity.Appendix;
import com.dalio.cloud.file.mapper.AppendixMapper;
import com.dalio.cloud.file.service.AppendixService;
import com.dalio.cloud.model.vo.result.AppendixResultVO;
import com.dalio.cloud.model.vo.save.AppendixSaveVO;
import com.google.common.collect.Multimap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AppendixServiceImplTest {

    @Mock
    private AppendixMapper appendixMapper;

    @Spy
    @InjectMocks
    private AppendixServiceImpl appendixService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(appendixService, "baseMapper", appendixMapper);
    }

    @Test
    @DisplayName("测试 echoAppendix 方法")
    void testEchoAppendix() {
        assertDoesNotThrow(() -> appendixService.echoAppendix((IPage<TestEchoEntity>) null, "AVATAR"));
        assertDoesNotThrow(() -> appendixService.echoAppendix(Collections.emptyList(), "AVATAR"));

        TestEchoEntity entity = new TestEchoEntity(100L);
        IPage<TestEchoEntity> page = new Page<>(1, 10);
        page.setRecords(Collections.singletonList(entity));

        Appendix appendix = new Appendix();
        appendix.setId(1L);
        appendix.setBizId(100L);
        appendix.setBizType("AVATAR");

        doReturn(Collections.singletonList(appendix)).when(appendixService).list(any(Wrapper.class));

        appendixService.echoAppendix(page, "AVATAR");

        assertTrue(entity.getEchoMap().containsKey("AVATAR"));
        Object coll = entity.getEchoMap().get("AVATAR");
        assertTrue(coll instanceof Collection);
        assertEquals(1, ((Collection<?>) coll).size());
    }

    @Test
    @DisplayName("测试 listByBizId 与 listByBizIds 校验与执行")
    void testListByBizIdAndIds() {
        assertThrows(RuntimeException.class, () -> appendixService.listByBizId(null, "AVATAR"));
        assertThrows(RuntimeException.class, () -> appendixService.listByBizIds(Collections.emptyList(), "AVATAR"));

        Appendix appendix = new Appendix();
        appendix.setId(1L);
        appendix.setBizId(100L);
        appendix.setBizType("AVATAR");

        doReturn(Collections.singletonList(appendix)).when(appendixService).list(any(Wrapper.class));

        Multimap<AppendixService.AppendixBizKey, AppendixResultVO> map1 = appendixService.listByBizId(100L, "AVATAR");
        assertEquals(1, map1.size());

        Multimap<AppendixService.AppendixBizKey, AppendixResultVO> map2 = appendixService.listByBizIds(Collections.singletonList(100L), "AVATAR");
        assertEquals(1, map2.size());
    }

    @Test
    @DisplayName("测试 listByBizIdAndBizType 查询")
    void testListByBizIdAndBizType() {
        assertThrows(RuntimeException.class, () -> appendixService.listByBizIdAndBizType(null, "AVATAR"));

        Appendix appendix = new Appendix();
        appendix.setId(1L);
        appendix.setBizId(100L);
        appendix.setBizType("AVATAR");

        when(appendixMapper.selectList(any())).thenReturn(Collections.singletonList(appendix));

        List<AppendixResultVO> list = appendixService.listByBizIdAndBizType(100L, "AVATAR");
        assertEquals(1, list.size());
        assertEquals(1L, list.get(0).getId());
    }

    @Test
    @DisplayName("测试 getByBiz 查询单条数据")
    void testGetByBiz() {
        assertThrows(RuntimeException.class, () -> appendixService.getByBiz(null, "AVATAR"));
        assertThrows(RuntimeException.class, () -> appendixService.getByBiz(100L, ""));

        doReturn(Collections.emptyList()).when(appendixService).list(any(Wrapper.class));
        assertNull(appendixService.getByBiz(100L, "AVATAR"));

        Appendix appendix = new Appendix();
        appendix.setId(1L);
        appendix.setBizId(100L);
        appendix.setBizType("AVATAR");

        doReturn(Collections.singletonList(appendix)).when(appendixService).list(any(Wrapper.class));
        AppendixResultVO result = appendixService.getByBiz(100L, "AVATAR");
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("测试 save 方法各分支")
    void testSave() {
        assertTrue(appendixService.save((List<AppendixSaveVO>) null));
        assertTrue(appendixService.save(Collections.emptyList()));

        AppendixSaveVO saveVO = new AppendixSaveVO();
        saveVO.setBizId(100L);
        AppendixSaveVO.TypeFile typeFile = new AppendixSaveVO.TypeFile();
        typeFile.setBizType("DOC");
        typeFile.setFileIdList(Arrays.asList(1L, 2L));
        saveVO.setTypeFiles(Collections.singletonList(typeFile));

        doReturn(true).when(appendixService).remove(any(Wrapper.class));
        doReturn(true).when(appendixService).saveBatch(any());

        assertTrue(appendixService.save(saveVO));

        AppendixSaveVO emptyFileVO = new AppendixSaveVO();
        emptyFileVO.setBizId(100L);
        emptyFileVO.setTypeFiles((List<AppendixSaveVO.TypeFile>) null);
        assertTrue(appendixService.save(Collections.singletonList(emptyFileVO)));
    }

    @Test
    @DisplayName("测试 removeByBiz 方法各分支")
    void testRemoveByBiz() {
        assertDoesNotThrow(() -> appendixService.removeByBiz(null));
        assertDoesNotThrow(() -> appendixService.removeByBiz(Collections.emptyList()));

        AppendixSaveVO noBizId = new AppendixSaveVO();
        assertDoesNotThrow(() -> appendixService.removeByBiz(Collections.singletonList(noBizId)));

        AppendixSaveVO noTypes = new AppendixSaveVO();
        noTypes.setBizId(100L);
        noTypes.setTypeFiles(Collections.emptyList());
        assertDoesNotThrow(() -> appendixService.removeByBiz(Collections.singletonList(noTypes)));

        AppendixSaveVO valid = new AppendixSaveVO();
        valid.setBizId(100L);
        AppendixSaveVO.TypeFile tf = new AppendixSaveVO.TypeFile();
        tf.setBizType("DOC");
        valid.setTypeFiles(Collections.singletonList(tf));

        doReturn(true).when(appendixService).remove(any(Wrapper.class));
        assertDoesNotThrow(() -> appendixService.removeByBiz(Collections.singletonList(valid)));
    }

    @Test
    @DisplayName("测试 removeByBizId 方法")
    void testRemoveByBizId() {
        assertFalse(appendixService.removeByBizId(null, "DOC"));
        assertFalse(appendixService.removeByBizId(Collections.emptyList(), "DOC"));
        assertFalse(appendixService.removeByBizId(Collections.singletonList(100L), ""));

        doReturn(true).when(appendixService).remove(any(Wrapper.class));
        assertTrue(appendixService.removeByBizId(Collections.singletonList(100L), "DOC"));
    }

    @Test
    @DisplayName("测试 buildBiz 默认接口方法")
    void testBuildBiz() {
        AppendixService.AppendixBizKey key = appendixService.buildBiz(100L, "TYPE_A");
        assertNotNull(key);
        assertEquals(100L, key.getBizId());
        assertEquals("TYPE_A", key.getBizType());
    }

    public static class TestEchoEntity extends SuperEntity<Long> implements EchoVO {
        private final Map<String, Object> echoMap = new HashMap<>();

        public TestEchoEntity(Long id) {
            this.id = id;
        }

        @Override
        public Map<String, Object> getEchoMap() {
            return echoMap;
        }
    }
}
