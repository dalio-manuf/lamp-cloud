package com.dalio.cloud.file.strategy.impl.minio;

import com.dalio.basic.base.R;
import com.dalio.cloud.file.dto.chunk.FileChunksMergeDTO;
import com.dalio.cloud.file.entity.File;
import com.dalio.cloud.file.mapper.FileMapper;
import com.dalio.cloud.file.properties.FileServerProperties;
import com.dalio.cloud.file.strategy.impl.AbstractFileChunkStrategy;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 欢迎PR
 * <p>
 * 思路1：minIO的putObject自身就支持断点续传， 所以先将分片文件上传到文件服务器并合并成大文件后， 在将大文件通过putObject直接上传到minIO
 *
 * @author admin
 * @date 2020/11/22 5:02 下午
 */
@Slf4j
public class MinIoFileChunkStrategyImpl extends AbstractFileChunkStrategy {
    public MinIoFileChunkStrategyImpl(FileMapper fileMapper, FileServerProperties fileProperties) {
        super(fileMapper, fileProperties);
    }


    @Override
    protected void copyFile(File file) {

    }


    @Override
    protected R<File> merge(List<java.io.File> files, String path, String fileName, FileChunksMergeDTO info) {

        File filePo = File.builder()
//                .relativePath(relativePath)
//                .url(StrUtil.replace(url, "\\\\", StrPool.SLASH))
                .build();
        return R.success(filePo);
    }
}
