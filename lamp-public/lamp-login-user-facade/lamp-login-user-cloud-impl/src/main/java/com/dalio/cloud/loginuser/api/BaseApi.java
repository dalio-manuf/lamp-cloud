package com.dalio.cloud.loginuser.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import com.dalio.basic.base.R;
import com.dalio.basic.constant.Constants;
import com.dalio.cloud.model.entity.base.SysEmployee;
import com.dalio.cloud.model.entity.base.SysOrg;
import com.dalio.cloud.model.entity.base.SysPosition;

import java.util.List;

/**
 * 基础服务
 *
 * @author admin
 * @version v1.0
 * @date 2022/9/29 11:05 PM
 * @create [2022/9/29 11:05 PM ] [admin] [初始创建]
 */
@FeignClient(name = "${" + Constants.PROJECT_PREFIX + ".feign.base-server:lamp-base-server}")
public interface BaseApi {
    /**
     * 查询员工
     *
     * @param id 员工ID
     * @return com.dalio.basic.base.R<com.dalio.cloud.model.entity.base.SysEmployee>
     * @author admin
     * @date 2022/11/18 2:26 PM
     * @create [2022/11/18 2:26 PM ] [admin] [初始创建]
     */
    @GetMapping("/baseEmployee/{id}")
    R<SysEmployee> getEmployeeById(@PathVariable Long id);

    /**
     * 查询机构信息
     *
     * @param id 机构
     * @return com.dalio.basic.base.R<com.dalio.cloud.model.entity.base.SysOrg>
     * @author admin
     * @date 2022/11/18 2:26 PM
     * @create [2022/11/18 2:26 PM ] [admin] [初始创建]
     */
    @GetMapping("/baseOrg/{id}")
    R<SysOrg> getOrgById(@PathVariable Long id);

    /**
     * 查询岗位信息
     *
     * @param id 岗位ID
     * @return com.dalio.basic.base.R<com.dalio.cloud.model.entity.base.SysPosition>
     * @author admin
     * @date 2022/11/18 2:26 PM
     * @create [2022/11/18 2:26 PM ] [admin] [初始创建]
     */
    @GetMapping("/basePosition/{id}")
    R<SysPosition> getPositionById(@PathVariable Long id);

    /**
     * 查询员工的角色编码
     *
     * @param employeeId 员工ID
     * @return com.dalio.basic.base.R<java.util.List < java.lang.String>>
     * @author admin
     * @date 2022/11/18 2:26 PM
     * @create [2022/11/18 2:26 PM ] [admin] [初始创建]
     */
    @GetMapping("/baseRole/findRoleCodeByEmployeeId")
    R<List<String>> findRoleCodeByEmployeeId(@RequestParam("employeeId") Long employeeId);

    /**
     * 查询员工在指定公司下的部门
     *
     * @param employeeId 员工ID
     * @param companyId  公司ID
     * @return com.dalio.basic.base.R<java.util.List < com.dalio.cloud.model.entity.base.SysOrg>>
     * @author admin
     * @date 2022/11/18 2:26 PM
     * @create [2022/11/18 2:26 PM ] [admin] [初始创建]
     */
    @GetMapping("/baseOrg/findDeptByEmployeeId")
    R<List<SysOrg>> findDeptByEmployeeId(@RequestParam("employeeId") Long employeeId, @RequestParam("companyId") Long companyId);

    /**
     * 查询员工的 公司
     *
     * @param employeeId 员工
     * @return com.dalio.basic.base.R<java.util.List < com.dalio.cloud.model.entity.base.SysOrg>>
     * @author admin
     * @date 2022/11/18 2:26 PM
     * @create [2022/11/18 2:26 PM ] [admin] [初始创建]
     */
    @GetMapping("/baseOrg/findCompanyByEmployeeId")
    R<List<SysOrg>> findCompanyByEmployeeId(@RequestParam("employeeId") Long employeeId);
}
