package com.dalio.cloud.file.facade.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import com.dalio.basic.base.R;
import com.dalio.cloud.file.api.FileApi;
import com.dalio.cloud.file.enumeration.FileStorageType;
import com.dalio.cloud.file.vo.result.FileResultVO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 微服务版 FileFacadeImpl 测试
 *
 * @author went
 */
@ExtendWith(MockitoExtension.class)
class FileFacadeImplTest {

    @Mock
    private FileApi fileApi;

    @InjectMocks
    private FileFacadeImpl fileFacade;

    @Test
    @DisplayName("测试 upload 成功")
    void testUploadSuccess() {
        MultipartFile file = new MockMultipartFile("f.txt", "f.txt", "text/plain", "abc".getBytes());
        FileResultVO vo = new FileResultVO();
        vo.setId(200L);

        when(fileApi.upload(file, "order", "bucket1", FileStorageType.LOCAL)).thenReturn(R.success(vo));

        FileResultVO result = fileFacade.upload(file, "order", "bucket1", FileStorageType.LOCAL);

        assertNotNull(result);
        assertEquals(200L, result.getId());
        verify(fileApi).upload(file, "order", "bucket1", FileStorageType.LOCAL);
    }

    @Test
    @DisplayName("测试 upload 失败返回 null")
    void testUploadFailure() {
        MultipartFile file = new MockMultipartFile("f.txt", "f.txt", "text/plain", "abc".getBytes());

        when(fileApi.upload(file, "order", "bucket1", FileStorageType.LOCAL)).thenReturn(R.fail("upload error"));

        FileResultVO result = fileFacade.upload(file, "order", "bucket1", FileStorageType.LOCAL);

        assertNull(result);
    }

    @Test
    @DisplayName("测试 upload 返回 null 响应")
    void testUploadNullResponse() {
        MultipartFile file = new MockMultipartFile("f.txt", "f.txt", "text/plain", "abc".getBytes());

        when(fileApi.upload(file, "order", "bucket1", FileStorageType.LOCAL)).thenReturn(null);

        FileResultVO result = fileFacade.upload(file, "order", "bucket1", FileStorageType.LOCAL);

        assertNull(result);
    }
}
