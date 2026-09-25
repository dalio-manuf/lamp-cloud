package com.dalio.cloud.file.strategy.impl.huawei;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.dalio.basic.utils.CollHelper;
import com.dalio.basic.utils.StrPool;
import com.dalio.cloud.file.domain.FileDeleteBO;
import com.dalio.cloud.file.domain.FileGetUrlBO;
import com.dalio.cloud.file.entity.File;
import com.dalio.cloud.file.enumeration.FileStorageType;
import com.dalio.cloud.file.mapper.FileMapper;
import com.dalio.cloud.file.properties.FileServerProperties;
import com.dalio.cloud.file.strategy.impl.AbstractFileStrategy;
import com.obs.services.ObsClient;
import com.obs.services.exception.ObsException;
import com.obs.services.model.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author admin
 * @date 2020/11/22 5:00 下午
 */
@Slf4j
@Component("HUAWEI_OSS")
public class HuaweiFileStrategyImpl extends AbstractFileStrategy {

    public HuaweiFileStrategyImpl(FileServerProperties fileProperties, FileMapper fileMapper) {
        super(fileProperties, fileMapper);
    }

    @Override
    protected void uploadFile(File file, MultipartFile multipartFile, String bucket) throws Exception {
        FileServerProperties.Huawei huawei = fileProperties.getHuawei();

        //生成文件名
        String uniqueFileName = getUniqueFileName(file);
        String path = getPath(file.getBizType(), uniqueFileName);
        bucket = StrUtil.isEmpty(bucket) ? huawei.getBucket() : bucket;

        try (ObsClient obsClient = new ObsClient(huawei.getAccessKey(), huawei.getSecretKey(), huawei.getEndpoint())) {
//            createBucket(obsClient, bucket);

            PutObjectRequest request = new PutObjectRequest(bucket, path, multipartFile.getInputStream());
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(multipartFile.getContentType());
            metadata.setContentDisposition("attachment;fileName=" + URLUtil.encode(file.getOriginalFileName()));
            request.setMetadata(metadata);
            obsClient.putObject(request);

            String url = huawei.getUrlPrefix() + bucket + StrPool.SLASH + path;
            file.setUrl(url);
        }
        file.setBucket(bucket);
        file.setPath(path);
        file.setUniqueFileName(uniqueFileName);
        file.setStorageType(FileStorageType.HUAWEI_OSS);
    }

    private void createBucket(ObsClient obsClient, String bucket) throws ObsException {
        boolean exists = obsClient.headBucket(bucket);
        if (!exists) {
            ObsBucket obsBucket = new ObsBucket();
            obsBucket.setBucketName(bucket);
            obsBucket.setLocation(fileProperties.getHuawei().getLocation());
            obsClient.createBucket(obsBucket);
        }
    }

    @Override
    @SneakyThrows
    public boolean delete(FileDeleteBO file) {
        FileServerProperties.Huawei huawei = fileProperties.getHuawei();
        try (ObsClient obsClient = new ObsClient(huawei.getAccessKey(), huawei.getSecretKey(),
                huawei.getEndpoint())) {
            obsClient.deleteObject(file.getBucket(), file.getPath());
        }
        return true;
    }

    @Override
    @SneakyThrows
    public Map<String, String> findUrl(List<FileGetUrlBO> fileGets) {
        FileServerProperties.Huawei huawei = fileProperties.getHuawei();
        Set<String> publicBucket = fileProperties.getPublicBucket();

        Map<String, String> map = new LinkedHashMap<>(CollHelper.initialCapacity(fileGets.size()));
        try (ObsClient obsClient = new ObsClient(huawei.getAccessKey(), huawei.getSecretKey(),
                huawei.getEndpoint())) {
            for (FileGetUrlBO fileGet : fileGets) {
                String bucket = StrUtil.isEmpty(fileGet.getBucket()) ? huawei.getBucket() : fileGet.getBucket();
                try {
                    if (CollUtil.isNotEmpty(publicBucket) && publicBucket.contains(bucket)) {
                        StringBuilder url = new StringBuilder(huawei.getUrlPrefix())
                                .append(bucket)
                                .append(StrPool.SLASH)
                                .append(fileGet.getPath());
                        map.put(fileGet.getPath(), url.toString());
                    } else {
                        TemporarySignatureRequest req = new TemporarySignatureRequest(HttpMethodEnum.GET, 300);
                        req.setBucketName(bucket);
                        req.setObjectKey(fileGet.getPath());
                        req.setExpires(huawei.getExpiry());
                        TemporarySignatureResponse res = obsClient.createTemporarySignature(req);
                        map.put(fileGet.getPath(), res.getSignedUrl());
                    }
                } catch (Exception e) {
                    log.warn("加载文件url地址失败，请确保yml中第三方存储参数配置正确. bucket={}, 文件名={} path={}", bucket, fileGet.getOriginalFileName(), fileGet.getPath(), e);
                    map.put(fileGet.getPath(), StrPool.EMPTY);
                }
            }
        }
        return map;
    }


}
