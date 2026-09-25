package com.dalio.cloud.base.controller.user;

import com.dalio.basic.annotation.log.WebLog;
import com.dalio.basic.base.R;
import com.dalio.basic.base.controller.SuperCacheController;
import com.dalio.basic.interfaces.echo.EchoService;
import com.dalio.basic.utils.TreeUtil;
import com.dalio.cloud.base.entity.user.BaseOrg;
import com.dalio.cloud.base.service.user.BaseOrgService;
import com.dalio.cloud.base.vo.query.user.BaseOrgPageQuery;
import com.dalio.cloud.base.vo.result.user.BaseOrgResultVO;
import com.dalio.cloud.base.vo.save.user.BaseOrgRoleRelSaveVO;
import com.dalio.cloud.base.vo.save.user.BaseOrgSaveVO;
import com.dalio.cloud.base.vo.update.user.BaseOrgUpdateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.dalio.cloud.common.constant.SwaggerConstants.DATA_TYPE_LONG;
import static com.dalio.cloud.common.constant.SwaggerConstants.DATA_TYPE_STRING;


/**
 * <p>
 * 前端控制器
 * 组织
 * </p>
 *
 * @author admin
 * @date 2021-10-18
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/baseOrg")
@Tag(name = "组织")
public class BaseOrgController extends SuperCacheController<BaseOrgService, Long, BaseOrg, BaseOrgSaveVO, BaseOrgUpdateVO, BaseOrgPageQuery, BaseOrgResultVO> {

    private final EchoService echoService;

    @Override
    public EchoService getEchoService() {
        return echoService;
    }


    @Parameters({
            @Parameter(name = "name", description = "name", required = true, schema = @Schema(type = DATA_TYPE_STRING), in = ParameterIn.QUERY),
            @Parameter(name = "parentId", description = "parentId", schema = @Schema(type = DATA_TYPE_LONG), in = ParameterIn.QUERY),
            @Parameter(name = "id", description = "ID", schema = @Schema(type = DATA_TYPE_LONG), in = ParameterIn.QUERY),
    })
    @Operation(summary = "检测名称是否可用")
    @GetMapping("/check")
    public R<Boolean> check(@RequestParam String name, @RequestParam Long parentId, @RequestParam(required = false) Long id) {
        return success(superService.check(name, parentId, id));
    }

    /**
     * 按树结构查询
     *
     * @param pageQuery 查询参数
     * @return 查询结果
     */
    @Operation(summary = "按树结构查询")
    @PostMapping("/tree")
    @WebLog("按树结构查询")
    public R<List<BaseOrgResultVO>> tree(@RequestBody BaseOrgPageQuery pageQuery) {
        List<BaseOrgResultVO> treeList = superService.list(pageQuery);
        echoService.action(treeList);
        return success(TreeUtil.buildTree(treeList));
    }

    /**
     * 给机构分配角色
     *
     * @param orgRoleSaveVO 参数
     * @return 新增结果
     */
    @Operation(summary = "给机构分配角色", description = "给机构分配角色")
    @PostMapping("/orgRole")
    @WebLog("给机构分配角色")
    public R<List<Long>> saveOrgRole(@RequestBody BaseOrgRoleRelSaveVO orgRoleSaveVO) {
        return success(superService.saveOrgRole(orgRoleSaveVO));
    }

    /**
     * 查询机构的角色
     *
     * @param orgId 员工id
     * @return 新增结果
     */
    @Operation(summary = "查询机构的角色")
    @GetMapping("/findOrgRoleByOrgId")
    @WebLog("查询机构的角色")
    public R<List<Long>> findOrgRoleByOrgId(@RequestParam Long orgId) {
        return success(superService.findOrgRoleByOrgId(orgId));
    }

    @Operation(summary = "查询员工的公司")
    @GetMapping("/findCompanyByEmployeeId")
    public R<List<BaseOrg>> findCompanyByEmployeeId(@RequestParam("employeeId") Long employeeId) {
        return success(superService.findCompanyByEmployeeId(employeeId));
    }

    @Operation(summary = "查询员工{employeeId}的在指定公司{companyId}下的所有部门")
    @GetMapping("/findDeptByEmployeeId")
    public R<List<BaseOrg>> findDeptByEmployeeId(@RequestParam("employeeId") Long employeeId,
                                                 @RequestParam("companyId") Long companyId) {
        return success(superService.findDeptByEmployeeId(employeeId, companyId));
    }
}
