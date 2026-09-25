package com.dalio.cloud.file.api.fallback;

import com.dalio.basic.base.R;
import com.dalio.cloud.file.api.FileApi;
import com.dalio.cloud.file.enumeration.FileStorageType;
import com.dalio.cloud.file.vo.result.FileResultVO;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * 熔断
 *
 * @author admin
 * @date 2019/07/25
 */
@Component
public class FileApiFallback implements FileApi {
    @Override
    public R<FileResultVO> upload(MultipartFile file, String bizType, String bucket, FileStorageType storageType) {
        return R.timeout();
    }

}
