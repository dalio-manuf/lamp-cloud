package com.dalio.cloud.base.manager.system;

import com.dalio.basic.base.manager.SuperManager;
import com.dalio.cloud.base.entity.system.BaseRoleResourceRel;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 通用业务接口
 * 角色的资源
 * </p>
 *
 * @author admin
 * @date 2021-10-18
 */
public interface BaseRoleResourceRelManager extends SuperManager<BaseRoleResourceRel> {

    /**
     * 根据角色id和角色类别，查询角色拥有的权限
     *
     * @param roleId   角色ID
     * @param category 角色类别
     * @return
     */
    List<BaseRoleResourceRel> findByRoleIdAndCategory(Long roleId, String category);

    /**
     * 根据角色ID删除角色的资源
     *
     * @param roleIdList idList
     * @author admin
     * @date 2022/10/25 10:50 PM
     * @create [2022/10/25 10:50 PM ] [admin] [初始创建]
     */
    void deleteByRole(Collection<Long> roleIdList);
}
