package com.dalio.cloud.test.service.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dalio.basic.base.service.impl.SuperServiceImpl;
import com.dalio.cloud.test.entity.DefGenTestSimple;
import com.dalio.cloud.test.manager.DefGenTestSimpleManager;
import com.dalio.cloud.test.service.DefGenTestSimpleService;

/**
 * <p>
 * 业务实现类
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
@Transactional(readOnly = true)

public class DefGenTestSimpleServiceImpl extends SuperServiceImpl<DefGenTestSimpleManager, Long, DefGenTestSimple> implements DefGenTestSimpleService {

}


