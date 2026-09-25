package com.dalio.cloud.system.mapper.application;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.dalio.basic.base.mapper.SuperMapper;
import com.dalio.cloud.system.entity.application.DefApplication;
import com.dalio.cloud.system.vo.result.application.DefApplicationResultVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * 应用
 * </p>
 *
 * @author admin
 * @date 2021-09-15
 */
@Repository
@InterceptorIgnore(tenantLine = "true", dynamicTableName = "true")
public interface DefApplicationMapper extends SuperMapper<DefApplication> {

    /**
     * 查询我的应用
     *
     * @param name 应用名
     * @return
     */
    List<DefApplicationResultVO> findMyApplication(@Param("name") String name);

}
