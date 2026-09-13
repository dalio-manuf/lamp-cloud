package com.dalio.cloud.test.manager.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.dalio.basic.base.manager.impl.SuperManagerImpl;
import com.dalio.cloud.test.entity.DefGenTestSimple;
import com.dalio.cloud.test.manager.DefGenTestSimpleManager;
import com.dalio.cloud.test.mapper.DefGenTestSimpleMapper;

/**
 * <p>
 * 通用业务实现类
 * 测试单表
 * </p>
 *
 * @author admin
 * @date 2022-04-15 15:36:45
 * @create [2022-04-15 15:36:45] [admin] [代码生成器生成]
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefGenTestSimpleManagerImpl extends SuperManagerImpl<DefGenTestSimpleMapper, DefGenTestSimple> implements DefGenTestSimpleManager {

}


