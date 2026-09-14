package com.dalio.cloud.file.api;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import com.dalio.basic.base.R;
import com.dalio.basic.constant.Constants;
import com.dalio.cloud.file.api.fallback.FileApiFallback;
import com.dalio.cloud.file.enumeration.FileStorageType;
import com.dalio.cloud.file.vo.result.FileResultVO;

/**
 * 文件接口
 *
 * @author admin
 * @date 2019/06/21
 */
@FeignClient(name = "${" + Constants.PROJECT_PREFIX + ".feign.base-server:lamp-base-server}", fallback = FileApiFallback.class)
public interface FileApi {

    /**
     * 通过feign-form 实现文件 跨服务上传
     *
     * @param file        文件
     * @param bizType     业务类型
     * @param bucket      桶
     * @param storageType 存储类型
     * @return 文件信息
     */
    @PostMapping(value = "/anyone/file/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    R<FileResultVO> upload(
            @RequestPart(value = "file") MultipartFile file,
            @RequestParam(value = "bizType") String bizType,
            @RequestParam(value = "bucket", required = false) String bucket,
            @RequestParam(value = "storageType", required = false) FileStorageType storageType);

}
