package com.dalio.cloud.file.strategy;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import com.dalio.basic.base.R;
import com.dalio.basic.database.mybatis.conditions.Wraps;
import com.dalio.basic.utils.ArgumentAssert;
import com.dalio.basic.utils.CollHelper;
import com.dalio.basic.utils.StrPool;
import com.dalio.cloud.file.domain.FileDeleteBO;
import com.dalio.cloud.file.domain.FileGetUrlBO;
import com.dalio.cloud.file.dto.chunk.FileChunksMergeDTO;
import com.dalio.cloud.file.entity.File;
import com.dalio.cloud.file.enumeration.FileStorageType;
import com.dalio.cloud.file.mapper.FileMapper;
import com.dalio.cloud.file.properties.FileServerProperties;
import com.dalio.cloud.file.utils.ZipUtils;
import com.dalio.cloud.file.vo.param.FileUploadVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.function.Predicate;

/**
 * @author admin
 * @date 2021/7/8 10:26
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileContext {
    private final Map<String, FileStrategy> contextStrategyMap;
    private final FileServerProperties fileServerProperties;
    private final FileMapper fileMapper;

    private static Predicate<File> getFilePredicate() {
        return file -> file != null && StrUtil.isNotEmpty(file.getUrl());
    }

    private static String buildNewFileName(String filename, Integer order) {
        return StrUtil.strBuilder(filename).insert(filename.lastIndexOf(StrPool.DOT), "(" + order + ")").toString();
    }

    /**
     * 文件上传
     *
     * @param file         文件
     * @param fileUploadVO 文件上传参数
     * @return 文件对象
     */
    public File upload(MultipartFile file, FileUploadVO fileUploadVO) {
        FileStrategy fileStrategy = getFileStrategy(fileUploadVO.getStorageType());
        return fileStrategy.upload(file, fileUploadVO.getBucket(), fileUploadVO.getBizType());
    }

    private FileStrategy getFileStrategy(FileStorageType storageType) {
        storageType = storageType == null ? fileServerProperties.getStorageType() : storageType;
        FileStrategy fileStrategy = contextStrategyMap.get(storageType.name());
        ArgumentAssert.notNull(fileStrategy, "请配置正确的文件存储类型");
        return fileStrategy;
    }

    /**
     * 删除源文件
     *
     * @param list 列表
     * @return 是否成功
     */
    public boolean delete(List<File> list) {
        if (CollUtil.isEmpty(list)) {
            return false;
        }
        if (!fileServerProperties.getDelFile()) {
            return true;
        }

        list.forEach(item -> {
            FileDeleteBO fileDeleteBO = FileDeleteBO.builder()
                    .bucket(item.getBucket())
                    .path(item.getPath())
                    .storageType(item.getStorageType())
                    .build();
            FileStrategy fileStrategy = getFileStrategy(item.getStorageType());
            fileStrategy.delete(fileDeleteBO);
        });
        return true;
    }

    /**
     * 根据路径获取访问地址
     *
     * @param paths 文件查询对象
     * @return
     */
    public Map<String, String> findUrlByPath(List<String> paths) {
        if (CollUtil.isEmpty(paths)) {
            return Collections.emptyMap();
        }
        List<File> pathFiles = fileMapper.selectList(Wraps.<File>lbQ().in(File::getPath, paths));

        return findUrl(pathFiles);
    }

    private Map<String, String> findUrl(List<File> pathFiles) {
        if (CollUtil.isEmpty(pathFiles)) {
            return Collections.emptyMap();
        }
        Map<String, String> map = new LinkedHashMap<>(CollHelper.initialCapacity(pathFiles.size()));
        for (File fileFile : pathFiles) {
            if (fileFile == null || map.containsKey(fileFile.getPath())) {
                continue;
            }
            if (FileStorageType.LOCAL.eq(fileFile.getStorageType())) {
                map.put(fileFile.getPath(), fileFile.getUrl());
            } else {
                FileStrategy fileStrategy = getFileStrategy(fileFile.getStorageType());
                map.put(fileFile.getPath(), fileStrategy.getUrl(FileGetUrlBO.builder()
                        .bucket(fileFile.getBucket())
                        .path(fileFile.getPath())
                        .originalFileName(fileFile.getOriginalFileName())
                        .build()));
            }
        }
        return map;
    }

    public Map<Long, String> findUrlById(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        List<File> pathFiles = fileMapper.selectList(Wraps.<File>lbQ().in(File::getId, ids));
        Map<Long, String> map = new LinkedHashMap<>(CollHelper.initialCapacity(pathFiles.size()));
        for (File fileFile : pathFiles) {
            if (fileFile == null) {
                continue;
            }
            FileStrategy fileStrategy = getFileStrategy(fileFile.getStorageType());
            map.put(fileFile.getId(), fileStrategy.getUrl(FileGetUrlBO.builder()
                    .bucket(fileFile.getBucket())
                    .path(fileFile.getPath())
                    .originalFileName(fileFile.getOriginalFileName())
                    .build()));
        }
        return map;
    }

    public void download(HttpServletRequest request, HttpServletResponse response, List<File> list) throws Exception {
        for (File fileFile : list) {
            FileStrategy fileStrategy = getFileStrategy(fileFile.getStorageType());
            String url = fileStrategy.getUrl(FileGetUrlBO.builder()
                    .bucket(fileFile.getBucket())
                    .path(fileFile.getPath())
                    .build());
            fileFile.setUrl(url);
        }
        down(request, response, list);
    }

    public void download(HttpServletRequest request, HttpServletResponse response, File file) throws Exception {
        FileStrategy fileStrategy = getFileStrategy(file.getStorageType());
        String url = fileStrategy.getUrl(FileGetUrlBO.builder()
                .bucket(file.getBucket())
                .path(file.getPath())
                .build());
        file.setUrl(url);
        if (StrUtil.isNotEmpty(url)) {
            // 重定向url，让浏览器直接下载该url，将下载压力转交给第三方oss，减轻文件服务的压力
            if (FileStorageType.LOCAL.eq(file.getStorageType())) {
                // 本地模式，需要在nginx配置  add_header Content-Disposition "attachment;filename=$arg_attname";
                response.sendRedirect(url + "?filename=" + URLUtil.encode(file.getOriginalFileName()));
            } else {
                response.sendRedirect(url);
            }
        } else {
            down(request, response, Collections.singletonList(file));
        }
    }

    public void down(HttpServletRequest request, HttpServletResponse response, List<File> list) throws Exception {
        long fileSize = list.stream().filter(getFilePredicate())
                .mapToLong(file -> Convert.toLong(file.getSize(), 0L)).sum();
        String packageName = list.get(0).getOriginalFileName();
        if (list.size() > 1) {
            packageName = StrUtil.subBefore(packageName, ".", true) + "等.zip";
        }

        Map<String, String> map = new LinkedHashMap<>(CollHelper.initialCapacity(list.size()));
        Map<String, Integer> duplicateFile = new HashMap<>(CollHelper.initialCapacity(list.size()));
        list.stream()
                //过滤不符合要求的文件
                .filter(getFilePredicate())
                //循环处理相同的文件名
                .forEach(file -> {
                    String originalFileName = file.getOriginalFileName();
                    if (map.containsKey(originalFileName)) {
                        int count = duplicateFile.merge(originalFileName, 1, Integer::sum);
                        originalFileName = buildNewFileName(originalFileName, count);
                    }
                    map.put(originalFileName, file.getUrl());
                });

        ZipUtils.zipFilesByInputStream(map, fileSize, packageName, request, response);
    }


    /**
     * 根据md5检测文件
     *
     * @param md5    md5
     * @param userId 用户id
     * @return 附件
     */
    public File md5Check(String md5, Long userId) {
        return new File();
    }

    /**
     * 合并文件
     *
     * @param merge 合并参数
     * @return 附件
     */
    public R<File> chunksMerge(FileChunksMergeDTO merge) {
        return R.success(new File());
    }
}
