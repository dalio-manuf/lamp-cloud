package com.dalio.cloud.file.controller;

import com.dalio.basic.base.controller.DeleteController;
import com.dalio.basic.base.controller.QueryController;
import com.dalio.basic.base.controller.SuperSimpleController;
import com.dalio.basic.base.request.PageParams;
import com.dalio.basic.interfaces.echo.EchoService;
import com.dalio.cloud.file.entity.File;
import com.dalio.cloud.file.service.FileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 前端控制器
 * 增量文件上传日志
 * </p>
 *
 * @author admin
 * @date 2021-06-30
 * @create [2021-06-30] [admin] [初始创建]
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
@Tag(name = "com_file表增删改")
public class FileController extends SuperSimpleController<FileService, Long, File>
        implements QueryController<Long, File, File, File>, DeleteController<Long, File> {

    private final EchoService echoService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }

    @Override
    public Class<File> getResultVOClass() {
        return File.class;
    }

    @Override
    public void handlerQueryParams(PageParams<File> params) {
    }

//    @Override
//    public R<Boolean> handlerDelete(List<Long> longs) {
//        ContextUtil.setDefTenantId();
//        return R.successDef(true);
//    }
}
