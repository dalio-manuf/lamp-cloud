package com.dalio.cloud.test.service.impl;

import com.dalio.basic.base.service.impl.SuperServiceImpl;
import com.dalio.basic.database.mybatis.conditions.Wraps;
import com.dalio.basic.utils.TreeUtil;
import com.dalio.cloud.test.entity.DefGenTestTree;
import com.dalio.cloud.test.manager.DefGenTestTreeManager;
import com.dalio.cloud.test.service.DefGenTestTreeService;
import com.dalio.cloud.test.vo.query.DefGenTestTreePageQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 业务实现类
 * 测试树结构
 * </p>
 *
 * @author admin
 * @date 2022-04-20 00:28:30
 * @create [2022-04-20 00:28:30] [admin] [代码生成器生成]
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DefGenTestTreeServiceImpl extends SuperServiceImpl<DefGenTestTreeManager, Long, DefGenTestTree> implements DefGenTestTreeService {

    @Override
    public List<DefGenTestTree> findTree(DefGenTestTreePageQuery query) {
        List<DefGenTestTree> list = superManager.list(Wraps.<DefGenTestTree>lbQ().orderByAsc(DefGenTestTree::getSortValue));
        return TreeUtil.buildTree(list);
    }

}


