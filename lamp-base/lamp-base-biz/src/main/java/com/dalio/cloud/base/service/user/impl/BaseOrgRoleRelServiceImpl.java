package com.dalio.cloud.base.service.user.impl;


import com.dalio.basic.base.service.impl.SuperServiceImpl;
import com.dalio.cloud.base.entity.user.BaseOrgRoleRel;
import com.dalio.cloud.base.manager.user.BaseOrgRoleRelManager;
import com.dalio.cloud.base.service.user.BaseOrgRoleRelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * <p>
 * 业务实现类
 * 组织的角色
 * </p>
 *
 * @author admin
 * @date 2021-10-18
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)

public class BaseOrgRoleRelServiceImpl extends SuperServiceImpl<BaseOrgRoleRelManager, Long, BaseOrgRoleRel> implements BaseOrgRoleRelService {

}
