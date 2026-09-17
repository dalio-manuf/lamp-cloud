package com.dalio.cloud.base.service.user.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dalio.basic.base.request.PageParams;
import com.dalio.basic.base.service.impl.SuperCacheServiceImpl;
import com.dalio.basic.context.ContextUtil;
import com.dalio.basic.database.mybatis.conditions.Wraps;
import com.dalio.basic.database.mybatis.conditions.query.LbQueryWrap;
import com.dalio.basic.utils.ArgumentAssert;
import com.dalio.cloud.base.entity.user.BaseEmployee;
import com.dalio.cloud.base.entity.user.BaseEmployeeOrgRel;
import com.dalio.cloud.base.entity.user.BaseEmployeeRoleRel;
import com.dalio.cloud.base.manager.user.BaseEmployeeManager;
import com.dalio.cloud.base.manager.user.BaseEmployeeOrgRelManager;
import com.dalio.cloud.base.manager.user.BaseEmployeeRoleRelManager;
import com.dalio.cloud.base.service.user.BaseEmployeeService;
import com.dalio.cloud.base.vo.query.user.BaseEmployeePageQuery;
import com.dalio.cloud.base.vo.result.user.BaseEmployeeResultVO;
import com.dalio.cloud.base.vo.save.user.BaseEmployeeRoleRelSaveVO;
import com.dalio.cloud.base.vo.save.user.BaseEmployeeSaveVO;
import com.dalio.cloud.base.vo.update.user.BaseEmployeeUpdateVO;
import com.dalio.cloud.common.cache.base.user.EmployeeOrgCacheKeyBuilder;
import com.dalio.cloud.common.cache.base.user.EmployeeRoleCacheKeyBuilder;
import com.dalio.cloud.common.constant.RoleConstant;

import java.util.Collection;
import java.util.List;

/**
 * <p>
 * 业务实现类
 * 员工
 * </p>
 *
 * @author admin
 * @date 2021-10-18
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)

public class BaseEmployeeServiceImpl extends SuperCacheServiceImpl<BaseEmployeeManager, Long, BaseEmployee> implements BaseEmployeeService {
    private final BaseEmployeeRoleRelManager baseEmployeeRoleRelManager;
    private final BaseEmployeeOrgRelManager baseEmployeeOrgRelManager;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrgInfo(Long id, Long lastCompanyId, Long lastDeptId) {
        superManager.update(Wrappers.<BaseEmployee>lambdaUpdate().set(BaseEmployee::getLastCompanyId, lastCompanyId)
                .set(BaseEmployee::getLastDeptId, lastDeptId).eq(BaseEmployee::getId, id));
        superManager.delCache(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatch(Collection<BaseEmployee> entityList) {
        return superManager.saveBatch(entityList);
    }

    @Override
    public IPage<BaseEmployeeResultVO> findPageResultVO(PageParams<BaseEmployeePageQuery> params) {
        IPage<BaseEmployee> page = params.buildPage(BaseEmployee.class);
        BaseEmployeePageQuery model = params.getModel();
        LbQueryWrap<BaseEmployee> wrap = Wraps.lbQ();
        wrap.like(BaseEmployee::getRealName, model.getRealName())
                .eq(BaseEmployee::getPositionStatus, model.getPositionStatus())
                .eq(BaseEmployee::getPositionId, model.getPositionId())
                .eq(BaseEmployee::getActiveStatus, model.getActiveStatus())
                .eq(BaseEmployee::getState, model.getState())
                .in(CollUtil.isNotEmpty(model.getUserIdList()), BaseEmployee::getUserId, model.getUserIdList());

        return superManager.selectPageResultVO(page, wrap, model);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> saveEmployeeRole(BaseEmployeeRoleRelSaveVO saveVO) {
        if (saveVO.getFlag() == null) {
            saveVO.setFlag(true);
        }

        baseEmployeeRoleRelManager.remove(Wraps.<BaseEmployeeRoleRel>lbQ().eq(BaseEmployeeRoleRel::getEmployeeId, saveVO.getEmployeeId())
                .in(CollUtil.isNotEmpty(saveVO.getRoleIdList()), BaseEmployeeRoleRel::getRoleId, saveVO.getRoleIdList()));

        if (saveVO.getFlag() && CollUtil.isNotEmpty(saveVO.getRoleIdList())) {
            List<BaseEmployeeRoleRel> list = saveVO.getRoleIdList().stream()
                    .map(roleId -> BaseEmployeeRoleRel.builder()
                            .roleId(roleId).employeeId(saveVO.getEmployeeId())
                            .build()).toList();
            baseEmployeeRoleRelManager.saveBatch(list);
        }

        cacheOps.del(EmployeeRoleCacheKeyBuilder.build(saveVO.getEmployeeId()));
        return findEmployeeRoleByEmployeeId(saveVO.getEmployeeId());
    }

    @Override
    public List<Long> findEmployeeRoleByEmployeeId(Long employeeId) {
        return baseEmployeeRoleRelManager.listObjs(Wrappers.<BaseEmployeeRoleRel>lambdaQuery()
                        .select(BaseEmployeeRoleRel::getRoleId)
                        .eq(BaseEmployeeRoleRel::getEmployeeId, employeeId),
                Convert::toLong
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <SaveVO> BaseEmployee save(SaveVO saveVO) {
        BaseEmployeeSaveVO employeeSaveVO = (BaseEmployeeSaveVO) saveVO;
        BaseEmployee baseEmployee = BeanUtil.toBean(employeeSaveVO, BaseEmployee.class);
        baseEmployee.setCreatedOrgId(ContextUtil.getCurrentDeptId());
        superManager.save(baseEmployee);
        List<Long> orgIdList = employeeSaveVO.getOrgIdList();
        saveEmployeeOrg(baseEmployee, orgIdList);
        return baseEmployee;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public <UpdateVO> BaseEmployee updateById(UpdateVO updateVO) {
        BaseEmployeeUpdateVO employeeUpdateVO = (BaseEmployeeUpdateVO) updateVO;
        BaseEmployee baseEmployee = BeanUtil.toBean(updateVO, BaseEmployee.class);
        superManager.updateById(baseEmployee);
        List<Long> orgIdList = employeeUpdateVO.getOrgIdList();
        if (orgIdList != null) {
            saveEmployeeOrg(baseEmployee, orgIdList);
        }
        return baseEmployee;
    }

    private void saveEmployeeOrg(BaseEmployee baseEmployee, List<Long> orgIdList) {
        baseEmployeeOrgRelManager.removeByEmployeeId(baseEmployee.getId());
        if (CollUtil.isNotEmpty(orgIdList)) {
            List<BaseEmployeeOrgRel> eoList = orgIdList.stream().map(orgId ->
                    BaseEmployeeOrgRel.builder()
                            .employeeId(baseEmployee.getId())
                            .orgId(orgId)
                            .build()).toList();
            baseEmployeeOrgRelManager.saveBatch(eoList);
        }

        cacheOps.del(EmployeeOrgCacheKeyBuilder.build(baseEmployee.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(Collection<Long> idList) {
        if (CollUtil.isEmpty(idList)) {
            return false;
        }
        boolean flag = superManager.removeByIds(idList);
        baseEmployeeOrgRelManager.removeByEmployeeIds(idList);
        baseEmployeeRoleRelManager.removeByEmployeeIds(idList);

        List<com.dalio.basic.model.cache.CacheKey> keys = new java.util.ArrayList<>(idList.size() * 2);
        for (Long employeeId : idList) {
            keys.add(EmployeeRoleCacheKeyBuilder.build(employeeId));
            keys.add(EmployeeOrgCacheKeyBuilder.build(employeeId));
        }
        cacheOps.del(keys);
        return flag;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatchBaseEmployeeAndRole(List<BaseEmployee> employeeList) {
        ArgumentAssert.notEmpty(employeeList, "员工列表不能为空");
        superManager.saveBatch(employeeList);

        List<Long> employeeIdList = employeeList.stream().map(BaseEmployee::getId).toList();
        return baseEmployeeRoleRelManager.bindRole(employeeIdList, RoleConstant.TENANT_ADMIN);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(BaseEmployee baseEmployee) {
        return superManager.updateById(baseEmployee);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateAllById(BaseEmployee baseEmployee) {
        return superManager.updateAllById(baseEmployee);
    }

    @Override
    public BaseEmployee getEmployeeByUser(Long userId) {
        return superManager.getEmployeeByUser(userId);
    }

    @Override
    public List<BaseEmployeeResultVO> listEmployeeByUserId(Long userId) {
        return superManager.listEmployeeByUserId(userId);
    }
}
