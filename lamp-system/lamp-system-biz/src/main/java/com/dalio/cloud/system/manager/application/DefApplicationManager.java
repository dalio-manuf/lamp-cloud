package com.dalio.cloud.system.manager.application;

import com.dalio.basic.base.manager.SuperCacheManager;
import com.dalio.basic.interfaces.echo.LoadService;
import com.dalio.cloud.system.entity.application.DefApplication;
import com.dalio.cloud.system.vo.result.application.DefApplicationResultVO;

import java.util.List;

/**
 * 应用管理
 *
 * @author admin
 * @version v1.0
 * @date 2021/9/29 1:26 下午
 * @create [2021/9/29 1:26 下午 ] [admin] [初始创建]
 */
public interface DefApplicationManager extends SuperCacheManager<DefApplication>, LoadService {

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
     * 查询公共应用
     *
     * @return
     */
    List<DefApplication> findGeneral();

}
