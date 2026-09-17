package com.dalio.cloud.file.strategy;

import cn.hutool.core.io.FileUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.CompleteMultipartUploadResult;
import com.aliyun.oss.model.InitiateMultipartUploadResult;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.UploadPartCopyResult;
import com.aliyun.oss.model.UploadPartResult;
import com.obs.services.ObsClient;
import com.obs.services.model.TemporarySignatureResponse;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import io.minio.MinioClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import com.dalio.basic.base.R;
import com.dalio.cloud.file.domain.FileDeleteBO;
import com.dalio.cloud.file.domain.FileGetUrlBO;
import com.dalio.cloud.file.dto.chunk.FileChunksMergeDTO;
import com.dalio.cloud.file.entity.File;
import com.dalio.cloud.file.enumeration.FileStorageType;
import com.dalio.cloud.file.mapper.FileMapper;
import com.dalio.cloud.file.properties.FileServerProperties;
import com.dalio.cloud.file.strategy.impl.ali.AliFileChunkStrategyImpl;
import com.dalio.cloud.file.strategy.impl.huawei.HuaweiFileStrategyImpl;
import com.dalio.cloud.file.strategy.impl.local.LocalFileChunkStrategyImpl;
import com.dalio.cloud.file.strategy.impl.local.LocalFileStrategyImpl;
import com.dalio.cloud.file.strategy.impl.minio.MinIoFileStrategyImpl;
import com.dalio.cloud.file.strategy.impl.qiniu.QiNiuFileStrategyImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 各种存储策略与分片策略单元测试
 */
class StorageStrategiesTest {

    static class TestAliFileChunkStrategy extends AliFileChunkStrategyImpl {
        public TestAliFileChunkStrategy(FileMapper fileMapper, FileServerProperties fileProperties) {
            super(fileMapper, fileProperties);
        }

        @Override
        public R<File> merge(List<java.io.File> files, String path, String fileName, FileChunksMergeDTO info) throws IOException {
            return super.merge(files, path, fileName, info);
        }
    }

    @Test
    @DisplayName("测试 LocalFileStrategyImpl 本地上传、删除与查找URL")
    void testLocalFileStrategy(@TempDir Path tempDir) throws Exception {
        FileServerProperties properties = new FileServerProperties();
        FileServerProperties.Local local = new FileServerProperties.Local();
        local.setBucket("default-bucket");
        local.setStoragePath(tempDir.toString());
        local.setUrlPrefix("http://localhost:8080/files/");
        properties.setLocal(local);

        FileMapper fileMapper = mock(FileMapper.class);
        LocalFileStrategyImpl strategy = new LocalFileStrategyImpl(properties, fileMapper);

        MockMultipartFile multipartFile = new MockMultipartFile("file", "pic.png", "image/png", "test content".getBytes());

        // 1. 上传
        File file = strategy.upload(multipartFile, "my-bucket", "user-avatar");
        assertNotNull(file);
        assertEquals(FileStorageType.LOCAL, file.getStorageType());
        assertTrue(file.getUrl().contains("my-bucket"));
        assertTrue(Files.exists(Paths.get(tempDir.toString(), "my-bucket", file.getPath())));

        // 2. 查 URL
        FileGetUrlBO getUrlBO = FileGetUrlBO.builder().bucket("my-bucket").path(file.getPath()).build();
        Map<String, String> urlMap = strategy.findUrl(List.of(getUrlBO));
        assertEquals(1, urlMap.size());
        assertTrue(urlMap.get(file.getPath()).startsWith("http://localhost:8080/files/"));

        // 3. 删除
        FileDeleteBO deleteBO = FileDeleteBO.builder().bucket("my-bucket").path(file.getPath()).build();
        assertTrue(strategy.delete(deleteBO));
        assertFalse(Files.exists(Paths.get(tempDir.toString(), "my-bucket", file.getPath())));
    }

    @Test
    @DisplayName("测试 MinIoFileStrategyImpl 上传、删除、签名及公开Bucket URL")
    void testMinIoFileStrategy() throws Exception {
        FileServerProperties properties = new FileServerProperties();
        FileServerProperties.MinIo minIo = new FileServerProperties.MinIo();
        minIo.setBucket("minio-bucket");
        minIo.setEndpoint("http://minio:9000/");
        minIo.setExpiry(3600);
        properties.setMinIo(minIo);
        properties.setPublicBucket(Set.of("public-bucket"));

        MinioClient minioClient = mock(MinioClient.class);
        FileMapper fileMapper = mock(FileMapper.class);

        MinIoFileStrategyImpl strategy = new MinIoFileStrategyImpl(properties, minioClient, fileMapper);

        when(minioClient.bucketExists(any())).thenReturn(false);
        when(minioClient.getPresignedObjectUrl(any())).thenReturn("http://minio:9000/presigned");

        MockMultipartFile multipartFile = new MockMultipartFile("file", "doc.pdf", "application/pdf", "pdf bytes".getBytes());

        // 1. 上传
        File file = strategy.upload(multipartFile, "minio-bucket", "docs");
        assertNotNull(file);
        assertEquals(FileStorageType.MIN_IO, file.getStorageType());
        verify(minioClient).makeBucket(any());
        verify(minioClient).putObject(any());

        // 2. 查 URL (含公开与私有)
        FileGetUrlBO bo1 = FileGetUrlBO.builder().bucket("public-bucket").path("pub.png").build();
        FileGetUrlBO bo2 = FileGetUrlBO.builder().bucket("minio-bucket").path("pri.png").build();
        Map<String, String> urlMap = strategy.findUrl(List.of(bo1, bo2));
        assertEquals(2, urlMap.size());
        assertTrue(urlMap.get("pub.png").contains("public-bucket"));
        assertEquals("http://minio:9000/presigned", urlMap.get("pri.png"));

        // 3. 删除
        FileDeleteBO delBO = FileDeleteBO.builder().bucket("minio-bucket").path("pri.png").build();
        assertTrue(strategy.delete(delBO));
        verify(minioClient).removeObject(any());
    }

    @Test
    @DisplayName("测试 HuaweiFileStrategyImpl 华为OBS上传、删除与签名URL")
    void testHuaweiFileStrategy() throws Exception {
        FileServerProperties properties = new FileServerProperties();
        FileServerProperties.Huawei huawei = new FileServerProperties.Huawei();
        huawei.setBucket("obs-bucket");
        huawei.setAccessKey("ak");
        huawei.setSecretKey("sk");
        huawei.setEndpoint("obs.cn-north-4.myhuaweicloud.com");
        huawei.setUrlPrefix("https://obs-bucket.obs.cn-north-4.myhuaweicloud.com/");
        huawei.setExpiry(1800);
        properties.setHuawei(huawei);
        properties.setPublicBucket(Set.of("public-obs"));

        FileMapper fileMapper = mock(FileMapper.class);
        HuaweiFileStrategyImpl strategy = new HuaweiFileStrategyImpl(properties, fileMapper);

        ObsClient mockObs = mock(ObsClient.class);
        TemporarySignatureResponse sigRes = mock(TemporarySignatureResponse.class);
        when(sigRes.getSignedUrl()).thenReturn("https://obs.signed/url");
        when(mockObs.createTemporarySignature(any())).thenReturn(sigRes);

        try (MockedConstruction<ObsClient> mocked = Mockito.mockConstruction(ObsClient.class,
                (client, context) -> {
                    when(client.createTemporarySignature(any())).thenReturn(sigRes);
                })) {

            MockMultipartFile multipartFile = new MockMultipartFile("file", "hw.png", "image/png", "hw bytes".getBytes());

            // 1. 上传
            File file = strategy.upload(multipartFile, "obs-bucket", "images");
            assertNotNull(file);
            assertEquals(FileStorageType.HUAWEI_OSS, file.getStorageType());

            // 2. 查 URL
            FileGetUrlBO pubBO = FileGetUrlBO.builder().bucket("public-obs").path("hw_pub.png").build();
            FileGetUrlBO priBO = FileGetUrlBO.builder().bucket("obs-bucket").path("hw_pri.png").build();
            Map<String, String> urls = strategy.findUrl(List.of(pubBO, priBO));
            assertEquals(2, urls.size());
            assertTrue(urls.get("hw_pub.png").contains("public-obs"));
            assertEquals("https://obs.signed/url", urls.get("hw_pri.png"));

            // 3. 删除
            FileDeleteBO delBO = FileDeleteBO.builder().bucket("obs-bucket").path("hw_pri.png").build();
            assertTrue(strategy.delete(delBO));
        }
    }

    @Test
    @DisplayName("测试 QiNiuFileStrategyImpl 七牛云上传、删除与下载URL")
    void testQiNiuFileStrategy() throws Exception {
        FileServerProperties properties = new FileServerProperties();
        FileServerProperties.QiNiu qiNiu = new FileServerProperties.QiNiu();
        qiNiu.setBucket("qiniu-bucket");
        qiNiu.setDomain("qiniu.domain");
        qiNiu.setExpiry(3600);
        properties.setQiNiu(qiNiu);
        properties.setPublicBucket(Set.of("qiniu-public"));

        FileMapper fileMapper = mock(FileMapper.class);
        UploadManager uploadManager = mock(UploadManager.class);
        BucketManager bucketManager = mock(BucketManager.class);
        Auth auth = Auth.create("test-ak", "test-sk");

        QiNiuFileStrategyImpl strategy = new QiNiuFileStrategyImpl(properties, fileMapper, uploadManager, bucketManager, auth);

        Response putResponse = mock(Response.class);
        ReflectionTestUtils.setField(putResponse, "statusCode", 200);
        when(putResponse.bodyString()).thenReturn("{\"key\":\"key1\",\"hash\":\"hash1\"}");
        when(uploadManager.put(any(), anyString(), anyString(), any(), anyString())).thenReturn(putResponse);

        MockMultipartFile multipartFile = new MockMultipartFile("file", "qn.jpg", "image/jpeg", "qn bytes".getBytes());

        // 1. 上传
        File file = strategy.upload(multipartFile, "qiniu-bucket", "avatar");
        assertNotNull(file);
        assertEquals(FileStorageType.QINIU_OSS, file.getStorageType());

        // 2. 查 URL
        FileGetUrlBO pubBO = FileGetUrlBO.builder().bucket("qiniu-public").path("pub.jpg").build();
        FileGetUrlBO priBO = FileGetUrlBO.builder().bucket("qiniu-bucket").path("pri.jpg").originalFileName("pri.jpg").build();
        Map<String, String> urlMap = strategy.findUrl(List.of(pubBO, priBO));
        assertEquals(2, urlMap.size());
        assertTrue(urlMap.get("pub.jpg").contains("qiniu-public"));
        assertNotNull(urlMap.get("pri.jpg"));

        // 3. 删除
        when(bucketManager.delete(anyString(), anyString())).thenReturn(putResponse);
        FileDeleteBO delBO = FileDeleteBO.builder().bucket("qiniu-bucket").path("pri.jpg").build();
        assertTrue(strategy.delete(delBO));
    }

    @Test
    @DisplayName("测试 LocalFileChunkStrategyImpl 与 AbstractFileChunkStrategy 分片合并与秒传复制")
    void testLocalChunkStrategy(@TempDir Path tempDir) throws Exception {
        FileServerProperties properties = new FileServerProperties();
        FileServerProperties.Local local = new FileServerProperties.Local();
        local.setStoragePath(tempDir.toString());
        local.setUrlPrefix("http://localhost/files/");
        properties.setLocal(local);

        FileMapper fileMapper = mock(FileMapper.class);
        LocalFileChunkStrategyImpl chunkStrategy = new LocalFileChunkStrategyImpl(fileMapper, properties);

        // 1. 测试 md5Check 未找到文件时返回 null
        when(fileMapper.selectList(any())).thenReturn(List.of());
        assertNull(chunkStrategy.md5Check("unknown_md5", 100L));

        // 2. 测试 md5Check 秒传命中并复制
        Path existingFilePath = tempDir.resolve("2026/09/orig.png");
        Files.createDirectories(existingFilePath.getParent());
        Files.writeString(existingFilePath, "existing file content");

        File existingFile = new File();
        existingFile.setId(1L);
        existingFile.setPath("2026/09/orig.png");
        existingFile.setUniqueFileName("orig.png");
        existingFile.setUrl("http://localhost/files/2026/09/orig.png");
        existingFile.setFileMd5("known_md5");

        when(fileMapper.selectList(any())).thenReturn(List.of(existingFile));
        File fastUploadFile = chunkStrategy.md5Check("known_md5", 100L);
        assertNotNull(fastUploadFile);
        verify(fileMapper).insert(any(File.class));

        // 3. 测试 chunksMerge 分片合并
        String folderName = "upload_folder_1";
        Path chunkFolder = tempDir.resolve(folderName);
        Files.createDirectories(chunkFolder);

        // 创建两个分片文件 0 和 1
        Files.writeString(chunkFolder.resolve("0"), "part 0; ");
        Files.writeString(chunkFolder.resolve("1"), "part 1.");

        FileChunksMergeDTO mergeDTO = new FileChunksMergeDTO();
        mergeDTO.setName(folderName);
        mergeDTO.setExt("txt");
        mergeDTO.setChunks(2);
        mergeDTO.setMd5("merge_md5");
        mergeDTO.setSize(15L);
        mergeDTO.setSubmittedFileName("full.txt");
        mergeDTO.setContextType("text/plain");

        R<File> mergeResult = chunkStrategy.chunksMerge(mergeDTO);
        assertNotNull(mergeResult);
        assertTrue(mergeResult.getIsSuccess());
        assertNotNull(mergeResult.getData());
        verify(fileMapper, atLeastOnce()).insert(any(File.class));
    }

    @Test
    @DisplayName("测试 AliFileChunkStrategyImpl 阿里云分片合并与复制")
    void testAliFileChunkStrategy(@TempDir Path tempDir) throws Exception {
        FileServerProperties properties = new FileServerProperties();
        FileServerProperties.Ali ali = new FileServerProperties.Ali();
        ali.setBucket("ali-chunk-bucket");
        ali.setEndpoint("oss-cn-beijing.aliyuncs.com");
        ali.setAccessKeyId("ak");
        ali.setAccessKeySecret("sk");
        ali.setUrlPrefix("https://ali-chunk-bucket.oss-cn-beijing.aliyuncs.com/");
        properties.setAli(ali);

        FileMapper fileMapper = mock(FileMapper.class);
        TestAliFileChunkStrategy aliChunkStrategy = new TestAliFileChunkStrategy(fileMapper, properties);

        OSS mockOss = mock(OSS.class);
        InitiateMultipartUploadResult initResult = mock(InitiateMultipartUploadResult.class);
        when(initResult.getUploadId()).thenReturn("mock-upload-id");
        when(mockOss.initiateMultipartUpload(any())).thenReturn(initResult);

        UploadPartResult uploadPartResult = mock(UploadPartResult.class);
        com.aliyun.oss.model.PartETag partETag = new com.aliyun.oss.model.PartETag(1, "etag1");
        when(uploadPartResult.getPartETag()).thenReturn(partETag);
        when(mockOss.uploadPart(any())).thenReturn(uploadPartResult);

        CompleteMultipartUploadResult completeResult = mock(CompleteMultipartUploadResult.class);
        when(completeResult.getBucketName()).thenReturn("ali-chunk-bucket");
        when(mockOss.completeMultipartUpload(any())).thenReturn(completeResult);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(1024L);
        when(mockOss.getObjectMetadata(anyString(), anyString())).thenReturn(metadata);

        UploadPartCopyResult copyResult = mock(UploadPartCopyResult.class);
        when(copyResult.getPartETag()).thenReturn(partETag);
        when(mockOss.uploadPartCopy(any())).thenReturn(copyResult);

        try (MockedConstruction<OSSClientBuilder> mocked = Mockito.mockConstruction(OSSClientBuilder.class,
                (builder, context) -> when(builder.build(anyString(), anyString(), anyString())).thenReturn(mockOss))) {

            // 1. 测试 copyFile (通过 md5Check 触发)
            File existingFile = new File();
            existingFile.setId(10L);
            existingFile.setPath("2026/09/file.txt");
            existingFile.setUniqueFileName("file.txt");
            existingFile.setSuffix("txt");
            existingFile.setUrl("https://ali.com/file.txt");

            when(fileMapper.selectList(any())).thenReturn(List.of(existingFile));
            File copied = aliChunkStrategy.md5Check("test_md5", 99L);
            assertNotNull(copied);

            // 2. 测试 merge
            java.io.File chunk1 = tempDir.resolve("chunk1.tmp").toFile();
            FileUtil.writeUtf8String("chunk data", chunk1);

            FileChunksMergeDTO mergeDTO = new FileChunksMergeDTO();
            mergeDTO.setSubmittedFileName("merged.txt");
            mergeDTO.setContextType("text/plain");

            R<File> result = aliChunkStrategy.merge(List.of(chunk1), tempDir.toString(), "merged.txt", mergeDTO);
            assertNotNull(result);
            assertTrue(result.getIsSuccess());
            assertEquals("ali-chunk-bucket", result.getData().getBucket());
        }
    }

    @Test
    @DisplayName("测试 AliFileStrategyImpl 上传、获取URL与删除")
    void testAliFileStrategy() throws Exception {
        FileServerProperties properties = new FileServerProperties();
        FileServerProperties.Ali ali = new FileServerProperties.Ali();
        ali.setBucket("ali-standard-bucket");
        ali.setEndpoint("oss-cn-beijing.aliyuncs.com");
        ali.setAccessKeyId("ak");
        ali.setAccessKeySecret("sk");
        ali.setUrlPrefix("https://ali-standard-bucket.oss-cn-beijing.aliyuncs.com/");
        properties.setAli(ali);
        properties.setPublicBucket(Set.of("public-bucket"));

        FileMapper fileMapper = mock(FileMapper.class);
        com.dalio.cloud.file.strategy.impl.ali.AliFileStrategyImpl strategy = new com.dalio.cloud.file.strategy.impl.ali.AliFileStrategyImpl(properties, fileMapper);

        OSS mockOss = mock(OSS.class);
        when(mockOss.doesBucketExist(anyString())).thenReturn(true);
        when(mockOss.putObject(any(com.aliyun.oss.model.PutObjectRequest.class))).thenReturn(new com.aliyun.oss.model.PutObjectResult());
        when(mockOss.generatePresignedUrl(anyString(), anyString(), any())).thenReturn(new java.net.URL("https://ali.com/signed-url"));

        try (MockedConstruction<OSSClientBuilder> mocked = Mockito.mockConstruction(OSSClientBuilder.class,
                (builder, context) -> when(builder.build(anyString(), anyString(), anyString())).thenReturn(mockOss))) {

            // 1. 上传
            MockMultipartFile multipartFile = new MockMultipartFile("file", "doc.pdf", "application/pdf", "pdf content".getBytes());
            File uploaded = strategy.upload(multipartFile, "ali-standard-bucket", "user-files");
            assertNotNull(uploaded);
            assertEquals(FileStorageType.ALI_OSS, uploaded.getStorageType());

            // 2. 查 URL (含 publicBucket 与 私有签名 URL)
            FileGetUrlBO bo1 = FileGetUrlBO.builder().bucket("public-bucket").path("path1.png").build();
            FileGetUrlBO bo2 = FileGetUrlBO.builder().bucket("private-bucket").path("path2.png").build();
            Map<String, String> urlMap = strategy.findUrl(List.of(bo1, bo2));
            assertEquals(2, urlMap.size());

            // 3. 删除
            FileDeleteBO deleteBO = FileDeleteBO.builder().bucket("ali-standard-bucket").path("path1.png").build();
            assertTrue(strategy.delete(deleteBO));
            verify(mockOss).deleteObject("ali-standard-bucket", "path1.png");
        }
    }

    @Test
    @DisplayName("测试 AbstractFileChunkStrategy 分片缺失导致合并失败分支")
    void testAbstractFileChunkStrategyMissingChunks(@TempDir Path tempDir) {
        FileServerProperties properties = new FileServerProperties();
        FileServerProperties.Local local = new FileServerProperties.Local();
        local.setStoragePath(tempDir.toString());
        properties.setLocal(local);

        FileMapper fileMapper = mock(FileMapper.class);
        LocalFileChunkStrategyImpl chunkStrategy = new LocalFileChunkStrategyImpl(fileMapper, properties);

        // 分片数量期望 3 个，实际 0 个，且持久层无对应 md5 签名
        FileChunksMergeDTO mergeDTO = new FileChunksMergeDTO();
        mergeDTO.setName("missing_folder");
        mergeDTO.setChunks(3);
        mergeDTO.setMd5("unknown_md5");
        mergeDTO.setExt("txt");

        when(fileMapper.selectList(any())).thenReturn(List.of());
        R<File> result = chunkStrategy.chunksMerge(mergeDTO);
        assertNotNull(result);
        assertFalse(result.getIsSuccess());
    }
}
