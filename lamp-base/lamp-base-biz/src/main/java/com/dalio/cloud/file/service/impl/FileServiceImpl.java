package com.dalio.cloud.file.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.dalio.basic.base.service.impl.SuperServiceImpl;
import com.dalio.basic.exception.BizException;
import com.dalio.basic.utils.ArgumentAssert;
import com.dalio.basic.utils.BeanPlusUtil;
import com.dalio.cloud.file.entity.File;
import com.dalio.cloud.file.manager.FileManager;
import com.dalio.cloud.file.properties.FileServerProperties;
import com.dalio.cloud.file.service.FileService;
import com.dalio.cloud.file.strategy.FileContext;
import com.dalio.cloud.file.vo.param.FileUploadVO;
import com.dalio.cloud.file.vo.result.FileResultVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 业务实现类
 * 增量文件上传日志
 * </p>
 *
 * @author admin
 * @date 2021-06-30
 * @create [2021-06-30] [admin] [初始创建]
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileServiceImpl extends SuperServiceImpl<FileManager, Long, File> implements FileService {
    private final FileContext fileContext;
    private final FileServerProperties fileServerProperties;

    @Override
    public List<FileResultVO> listByBizIdAndBizType(Long bizId, String bizType) {
        ArgumentAssert.notNull(bizId, "请传入业务id");
        return superManager.listByBizIdAndBizType(bizId, bizType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileResultVO upload(MultipartFile file, FileUploadVO fileUploadVO) {
        // 忽略路径字段,只处理文件类型
        if (file.isEmpty()) {
            throw new BizException("请上传有效文件");
        }

        if (!fileServerProperties.validSuffix(file.getOriginalFilename())) {
            throw new BizException("文件后缀不支持");
        }
        if (StrUtil.containsAny(file.getOriginalFilename(), "../", "./")) {
            throw new BizException("文件名不能含有特殊字符");
        }

        File fileFile = fileContext.upload(file, fileUploadVO);
        superManager.save(fileFile);
        return BeanPlusUtil.toBean(fileFile, FileResultVO.class);
    }

    @Override
    public Map<String, String> findUrlByPath(List<String> paths) {
        if (CollUtil.isEmpty(paths)) {
            return Collections.emptyMap();
        }
        return fileContext.findUrlByPath(paths);
    }

    @Override
    public Map<Long, String> findUrlById(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        return fileContext.findUrlById(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return false;
        }
        List<File> list = superManager.listByIds(ids);
        if (list.isEmpty()) {
            return false;
        }
        superManager.removeByIds(ids);
        return fileContext.delete(list);
    }

    @Override
    public void download(HttpServletRequest request, HttpServletResponse response, List<Long> ids) throws Exception {
        List<File> list = superManager.listByIds(ids);
        ArgumentAssert.notEmpty(list, "未找到要下载的文件");
        fileContext.download(request, response, list);
    }

    @Override
    public void download(HttpServletRequest request, HttpServletResponse response, Long id) throws Exception {
        File file = superManager.getById(id);
        ArgumentAssert.notNull(file, "未找到要下载的文件");
        fileContext.download(request, response, file);
    }
}
