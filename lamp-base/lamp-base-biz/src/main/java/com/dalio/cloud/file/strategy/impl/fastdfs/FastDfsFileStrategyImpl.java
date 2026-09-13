package com.dalio.cloud.file.strategy.impl.fastdfs;


import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import com.dalio.basic.utils.CollHelper;
import com.dalio.basic.utils.StrPool;
import com.dalio.cloud.file.domain.FileDeleteBO;
import com.dalio.cloud.file.domain.FileGetUrlBO;
import com.dalio.cloud.file.entity.File;
import com.dalio.cloud.file.enumeration.FileStorageType;
import com.dalio.cloud.file.mapper.FileMapper;
import com.dalio.cloud.file.properties.FileServerProperties;
import com.dalio.cloud.file.strategy.impl.AbstractFileStrategy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author admin
 * @date 2020/11/22 5:17 下午
 */

@Component("FAST_DFS")
public class FastDfsFileStrategyImpl extends AbstractFileStrategy {
    private final FastFileStorageClient storageClient;

    public FastDfsFileStrategyImpl(FileServerProperties fileProperties, FastFileStorageClient storageClient,
                                   FileMapper fileMapper) {
        super(fileProperties, fileMapper);
        this.storageClient = storageClient;
    }

    @Override
    protected void uploadFile(File file, MultipartFile multipartFile, String bucket) throws Exception {
        StorePath storePath = storageClient.uploadFile(multipartFile.getInputStream(), multipartFile.getSize(), file.getSuffix(), null);
        file.setUrl(fileProperties.getFastDfs().getUrlPrefix() + storePath.getFullPath());
        file.setBucket(storePath.getGroup());
        file.setPath(storePath.getPath());
        file.setStorageType(FileStorageType.FAST_DFS);
    }

    @Override
    public boolean delete(FileDeleteBO file) {
        storageClient.deleteFile(file.getBucket(), file.getPath());
        return true;
    }

    @Override
    public Map<String, String> findUrl(List<FileGetUrlBO> fileGets) {
        Map<String, String> map = new LinkedHashMap<>(CollHelper.initialCapacity(fileGets.size()));
        // 方式1 取上传时存的url （多查询一次数据库）
        /*
        List<String> paths = fileGets.stream().map(FileGetUrlBO::getPath).toList();
        List<File> list = fileMapper.selectList(Wraps.<File>lbQ().eq(File::getPath, paths));
        list.forEach(item -> map.put(item.getPath(), item.getUrl()));
        */

        // 方式2 重新拼接 （urlPrefix 可能跟上传时不一样）
        FileServerProperties.FastDfs fastDfs = fileProperties.getFastDfs();
        fileGets.forEach(item -> {
            String url = fastDfs.getUrlPrefix() +
                         item.getBucket() +
                         StrPool.SLASH +
                         item.getPath();
            map.put(item.getPath(), url);
        });
        return map;
    }
}

