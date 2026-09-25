package com.dalio.cloud.system.api;

import com.dalio.basic.base.R;
import com.dalio.basic.constant.Constants;
import com.dalio.cloud.model.entity.system.SysUser;
import com.dalio.cloud.model.vo.result.UserQuery;
import com.dalio.cloud.system.api.hystrix.DefUserApiFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户
 *
 * @author admin
 * @date 2019/07/02
 */
@FeignClient(name = "${" + Constants.PROJECT_PREFIX + ".feign.system-server:${" + Constants.PROJECT_PREFIX + ".feign.tenant-server:lamp-system-server}}", fallback = DefUserApiFallback.class)
public interface DefUserApi {
    /**
     * 查询所有的用户id
     *
     * @return 用户id
     */

    @PostMapping(value = "/defUser/findAllUserId")
    R<List<Long>> findAllUserId();


    /**
     * 根据id查询实体
     *
     * @param ids 唯一键（可能不是主键ID)
     * @return
     */
    @PostMapping("/echo/user/findByIds")
    Map<Serializable, Object> findByIds(@RequestParam(value = "ids") Set<Serializable> ids);


    /**
     * 根据id 查询用户详情
     *
     * @param userQuery 查询条件
     * @return 系统用户
     */
    @PostMapping(value = "/anyone/getSysUserById")
    R<SysUser> getById(@RequestBody UserQuery userQuery);
}
