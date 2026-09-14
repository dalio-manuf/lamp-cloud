package com.dalio.cloud.test;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.dalio.basic.base.R;
import com.dalio.cloud.base.entity.user.BaseEmployee;
import com.dalio.cloud.base.service.BaseEmployeeTestService;

/**
 * @author admin
 * @version v1.0
 * @date 2022/9/20 11:33 AM
 * @create [2022/9/20 11:33 AM ] [admin] [初始创建]
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/baseEmployeeController")
@Tag(name = "员工测试")
public class BaseEmployeeTestController {

    private final BaseEmployeeTestService baseEmployeeTestService;

    @Operation(summary = "保存员工")
    @PostMapping("/save")
    public R<Boolean> save(@RequestBody BaseEmployee baseEmployee) {
        // 拼接
        return R.success(baseEmployeeTestService.save(baseEmployee));
    }

    @Operation(summary = "根据ID查询员工")
    @GetMapping("/getById")
    public R<BaseEmployee> getById(@RequestParam Long id) {
        // 拼接
        return R.success(baseEmployeeTestService.getById(id));
    }

    @Operation(summary = "获取员工")
    @GetMapping("/get")
    public R<BaseEmployee> get(@RequestParam Long id) {
        // 不会拼接
        return R.success(baseEmployeeTestService.get(id));
    }
}
