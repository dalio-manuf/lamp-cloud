package com.dalio.cloud.test.service;

import com.dalio.basic.base.service.SuperService;
import com.dalio.cloud.test.entity.DefGenTestTree;
import com.dalio.cloud.test.vo.query.DefGenTestTreePageQuery;

import java.util.List;


/**
 * <p>
 * 业务接口
 * 测试树结构
 * </p>
 *
 * @author admin
 * @date 2022-04-20 00:28:30
 * @create [2022-04-20 00:28:30] [admin] [代码生成器生成]
 */
public interface DefGenTestTreeService extends SuperService<Long, DefGenTestTree> {

    /**
     * 查询树结构
     *
     * @param query 参数
     * @return 树
     */
    List<DefGenTestTree> findTree(DefGenTestTreePageQuery query);
}


