package com.dalio.cloud.file.strategy.impl.fastdfs;

import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.AppendFileStorageClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import com.dalio.basic.base.R;
import com.dalio.cloud.file.dto.chunk.FileChunksMergeDTO;
import com.dalio.cloud.file.entity.File;
import com.dalio.cloud.file.mapper.FileMapper;
import com.dalio.cloud.file.properties.FileServerProperties;
import com.dalio.cloud.file.strategy.impl.AbstractFileChunkStrategy;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * @author admin
 * @date 2020/11/22 5:18 下午
 */
@Slf4j
public class FastDfsFileChunkStrategyImpl extends AbstractFileChunkStrategy {
    protected final AppendFileStorageClient storageClient;

    public FastDfsFileChunkStrategyImpl(FileMapper fileMapper, FileServerProperties fileProperties, AppendFileStorageClient storageClient) {
        super(fileMapper, fileProperties);
        this.storageClient = storageClient;
    }

    @Override
    protected void copyFile(File file) {
        // 由于大文件下载然后在上传会内存溢出， 所以 FastDFS 不复制，删除时通过业务手段
        /*
            DownloadByteArray callback = new DownloadByteArray();
            byte[] content = storageClient.downloadFile(file.getGroup(), file.getPath(), callback);
            InputStream in = new ByteArrayInputStream(content);
            StorePath storePath = storageClient.uploadFile(file.getGroup(), in, file.getSize(), file.getExt());
            file.setUrl(fileProperties.getUrlPrefix() + storePath.getFullPath());
            file.setGroup(storePath.getGroup());
            file.setPath(storePath.getPath());
         */
    }

    @Override
    protected R<File> merge(List<java.io.File> files, String path, String fileName, FileChunksMergeDTO info) throws IOException {
        StorePath storePath = null;

        long start = System.currentTimeMillis();
        for (int i = 0; i < files.size(); i++) {
            java.io.File file = files.get(i);

            try (FileInputStream in = FileUtils.openInputStream(file)) {
                if (i == 0) {
                    storePath = storageClient.uploadAppenderFile(null, in,
                            file.length(), info.getExt());
                } else {
                    storageClient.appendFile(storePath.getGroup(), storePath.getPath(),
                            in, file.length());
                }
            }
        }
        if (storePath == null) {
            return R.fail("上传失败");
        }

        long end = System.currentTimeMillis();
        log.info("上传耗时={}", (end - start));
        String url = fileProperties.getFastDfs().getUrlPrefix() + storePath.getFullPath();
        File filePo = File.builder()
                .url(url)
                .bucket(storePath.getGroup())
                .path(storePath.getPath())
                .build();
        return R.success(filePo);
    }
}
