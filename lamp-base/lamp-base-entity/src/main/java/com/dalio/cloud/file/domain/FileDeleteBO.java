package com.dalio.cloud.file.domain;

import lombok.Builder;
import lombok.Data;
import com.dalio.cloud.file.enumeration.FileStorageType;

/**
 * 文件删除
 *
 * @author admin
 * @date 2019/05/07
 */
@Data
@Builder
public class FileDeleteBO {
    /**
     * 桶
     */
    private String bucket;
    /**
     * 相对路径
     */
    private String path;
    /**
     * 存储类型
     */
    private FileStorageType storageType;
}
