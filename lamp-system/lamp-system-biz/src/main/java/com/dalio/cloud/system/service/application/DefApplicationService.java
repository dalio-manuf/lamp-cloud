package com.dalio.cloud.system.service.application;

import com.dalio.basic.base.service.SuperCacheService;
import com.dalio.cloud.system.entity.application.DefApplication;
import com.dalio.cloud.system.vo.result.application.ApplicationResourceResultVO;
import com.dalio.cloud.system.vo.result.application.DefApplicationResultVO;

import java.util.List;

/**
 * <p>
 * 业务接口
 * 应用
 * </p>
 *
 * @author admin
 * @date 2021-09-15
 */
public interface DefApplicationService extends SuperCacheService<Long, DefApplication> {

    /**
     * id!=null：排除此id，检测name是否重复
     * id==null: 直接检测name是否重复
     *
     * @param id   应用id
     * @param name 应用名
     * @return java.lang.Boolean
     * @author admin
     * @date 2021/9/15 6:46 下午
     * @create [2021/9/15 6:46 下午 ] [admin] [初始创建]
     */
    Boolean check(Long id, String name);

    /**
     * 查询全部应用资源列表
     *
     * @return java.util.List<com.dalio.cloud.tenant.vo.result.tenant.ApplicationResourceResultVO>
     * @author admin
     * @date 2021/9/27 11:39 下午
     * @create [2021/9/27 11:39 下午 ] [admin] [初始创建]
     */
    List<ApplicationResourceResultVO> findApplicationResourceList();

    /**
     * 查询可用的应用资源列表
     *
     * @return java.util.List<com.dalio.cloud.tenant.vo.result.tenant.ApplicationResourceResultVO>
     * @author admin
     * @date 2021/9/27 11:39 下午
     * @create [2021/9/27 11:39 下午 ] [admin] [初始创建]
     */
    List<ApplicationResourceResultVO> findAvailableApplicationResourceList();

    /**
     * 查询我的应用
     *
     * @param name 应用名
     * @return
     */
    List<DefApplicationResultVO> findMyApplication(String name);

    /**
     * 查询推荐应用
     *
     * @param name 应用名
     * @return
     */
    List<DefApplicationResultVO> findRecommendApplication(String name);

    /**
     * 查询可用的应用数据权限列表
     *
     * @return java.util.List<com.dalio.cloud.tenant.vo.result.tenant.ApplicationResourceResultVO>
     * @author admin
     * @date 2021/9/27 11:39 下午
     * @create [2021/9/27 11:39 下午 ] [admin] [初始创建]
     */
    List<ApplicationResourceResultVO> findAvailableApplicationDataScopeList();

    /**
     * 修改默认应用
     *
     * @param applicationId 应用Id
     * @param userId        用户ID
     * @return
     */
    Boolean updateDefApp(Long applicationId, Long userId);

    /**
     * 查询默认应用
     *
     * @param userId
     * @return
     */
    DefApplication getDefApp(Long userId);

}
