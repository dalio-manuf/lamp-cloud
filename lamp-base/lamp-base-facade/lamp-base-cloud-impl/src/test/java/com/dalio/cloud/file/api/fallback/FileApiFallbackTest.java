package com.dalio.cloud.file.api.fallback;

import com.dalio.basic.base.R;
import com.dalio.cloud.file.enumeration.FileStorageType;
import com.dalio.cloud.file.vo.result.FileResultVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * FileApiFallback 单元测试
 *
 * @author went
 */
class FileApiFallbackTest {

    @Test
    @DisplayName("测试 FileApiFallback.upload 熔断返回 timeout")
    void testUploadFallback() {
        FileApiFallback fallback = new FileApiFallback();
        MultipartFile file = new MockMultipartFile("test.txt", "content".getBytes());

        R<FileResultVO> result = fallback.upload(file, "testBiz", "bucket1", FileStorageType.LOCAL);

        assertNotNull(result);
        assertEquals(R.TIMEOUT_CODE, result.getCode());
    }
}
