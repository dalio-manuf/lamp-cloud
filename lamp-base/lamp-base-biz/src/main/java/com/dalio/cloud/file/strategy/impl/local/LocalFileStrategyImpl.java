package com.dalio.cloud.file.strategy.impl.local;

import cn.hutool.core.util.StrUtil;
import com.dalio.basic.utils.CollHelper;
import com.dalio.basic.utils.StrPool;
import com.dalio.cloud.file.domain.FileDeleteBO;
import com.dalio.cloud.file.domain.FileGetUrlBO;
import com.dalio.cloud.file.entity.File;
import com.dalio.cloud.file.enumeration.FileStorageType;
import com.dalio.cloud.file.mapper.FileMapper;
import com.dalio.cloud.file.properties.FileServerProperties;
import com.dalio.cloud.file.strategy.impl.AbstractFileStrategy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author admin
 * @date 2020/11/22 5:00 下午
 */
@Slf4j
@Component("LOCAL")
@Primary
public class LocalFileStrategyImpl extends AbstractFileStrategy {
    public LocalFileStrategyImpl(FileServerProperties fileProperties, FileMapper fileMapper) {
        super(fileProperties, fileMapper);
    }

    @Override
    protected void uploadFile(File file, MultipartFile multipartFile, String bucket) throws Exception {
        FileServerProperties.Local local = fileProperties.getLocal();
        bucket = StrUtil.isEmpty(bucket) ? local.getBucket() : bucket;

        //生成文件名
        String uniqueFileName = getUniqueFileName(file);
        // 相对路径
        String path = getPath(file.getBizType(), uniqueFileName);
        // web服务器存放的绝对路径
        String absolutePath = Paths.get(local.getStoragePath(), bucket, path).toString();

        // 存储文件
        java.io.File outFile = new java.io.File(absolutePath);
        FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), outFile);

        // 返回数据
        String url = local.getUrlPrefix() + bucket + StrPool.SLASH + path;
        file.setUrl(url);
        file.setUniqueFileName(uniqueFileName);
        file.setPath(path);
        file.setBucket(bucket);
        file.setStorageType(FileStorageType.LOCAL);
    }

    @Override
    public boolean delete(FileDeleteBO file) {
        FileServerProperties.Local local = fileProperties.getLocal();
        java.io.File ioFile = new java.io.File(Paths.get(local.getStoragePath(), file.getBucket(), file.getPath()).toString());
        FileUtils.deleteQuietly(ioFile);
        return true;
    }

    @Override
    public Map<String, String> findUrl(List<FileGetUrlBO> fileGets) {
        Map<String, String> map = new LinkedHashMap<>(CollHelper.initialCapacity(fileGets.size()));
        // 方式2 重新拼接 （urlPrefix 可能跟上传时不一样）
        FileServerProperties.Local local = fileProperties.getLocal();
        fileGets.forEach(item -> {
            String url = local.getUrlPrefix() +
                    item.getBucket() +
                    StrPool.SLASH +
                    item.getPath();
            map.put(item.getPath(), url);
        });
        return map;
    }
}
