package com.dalio.cloud.system.mapper.system;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import com.dalio.basic.base.mapper.SuperMapper;
import com.dalio.cloud.system.entity.system.DefLoginLog;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * Mapper 接口
 * 登录日志
 * </p>
 *
 * @author admin
 * @date 2021-11-12
 */
@Repository
@InterceptorIgnore(tenantLine = "true", dynamicTableName = "true")
public interface DefLoginLogMapper extends SuperMapper<DefLoginLog> {
    /**
     * 清理日志
     *
     * @param clearBeforeTime 多久之前的
     * @param cutoffId        截断保留点ID（小于该ID的被清理）
     * @param idList          待保留排除的ID列表
     * @return 是否成功
     */
    Long clearLog(@Param("clearBeforeTime") LocalDateTime clearBeforeTime, @Param("cutoffId") Long cutoffId, @Param("idList") List<Long> idList);
}
