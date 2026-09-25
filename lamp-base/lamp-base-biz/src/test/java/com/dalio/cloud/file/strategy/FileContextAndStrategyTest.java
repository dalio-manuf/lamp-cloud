package com.dalio.cloud.file.strategy;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.dalio.cloud.file.domain.FileDeleteBO;
import com.dalio.cloud.file.domain.FileGetUrlBO;
import com.dalio.cloud.file.dto.chunk.FileChunksMergeDTO;
import com.dalio.cloud.file.entity.File;
import com.dalio.cloud.file.enumeration.FileStorageType;
import com.dalio.cloud.file.mapper.FileMapper;
import com.dalio.cloud.file.properties.FileServerProperties;
import com.dalio.cloud.file.strategy.impl.ali.AliFileStrategyImpl;
import com.dalio.cloud.file.vo.param.FileUploadVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;

import java.net.URL;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class FileContextAndStrategyTest {

    @Test
    @DisplayName("测试 FileContext 上传、获取URL、删除逻辑路由")
    void testFileContextRouting() {
        FileStrategy mockStrategy = mock(FileStrategy.class);
        Map<String, FileStrategy> strategyMap = Map.of(FileStorageType.LOCAL.name(), mockStrategy);

        FileServerProperties properties = new FileServerProperties();
        properties.setStorageType(FileStorageType.LOCAL);

        FileMapper fileMapper = mock(FileMapper.class);

        FileContext context = new FileContext(strategyMap, properties, fileMapper);

        // 1. 上传测试 (storageType 为 null 使用默认)
        MockMultipartFile multipartFile = new MockMultipartFile("file", "test.png", "image/png", new byte[]{1, 2, 3});
        FileUploadVO uploadVO = new FileUploadVO();
        uploadVO.setBucket("test-bucket");
        uploadVO.setBizType("avatar");

        File expectedFile = new File();
        expectedFile.setId(123L);
        when(mockStrategy.upload(any(), any(), any())).thenReturn(expectedFile);

        File result = context.upload(multipartFile, uploadVO);
        assertNotNull(result);
        assertEquals(123L, result.getId());
        verify(mockStrategy).upload(multipartFile, "test-bucket", "avatar");

        // 2. 删除文件 (delFile = false 与 delFile = true)
        File delFile = new File();
        delFile.setBucket("bucket");
        delFile.setPath("/path/to/file.png");
        delFile.setStorageType(FileStorageType.LOCAL);

        properties.setDelFile(false);
        assertTrue(context.delete(List.of(delFile)));
        verify(mockStrategy, never()).delete(any());

        properties.setDelFile(true);
        assertTrue(context.delete(List.of(delFile)));
        verify(mockStrategy).delete(any(FileDeleteBO.class));

        // 3. 查 URL (空列表)
        assertTrue(context.findUrlByPath(Collections.emptyList()).isEmpty());
        assertTrue(context.findUrlById(Collections.emptyList()).isEmpty());
        assertFalse(context.delete(Collections.emptyList()));
    }

    @Test
    @DisplayName("测试 FileContext findUrlByPath 与 findUrlById")
    void testFindUrls() {
        FileStrategy localStrategy = mock(FileStrategy.class);
        FileStrategy ossStrategy = mock(FileStrategy.class);
        Map<String, FileStrategy> strategyMap = Map.of(
                FileStorageType.LOCAL.name(), localStrategy,
                FileStorageType.ALI_OSS.name(), ossStrategy
        );

        FileServerProperties properties = new FileServerProperties();
        properties.setStorageType(FileStorageType.LOCAL);
        FileMapper fileMapper = mock(FileMapper.class);

        FileContext context = new FileContext(strategyMap, properties, fileMapper);

        File f1 = new File();
        f1.setId(1L);
        f1.setPath("/local/path.png");
        f1.setUrl("http://localhost/files/path.png");
        f1.setStorageType(FileStorageType.LOCAL);

        File f2 = new File();
        f2.setId(2L);
        f2.setPath("/oss/path.png");
        f2.setStorageType(FileStorageType.ALI_OSS);

        when(fileMapper.selectList(any())).thenReturn(List.of(f1, f2));
        when(ossStrategy.getUrl(any())).thenReturn("http://oss/files/path.png");

        Map<String, String> urlMap = context.findUrlByPath(List.of("/local/path.png", "/oss/path.png"));
        assertEquals(2, urlMap.size());
        assertEquals("http://localhost/files/path.png", urlMap.get("/local/path.png"));
        assertEquals("http://oss/files/path.png", urlMap.get("/oss/path.png"));

        Map<Long, String> idMap = context.findUrlById(List.of(1L, 2L));
        assertEquals(2, idMap.size());
    }

    @Test
    @DisplayName("测试 FileContext download 单文件与多文件重定向与打包")
    void testDownload() throws Exception {
        FileStrategy localStrategy = mock(FileStrategy.class);
        FileStrategy ossStrategy = mock(FileStrategy.class);
        Map<String, FileStrategy> strategyMap = Map.of(
                FileStorageType.LOCAL.name(), localStrategy,
                FileStorageType.ALI_OSS.name(), ossStrategy
        );

        FileServerProperties properties = new FileServerProperties();
        properties.setStorageType(FileStorageType.LOCAL);
        FileMapper fileMapper = mock(FileMapper.class);

        FileContext context = new FileContext(strategyMap, properties, fileMapper);

        // 1. download 单文件 LOCAL 重定向带文件名
        File f1 = new File();
        f1.setId(1L);
        f1.setPath("/local/f1.png");
        f1.setOriginalFileName("test.png");
        f1.setStorageType(FileStorageType.LOCAL);
        when(localStrategy.getUrl(any())).thenReturn("http://localhost/f1.png");

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        context.download(request, response, f1);
        assertEquals(302, response.getStatus());
        assertTrue(response.getHeader("Location").contains("filename=test.png"));

        // 2. download 单文件 OSS 直接重定向
        File f2 = new File();
        f2.setId(2L);
        f2.setPath("/oss/f2.png");
        f2.setStorageType(FileStorageType.ALI_OSS);
        when(ossStrategy.getUrl(any())).thenReturn("http://oss/f2.png");

        MockHttpServletResponse responseOss = new MockHttpServletResponse();
        context.download(request, responseOss, f2);
        assertEquals(302, responseOss.getStatus());
        assertEquals("http://oss/f2.png", responseOss.getHeader("Location"));

        // 3. md5Check 与 chunksMerge
        assertNotNull(context.md5Check("abc123md5", 1L));
        assertNotNull(context.chunksMerge(new FileChunksMergeDTO()));
    }

    @Test
    @DisplayName("测试 FileContext download 多文件打包与同名文件重命名")
    void testDownloadMultiFiles() throws Exception {
        FileStrategy localStrategy = mock(FileStrategy.class);
        Map<String, FileStrategy> strategyMap = Map.of(FileStorageType.LOCAL.name(), localStrategy);

        FileServerProperties properties = new FileServerProperties();
        properties.setStorageType(FileStorageType.LOCAL);
        FileMapper fileMapper = mock(FileMapper.class);

        FileContext context = new FileContext(strategyMap, properties, fileMapper);

        File f1 = new File();
        f1.setId(1L);
        f1.setPath("/local/f1.png");
        f1.setOriginalFileName("test.png");
        f1.setSize(1024L);
        f1.setUrl("http://localhost/f1.png");
        f1.setStorageType(FileStorageType.LOCAL);

        File f2 = new File();
        f2.setId(2L);
        f2.setPath("/local/f2.png");
        f2.setOriginalFileName("test.png"); // 同名
        f2.setSize(2048L);
        f2.setUrl("http://localhost/f2.png");
        f2.setStorageType(FileStorageType.LOCAL);

        when(localStrategy.getUrl(any())).thenReturn("http://localhost/f1.png");

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // 用 mockStatic 拦截 ZipUtils.zipFilesByInputStream，避免发起真实网络 IO
        try (var mockedZip = Mockito.mockStatic(com.dalio.cloud.file.utils.ZipUtils.class)) {
            context.download(request, response, List.of(f1, f2));
            mockedZip.verify(() -> com.dalio.cloud.file.utils.ZipUtils.zipFilesByInputStream(
                    anyMap(), eq(3072L), eq("test等.zip"), eq(request), eq(response)
            ));
        }
    }

    @Test
    @DisplayName("测试不存在的存储策略时抛出异常")
    void testStrategyNotFoundThrows() {
        Map<String, FileStrategy> emptyMap = Collections.emptyMap();
        FileServerProperties properties = new FileServerProperties();
        properties.setStorageType(FileStorageType.ALI_OSS);

        FileMapper fileMapper = mock(FileMapper.class);
        FileContext context = new FileContext(emptyMap, properties, fileMapper);

        FileUploadVO uploadVO = new FileUploadVO();
        uploadVO.setStorageType(FileStorageType.ALI_OSS);

        assertThrows(RuntimeException.class, () -> context.upload(null, uploadVO));
    }

    @Test
    @DisplayName("测试 AliFileStrategyImpl findUrl 与 delete")
    void testAliFileStrategy() throws Exception {
        FileServerProperties properties = new FileServerProperties();
        FileServerProperties.Ali ali = new FileServerProperties.Ali();
        ali.setAccessKeyId("ak");
        ali.setAccessKeySecret("sk");
        ali.setEndpoint("oss-cn-beijing.aliyuncs.com");
        ali.setBucket("my-bucket");
        ali.setUrlPrefix("https://my-bucket.oss-cn-beijing.aliyuncs.com/");
        ali.setExpiry(3600);
        properties.setAli(ali);
        properties.setPublicBucket(Set.of("public-bucket"));

        FileMapper fileMapper = mock(FileMapper.class);
        AliFileStrategyImpl aliStrategy = new AliFileStrategyImpl(properties, fileMapper);

        OSS mockOss = mock(OSS.class);
        when(mockOss.generatePresignedUrl(anyString(), anyString(), any(Date.class)))
                .thenReturn(new URL("https://my-bucket.oss-cn-beijing.aliyuncs.com/presigned"));

        try (MockedConstruction<OSSClientBuilder> mocked = Mockito.mockConstruction(OSSClientBuilder.class,
                (builder, context) -> when(builder.build(anyString(), anyString(), anyString())).thenReturn(mockOss))) {

            // 1. 公共读 bucket
            FileGetUrlBO bo1 = FileGetUrlBO.builder().bucket("public-bucket").path("img/a.png").build();
            // 2. 私有 bucket 走预签名
            FileGetUrlBO bo2 = FileGetUrlBO.builder().bucket("private-bucket").path("docs/b.pdf").build();

            Map<String, String> urlMap = aliStrategy.findUrl(List.of(bo1, bo2));
            assertEquals(2, urlMap.size());
            assertTrue(urlMap.get("img/a.png").contains("public-bucket"));
            assertEquals("https://my-bucket.oss-cn-beijing.aliyuncs.com/presigned", urlMap.get("docs/b.pdf"));

            // 3. delete
            FileDeleteBO delBO = FileDeleteBO.builder().bucket("my-bucket").path("img/a.png").build();
            assertTrue(aliStrategy.delete(delBO));
            verify(mockOss).deleteObject("my-bucket", "img/a.png");
        }
    }
}
