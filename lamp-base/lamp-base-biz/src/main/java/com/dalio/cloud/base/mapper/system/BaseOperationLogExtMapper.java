package com.dalio.cloud.base.mapper.system;

import com.dalio.basic.base.mapper.SuperMapper;
import com.dalio.cloud.base.entity.system.BaseOperationLogExt;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * Mapper 接口
 * 操作日志
 * </p>
 *
 * @author admin
 * @date 2021-11-08
 */
@Repository
public interface BaseOperationLogExtMapper extends SuperMapper<BaseOperationLogExt> {
    /**
     * 清理日志
     *
     * @param clearBeforeTime 多久之前的
     * @param idList          待删除
     * @return 是否成功
     */
    Long clearLog(@Param("clearBeforeTime") LocalDateTime clearBeforeTime, @Param("idList") List<Long> idList);
}
