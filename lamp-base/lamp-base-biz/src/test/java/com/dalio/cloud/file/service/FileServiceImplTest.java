package com.dalio.cloud.file.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.dalio.basic.exception.ArgumentException;
import com.dalio.basic.exception.BizException;
import com.dalio.cloud.file.entity.File;
import com.dalio.cloud.file.manager.FileManager;
import com.dalio.cloud.file.properties.FileServerProperties;
import com.dalio.cloud.file.service.impl.FileServiceImpl;
import com.dalio.cloud.file.strategy.FileContext;
import com.dalio.cloud.file.vo.param.FileUploadVO;
import com.dalio.cloud.file.vo.result.FileResultVO;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class FileServiceImplTest {

    @BeforeAll
    static void init() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, File.class);
    }

    @Test
    @DisplayName("测试 listByBizIdAndBizType 参数校验与查询")
    void testListByBizIdAndBizType() {
        FileContext fileContext = mock(FileContext.class);
        FileServerProperties properties = new FileServerProperties();
        FileManager fileManager = mock(FileManager.class);

        FileServiceImpl service = new FileServiceImpl(fileContext, properties);
        ReflectionTestUtils.setField(service, "superManager", fileManager);

        assertThrows(ArgumentException.class, () -> service.listByBizIdAndBizType(null, "avatar"));

        when(fileManager.listByBizIdAndBizType(1L, "avatar")).thenReturn(List.of(new FileResultVO()));
        assertEquals(1, service.listByBizIdAndBizType(1L, "avatar").size());
    }

    @Test
    @DisplayName("测试 upload 文件校验、格式校验、路径安全校验与保存")
    void testUpload() {
        FileContext fileContext = mock(FileContext.class);
        FileServerProperties properties = mock(FileServerProperties.class);
        FileManager fileManager = mock(FileManager.class);

        FileServiceImpl service = new FileServiceImpl(fileContext, properties);
        ReflectionTestUtils.setField(service, "superManager", fileManager);

        // 1. 空文件
        MockMultipartFile emptyFile = new MockMultipartFile("file", "test.png", "image/png", new byte[0]);
        assertThrows(BizException.class, () -> service.upload(emptyFile, new FileUploadVO()));

        // 2. 后缀不支持
        MockMultipartFile fileWithData = new MockMultipartFile("file", "test.exe", "application/x-msdownload", new byte[]{1, 2});
        when(properties.validSuffix("test.exe")).thenReturn(false);
        assertThrows(BizException.class, () -> service.upload(fileWithData, new FileUploadVO()));

        // 3. 路径穿越 ../
        MockMultipartFile hackFile = new MockMultipartFile("file", "../../../etc/passwd.png", "image/png", new byte[]{1, 2});
        when(properties.validSuffix("../../../etc/passwd.png")).thenReturn(true);
        assertThrows(BizException.class, () -> service.upload(hackFile, new FileUploadVO()));

        // 4. 正常上传
        MockMultipartFile validFile = new MockMultipartFile("file", "avatar.png", "image/png", new byte[]{1, 2, 3});
        when(properties.validSuffix("avatar.png")).thenReturn(true);

        File created = new File();
        created.setId(100L);
        created.setOriginalFileName("avatar.png");
        when(fileContext.upload(any(), any())).thenReturn(created);

        FileResultVO vo = service.upload(validFile, new FileUploadVO());
        assertNotNull(vo);
        assertEquals(100L, vo.getId());
        verify(fileManager).save(created);
    }

    @Test
    @DisplayName("测试 findUrlByPath, findUrlById, removeByIds")
    void testFindUrlAndRemove() {
        FileContext fileContext = mock(FileContext.class);
        FileServerProperties properties = new FileServerProperties();
        FileManager fileManager = mock(FileManager.class);

        FileServiceImpl service = new FileServiceImpl(fileContext, properties);
        ReflectionTestUtils.setField(service, "superManager", fileManager);

        // 1. findUrlByPath
        assertTrue(service.findUrlByPath(Collections.emptyList()).isEmpty());
        when(fileContext.findUrlByPath(anyList())).thenReturn(Map.of("/path.png", "http://url"));
        assertEquals(1, service.findUrlByPath(List.of("/path.png")).size());

        // 2. findUrlById
        assertTrue(service.findUrlById(Collections.emptyList()).isEmpty());
        when(fileContext.findUrlById(anyList())).thenReturn(Map.of(1L, "http://url"));
        assertEquals(1, service.findUrlById(List.of(1L)).size());

        // 3. removeByIds
        assertFalse(service.removeByIds(Collections.emptyList()));

        when(fileManager.listByIds(anyList())).thenReturn(Collections.emptyList());
        assertFalse(service.removeByIds(List.of(1L)));

        File f = new File();
        f.setId(1L);
        when(fileManager.listByIds(anyList())).thenReturn(List.of(f));
        when(fileContext.delete(anyList())).thenReturn(true);

        assertTrue(service.removeByIds(List.of(1L)));
        verify(fileManager).removeByIds(List.of(1L));
        verify(fileContext).delete(List.of(f));
    }

    @Test
    @DisplayName("测试 download 单文件与多文件")
    void testDownload() throws Exception {
        FileContext fileContext = mock(FileContext.class);
        FileServerProperties properties = new FileServerProperties();
        FileManager fileManager = mock(FileManager.class);

        FileServiceImpl service = new FileServiceImpl(fileContext, properties);
        ReflectionTestUtils.setField(service, "superManager", fileManager);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // 1. 多文件为空报错
        when(fileManager.listByIds(anyList())).thenReturn(Collections.emptyList());
        assertThrows(ArgumentException.class, () -> service.download(request, response, List.of(1L)));

        // 2. 多文件存在
        File f = new File();
        f.setId(1L);
        when(fileManager.listByIds(anyList())).thenReturn(List.of(f));
        service.download(request, response, List.of(1L));
        verify(fileContext).download(eq(request), eq(response), eq(List.of(f)));

        // 3. 单文件为空报错
        when(fileManager.getById(99L)).thenReturn(null);
        assertThrows(ArgumentException.class, () -> service.download(request, response, 99L));

        // 4. 单文件存在
        when(fileManager.getById(1L)).thenReturn(f);
        service.download(request, response, 1L);
        verify(fileContext).download(eq(request), eq(response), eq(f));
    }
}
