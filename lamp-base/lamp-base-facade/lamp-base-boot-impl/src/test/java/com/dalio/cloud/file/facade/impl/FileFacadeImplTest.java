package com.dalio.cloud.file.facade.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import com.dalio.cloud.file.enumeration.FileStorageType;
import com.dalio.cloud.file.service.FileService;
import com.dalio.cloud.file.vo.param.FileUploadVO;
import com.dalio.cloud.file.vo.result.FileResultVO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 单体版 FileFacadeImpl 测试
 *
 * @author went
 */
@ExtendWith(MockitoExtension.class)
class FileFacadeImplTest {

    @Mock
    private FileService fileService;

    @InjectMocks
    private FileFacadeImpl fileFacade;

    @Test
    @DisplayName("测试单体版 upload 文件接口")
    void testUpload() {
        MultipartFile file = new MockMultipartFile("test.txt", "test.txt", "text/plain", "hello".getBytes());
        FileResultVO expected = new FileResultVO();
        expected.setId(100L);
        expected.setOriginalFileName("test.txt");

        when(fileService.upload(eq(file), any(FileUploadVO.class))).thenReturn(expected);

        FileResultVO result = fileFacade.upload(file, "order", "bucket1", FileStorageType.LOCAL);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        verify(fileService).upload(eq(file), any(FileUploadVO.class));
    }
}
