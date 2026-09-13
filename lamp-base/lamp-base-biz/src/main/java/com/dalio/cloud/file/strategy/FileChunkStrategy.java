package com.dalio.cloud.file.strategy;


import com.dalio.basic.base.R;
import com.dalio.cloud.file.dto.chunk.FileChunksMergeDTO;
import com.dalio.cloud.file.entity.File;

/**
 * 文件分片处理策略类
 *
 * @author admin
 * @date 2019/06/19
 */
public interface FileChunkStrategy {

    /**
     * 根据md5检测文件
     *
     * @param md5    md5
     * @param userId 用户id
     * @return 附件
     */
    File md5Check(String md5, Long userId);

    /**
     * 合并文件
     *
     * @param merge 合并参数
     * @return 附件
     */
    R<File> chunksMerge(FileChunksMergeDTO merge);
}
