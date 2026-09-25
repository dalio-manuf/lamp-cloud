package com.dalio.cloud.generator.controller;

import com.dalio.basic.annotation.log.WebLog;
import com.dalio.basic.base.R;
import com.dalio.basic.base.controller.DownloadController;
import com.dalio.basic.base.request.DownloadVO;
import com.dalio.basic.base.service.SuperService;
import com.dalio.basic.validator.utils.ValidatorUtils;
import com.dalio.cloud.generator.entity.DefGenTable;
import com.dalio.cloud.generator.service.DefGenTableService;
import com.dalio.cloud.generator.vo.save.ProjectGeneratorVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * <p>
 * 前端控制器
 * 项目生成
 * </p>
 *
 * @author admin
 * @date 2022-03-01
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/defGenProject")
@Tag(name = "项目生成")
public class DefGenProjectController implements DownloadController<Long, DefGenTable> {
    private final DefGenTableService defGenTableService;

    @Override
    public SuperService<Long, DefGenTable> getSuperService() {
        return defGenTableService;
    }

    @Override
    public Class<DefGenTable> getEntityClass() {
        return DefGenTable.class;
    }

    @Operation(summary = "获取默认配置", description = "获取默认配置")
    @PostMapping("/getDef")
    @WebLog(value = "获取默认配置")
    public R<ProjectGeneratorVO> getDef() {

        return R.success(defGenTableService.getDef());
    }

    @Operation(summary = "获取系统属性", description = "获取系统属性")
    @PostMapping("/anno/getProperties")
    @WebLog(value = "获取系统属性")
    public R<Object> getProperties() {
        return R.success(System.getProperties());
    }


    @Operation(summary = "下载项目", description = "下载项目")
    @PostMapping(value = "/download", produces = "application/octet-stream")
    @WebLog(value = "下载项目")
    public void download(ProjectGeneratorVO projectGenerator, HttpServletResponse response) {
        ValidatorUtils.validateFast(projectGenerator);
        DownloadVO download = defGenTableService.download(projectGenerator);
        if (download != null && download.getData() != null) {
            write(download.getData(), download.getFileName(), response);
        }
    }

    @Operation(summary = "生成项目", description = "生成项目")
    @PostMapping("/generator")
    @WebLog(value = "生成项目")
    public R<Boolean> generator(@RequestBody @Validated ProjectGeneratorVO projectGenerator) {
        defGenTableService.generator(projectGenerator);
        return R.success(true);
    }
}
