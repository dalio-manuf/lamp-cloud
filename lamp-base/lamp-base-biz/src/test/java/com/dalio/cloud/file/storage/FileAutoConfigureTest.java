package com.dalio.cloud.file.storage;

import com.qiniu.storage.BucketManager;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import io.minio.MinioClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.dalio.cloud.file.properties.FileServerProperties;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * FileAutoConfigure 单元测试
 */
class FileAutoConfigureTest {

    @Test
    @DisplayName("测试 FileAutoConfigure 创建各项存储 Client 与 Manager Bean")
    void testFileAutoConfigureBeans() {
        FileServerProperties properties = new FileServerProperties();
        properties.getMinIo().setEndpoint("http://127.0.0.1:9000");
        properties.getMinIo().setAccessKey("admin");
        properties.getMinIo().setSecretKey("password");

        FileAutoConfigure configure = new FileAutoConfigure(properties);

        MinioClient minioClient = configure.minioClient(properties);
        assertNotNull(minioClient);

        // 测试各种七牛 Zone 配置
        FileServerProperties.Region[] regions = {
                FileServerProperties.Region.z0,
                FileServerProperties.Region.z1,
                FileServerProperties.Region.z2,
                FileServerProperties.Region.na0,
                FileServerProperties.Region.as0
        };

        for (FileServerProperties.Region region : regions) {
            properties.getQiNiu().setZone(region);
            com.qiniu.storage.Configuration qiNiuConfig = configure.qiNiuConfig();
            assertNotNull(qiNiuConfig);
        }

        UploadManager uploadManager = configure.uploadManager();
        assertNotNull(uploadManager);

        Auth auth = configure.getQiniuAuth();
        assertNotNull(auth);

        BucketManager bucketManager = configure.bucketManager();
        assertNotNull(bucketManager);
    }
}
