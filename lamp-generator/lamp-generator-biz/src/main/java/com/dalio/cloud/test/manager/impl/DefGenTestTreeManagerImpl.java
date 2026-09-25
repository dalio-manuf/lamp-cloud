package com.dalio.cloud.test.manager.impl;

import com.dalio.basic.base.manager.impl.SuperManagerImpl;
import com.dalio.cloud.test.entity.DefGenTestTree;
import com.dalio.cloud.test.manager.DefGenTestTreeManager;
import com.dalio.cloud.test.mapper.DefGenTestTreeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 通用业务实现类
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
public class DefGenTestTreeManagerImpl extends SuperManagerImpl<DefGenTestTreeMapper, DefGenTestTree> implements DefGenTestTreeManager {

}


