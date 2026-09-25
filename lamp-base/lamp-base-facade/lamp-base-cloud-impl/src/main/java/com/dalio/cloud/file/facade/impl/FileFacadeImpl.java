package com.dalio.cloud.file.facade.impl;


import com.dalio.basic.base.R;
import com.dalio.cloud.file.api.FileApi;
import com.dalio.cloud.file.enumeration.FileStorageType;
import com.dalio.cloud.file.facade.FileFacade;
import com.dalio.cloud.file.vo.result.FileResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件接口
 *
 * @author admin
 * @since 2024年09月20日10:45:54
 */
@Service
@RequiredArgsConstructor(onConstructor_ = @Lazy)
public class FileFacadeImpl implements FileFacade {

    private final FileApi fileApi;

    @Override
    public FileResultVO upload(MultipartFile file, String bizType, String bucket, FileStorageType storageType) {
        R<FileResultVO> result = fileApi.upload(file, bizType, bucket, storageType);
        return result != null && result.getIsSuccess() ? result.getData() : null;
    }
}
