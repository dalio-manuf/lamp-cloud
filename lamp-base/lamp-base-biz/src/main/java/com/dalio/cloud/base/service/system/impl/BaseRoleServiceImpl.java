package com.dalio.cloud.base.service.system.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Multimap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dalio.basic.base.service.impl.SuperCacheServiceImpl;
import com.dalio.basic.database.mybatis.conditions.Wraps;
import com.dalio.basic.database.mybatis.conditions.query.LbQueryWrap;
import com.dalio.basic.exception.BizException;
import com.dalio.basic.model.cache.CacheKey;
import com.dalio.basic.utils.ArgumentAssert;
import com.dalio.basic.utils.CollHelper;
import com.dalio.basic.utils.StrHelper;
import com.dalio.cloud.base.entity.system.BaseRole;
import com.dalio.cloud.base.entity.system.BaseRoleResourceRel;
import com.dalio.cloud.base.entity.user.BaseEmployeeRoleRel;
import com.dalio.cloud.base.manager.system.BaseRoleManager;
import com.dalio.cloud.base.manager.system.BaseRoleResourceRelManager;
import com.dalio.cloud.base.manager.user.BaseEmployeeRoleRelManager;
import com.dalio.cloud.base.manager.user.BaseOrgRoleRelManager;
import com.dalio.cloud.base.service.system.BaseRoleService;
import com.dalio.cloud.base.vo.save.system.BaseRoleResourceRelSaveVO;
import com.dalio.cloud.base.vo.save.system.BaseRoleSaveVO;
import com.dalio.cloud.base.vo.save.system.RoleEmployeeSaveVO;
import com.dalio.cloud.base.vo.update.system.BaseRoleUpdateVO;
import com.dalio.cloud.common.cache.base.system.RoleResourceCacheKeyBuilder;
import com.dalio.cloud.common.cache.base.user.EmployeeRoleCacheKeyBuilder;
import com.dalio.cloud.model.enumeration.base.RoleCategoryEnum;
import com.dalio.cloud.model.enumeration.system.DataTypeEnum;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 业务实现类
 * 角色
 * </p>
 *
 * @author admin
 * @date 2021-10-18
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)

public class BaseRoleServiceImpl extends SuperCacheServiceImpl<BaseRoleManager, Long, BaseRole> implements BaseRoleService {
    private final BaseEmployeeRoleRelManager baseEmployeeRoleRelManager;
    private final BaseRoleResourceRelManager baseRoleResourceRelManager;
    private final BaseOrgRoleRelManager baseOrgRoleRelManager;

    @Override
    public Boolean check(String code, Long id) {
        ArgumentAssert.notEmpty(code, "请填写角色编码");
        LbQueryWrap<BaseRole> wrap = Wraps.<BaseRole>lbQ()
                .eq(BaseRole::getCode, code).ne(id != null, BaseRole::getId, id);
        return superManager.count(wrap) > 0;
    }

    @Override
    protected <SaveVO> BaseRole saveBefore(SaveVO saveVO) {
        BaseRoleSaveVO data = (BaseRoleSaveVO) saveVO;
        ArgumentAssert.isFalse(StrUtil.isNotBlank(data.getCode()) &&
                               check(data.getCode(), null), "角色编码{}已存在", data.getCode());
        BaseRole baseRole = super.saveBefore(data);
        baseRole.setCode(StrHelper.getOrDef(data.getCode(), RandomUtil.randomString(8)));
        baseRole.setType(DataTypeEnum.BUSINESS.getCode());
        baseRole.setReadonly(false);
        return baseRole;
    }

    @Override
    protected <UpdateVO> BaseRole updateBefore(UpdateVO updateVO) {
        BaseRoleUpdateVO data = (BaseRoleUpdateVO) updateVO;
        ArgumentAssert.isFalse(StrUtil.isNotBlank(data.getCode()) &&
                               check(data.getCode(), data.getId()), "角色编码{}已存在", data.getCode());
        BaseRole baseRole = super.updateBefore(data);
        baseRole.setCode(StrHelper.getOrDef(data.getCode(), RandomUtil.randomString(8)));
        baseRole.setReadonly(false);
        return baseRole;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(Collection<Long> idList) {
        if (idList.isEmpty()) {
            return true;
        }
        List<BaseRole> baseRoles = superManager.listByIds(idList);
        long count = baseRoles.stream().filter(item -> item.getReadonly() != null && item.getReadonly()).count();
        if (count > 0) {
            throw new BizException("内置角色不允许删除");
        }

        // 员工的角色
        baseEmployeeRoleRelManager.deleteByRole(idList);
        // 组织的角色
        baseOrgRoleRelManager.deleteByRole(idList);
        // 角色的资源
        baseRoleResourceRelManager.deleteByRole(idList);
        return superManager.removeByIds(idList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> saveRoleEmployee(RoleEmployeeSaveVO saveVO) {
        if (saveVO.getFlag() == null) {
            saveVO.setFlag(true);
        }

        baseEmployeeRoleRelManager.remove(Wraps.<BaseEmployeeRoleRel>lbQ().eq(BaseEmployeeRoleRel::getRoleId, saveVO.getRoleId())
                .in(CollUtil.isNotEmpty(saveVO.getEmployeeIdList()), BaseEmployeeRoleRel::getEmployeeId, saveVO.getEmployeeIdList()));
        if (saveVO.getFlag() && CollUtil.isNotEmpty(saveVO.getEmployeeIdList())) {
            List<BaseEmployeeRoleRel> list = saveVO.getEmployeeIdList().stream().map(employeeId ->
                    BaseEmployeeRoleRel.builder().employeeId(employeeId).roleId(saveVO.getRoleId()).build()).toList();
            baseEmployeeRoleRelManager.saveBatch(list);
        }

        if (CollUtil.isNotEmpty(saveVO.getEmployeeIdList())) {
            CacheKey[] cacheKeys = saveVO.getEmployeeIdList().stream().map(EmployeeRoleCacheKeyBuilder::build).toArray(CacheKey[]::new);
            cacheOps.del(cacheKeys);
        }
        return findEmployeeIdByRoleId(saveVO.getRoleId());
    }

    @Override
    public List<Long> findEmployeeIdByRoleId(Long roleId) {
        return baseEmployeeRoleRelManager.listObjs(Wraps.<BaseEmployeeRoleRel>lbQ()
                        .select(BaseEmployeeRoleRel::getEmployeeId).eq(BaseEmployeeRoleRel::getRoleId, roleId),
                Convert::toLong);
    }

    @Override
    public Map<Long, Collection<Long>> findResourceIdByRoleId(Long roleId, RoleCategoryEnum category) {
        if (category == null) {
            category = RoleCategoryEnum.FUNCTION;
        }
        List<BaseRoleResourceRel> list = baseRoleResourceRelManager.findByRoleIdAndCategory(roleId, category.getCode());
        Multimap<Long, Long> map = CollHelper.iterableToMultiMap(list, BaseRoleResourceRel::getApplicationId, BaseRoleResourceRel::getResourceId);
        return map.asMap();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveRoleResource(BaseRoleResourceRelSaveVO saveVO) {
        Map<Long, List<Long>> applicationResourceMap = saveVO.getApplicationResourceMap();
        baseRoleResourceRelManager.remove(Wraps.<BaseRoleResourceRel>lbQ().eq(BaseRoleResourceRel::getRoleId, saveVO.getRoleId()));
        if (CollUtil.isEmpty(applicationResourceMap)) {
            return false;
        }

        List<BaseRoleResourceRel> list = new ArrayList<>();
        List<CacheKey> keys = new ArrayList<>();
        applicationResourceMap.forEach((applicationId, resourceList) -> {
            if (CollUtil.isNotEmpty(resourceList)) {
                for (Long resourceId : resourceList) {
                    BaseRoleResourceRel baseRoleResRel = new BaseRoleResourceRel();
                    baseRoleResRel.setRoleId(saveVO.getRoleId());
                    baseRoleResRel.setResourceId(resourceId);
                    baseRoleResRel.setApplicationId(applicationId);
                    list.add(baseRoleResRel);
                }
            }
            keys.add(RoleResourceCacheKeyBuilder.build(applicationId, saveVO.getRoleId()));
        });
        keys.add(RoleResourceCacheKeyBuilder.build(null, saveVO.getRoleId()));
        boolean flag = baseRoleResourceRelManager.saveBatch(list);

        cacheOps.del(keys);
        return flag;
    }

    @Override
    public List<Long> findResourceIdByEmployeeId(Long applicationId, Long employeeId) {
        return superManager.findResourceIdByEmployeeId(applicationId, employeeId);
    }

    @Override
    public boolean checkRole(Long employeeId, String... codes) {
        return superManager.checkRole(employeeId, codes);
    }

    @Override
    public List<String> findRoleCodeByEmployeeId(Long employeeId) {
        List<BaseRole> list = superManager.findRoleByEmployeeId(employeeId);
        return list.stream().map(BaseRole::getCode).toList();
    }
}
