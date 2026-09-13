package com.dalio.cloud.oauth.service;

import com.dalio.cloud.base.entity.user.BaseOrg;
import com.dalio.cloud.oauth.vo.param.RegisterByEmailVO;
import com.dalio.cloud.oauth.vo.param.RegisterByMobileVO;
import com.dalio.cloud.oauth.vo.result.OrgResultVO;

import java.util.List;
import java.util.Map;

/**
 * @author admin
 * @version v1.0
 * @date 2022/9/16 12:21 PM
 * @create [2022/9/16 12:21 PM ] [admin] [初始创建]
 */
public interface UserInfoService {
    /**
     * 根据单位ID查找部门
     *
     * @param companyId 单位ID
     * @param employeeId  员工id
     * @return java.util.List<com.dalio.cloud.model.entity.base.SysOrg>
     * @author admin
     * @date 2022/9/29 11:18 PM
     * @create [2022/9/29 11:18 PM ] [admin] [初始创建]
     */
    List<BaseOrg> findDeptByCompany(Long companyId, Long employeeId);

    /**
     * 查询单位和部门信息
     *
     * @return com.dalio.cloud.oauth.vo.result.OrgResultVO
     * @author admin
     * @date 2022/9/15 2:37 PM
     * @create [2022/9/15 2:37 PM ] [admin] [初始创建]
     */
    OrgResultVO findCompanyAndDept();


    /**
     * 注册
     *
     * @param register 注册
     * @return
     */
    String registerByMobile(RegisterByMobileVO register);

    /**
     * 注册
     *
     * @param register 注册
     * @return
     */
    String registerByEmail(RegisterByEmailVO register);

    /**
     * 【演示专用接口】 注册临时管理员账号密码
     *
     * @param type 账号类型
     * @return
     */
    Map<String, Object> registerTempAdmin(String type);
}
