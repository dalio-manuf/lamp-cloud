package com.dalio.cloud.file.strategy.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;
import com.dalio.basic.exception.BizException;
import com.dalio.basic.utils.StrPool;
import com.dalio.cloud.file.domain.FileGetUrlBO;
import com.dalio.cloud.file.entity.File;
import com.dalio.cloud.file.mapper.FileMapper;
import com.dalio.cloud.file.properties.FileServerProperties;
import com.dalio.cloud.file.strategy.FileStrategy;
import com.dalio.cloud.file.utils.FileTypeUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.StringJoiner;

import static com.dalio.basic.exception.code.ExceptionCode.BASE_VALID_PARAM;
import static com.dalio.basic.utils.DateUtils.SLASH_DATE_FORMAT;


/**
 * 文件抽象策略 处理类
 *
 * @author admin
 * @date 2019/06/17
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractFileStrategy implements FileStrategy {

    private static final String FILE_SPLIT = ".";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(SLASH_DATE_FORMAT);

    protected final FileServerProperties fileProperties;
    protected final FileMapper fileMapper;

    /**
     * 上传文件
     *
     * @param multipartFile 文件
     * @return 附件
     */
    @Override
    public File upload(MultipartFile multipartFile, String bucket, String bizType) {
        try {
            String originalFilename = multipartFile.getOriginalFilename();
            if (StrUtil.isEmpty(originalFilename) || !StrUtil.contains(originalFilename, FILE_SPLIT)) {
                throw BizException.wrap(BASE_VALID_PARAM.build("文件缺少后缀名"));
            }

            File file = File.builder()
                    .originalFileName(originalFilename)
                    .contentType(multipartFile.getContentType())
                    .size(multipartFile.getSize())
                    .bizType(bizType)
                    .suffix(FilenameUtils.getExtension(originalFilename))
                    .fileType(FileTypeUtil.getFileType(multipartFile.getContentType()))
                    .build();
            uploadFile(file, multipartFile, bucket);
            return file;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw BizException.wrap(BASE_VALID_PARAM.build("文件上传失败"), e);
        }
    }

    /**
     * 具体类型执行上传操作
     *
     * @param file          附件
     * @param multipartFile 文件
     * @param bucket        bucket
     * @throws Exception 异常
     */
    protected abstract void uploadFile(File file, MultipartFile multipartFile, String bucket) throws Exception;

    @Override
    public String getUrl(FileGetUrlBO fileGet) {
        return findUrl(Collections.singletonList(fileGet)).get(fileGet.getPath());
    }

    /**
     * 获取年月日 2020/09/01
     *
     * @return 日期文件夹
     */
    protected String getDateFolder() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    /**
     * 企业/年/月/日/业务类型/唯一文件名
     */
    protected String getPath(String bizType, String uniqueFileName) {
        return new StringJoiner(StrPool.SLASH)
                .add(bizType).add(getDateFolder()).add(uniqueFileName).toString();
    }

    protected String getUniqueFileName(File file) {
        return new StringJoiner(StrPool.DOT)
                .add(IdUtil.fastSimpleUUID())
                .add(file.getSuffix()).toString();
    }
}
