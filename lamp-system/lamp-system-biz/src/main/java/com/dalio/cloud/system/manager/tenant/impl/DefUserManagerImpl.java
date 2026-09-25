package com.dalio.cloud.system.manager.tenant.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.dalio.basic.base.manager.impl.SuperCacheManagerImpl;
import com.dalio.basic.cache.redis2.CacheResult;
import com.dalio.basic.database.mybatis.conditions.Wraps;
import com.dalio.basic.model.cache.CacheKey;
import com.dalio.basic.model.cache.CacheKeyBuilder;
import com.dalio.basic.utils.ArgumentAssert;
import com.dalio.basic.utils.CollHelper;
import com.dalio.cloud.common.cache.tenant.base.*;
import com.dalio.cloud.system.entity.tenant.DefUser;
import com.dalio.cloud.system.manager.tenant.DefUserManager;
import com.dalio.cloud.system.mapper.tenant.DefUserMapper;
import com.dalio.cloud.system.vo.query.tenant.DefUserPageQuery;
import com.dalio.cloud.system.vo.result.tenant.DefUserResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 应用管理
 *
 * @author admin
 * @version v1.0
 * @date 2021/9/29 1:26 下午
 * @create [2021/9/29 1:26 下午 ] [admin] [初始创建]
 */
@RequiredArgsConstructor
@Service
public class DefUserManagerImpl extends SuperCacheManagerImpl<DefUserMapper, DefUser> implements DefUserManager {
    @Override
    protected CacheKeyBuilder cacheKeyBuilder() {
        return new DefUserCacheKeyBuilder();
    }

    @Transactional(readOnly = true)
    @Override

    public Map<Serializable, Object> findByIds(Set<Serializable> ids) {
        List<DefUser> list = findByIds(ids, null).stream().filter(Objects::nonNull).toList();
        return CollHelper.uniqueIndex(list, DefUser::getId, DefUser::getNickName);
    }


    @Override
    public IPage<DefUserResultVO> pageUser(DefUserPageQuery pageQuery, IPage<DefUser> page) {
        return baseMapper.pageUser(pageQuery, page);
    }

    @Override
    public int resetPassErrorNum(Long id) {
        return baseMapper.resetPassErrorNum(id, LocalDateTime.now());
    }

    @Override
    public void incrPasswordErrorNumById(Long id) {
        baseMapper.incrPasswordErrorNumById(id, LocalDateTime.now());
    }


    @Override
    public boolean checkUsername(String value, Long id) {
        return count(Wraps.<DefUser>lbQ().eq(DefUser::getUsername, value).ne(DefUser::getId, id)) > 0;
    }

    @Override
    public boolean checkEmail(String value, Long id) {
        return count(Wraps.<DefUser>lbQ().eq(DefUser::getEmail, value).ne(DefUser::getId, id)) > 0;
    }

    @Override
    public boolean checkMobile(String value, Long id) {
        return count(Wraps.<DefUser>lbQ().eq(DefUser::getMobile, value).ne(DefUser::getId, id)) > 0;
    }

    @Override
    public boolean checkIdCard(String value, Long id) {
        return count(Wraps.<DefUser>lbQ().eq(DefUser::getIdCard, value).ne(DefUser::getId, id)) > 0;
    }


    @Override
    public DefUser getUserByUsername(String username) {
        CacheKey key = DefUserUserNameCacheKeyBuilder.builder(username);
        return getDefUser(key, username, DefUser::getUsername);
    }

    @Override
    public DefUser getUserByMobile(String mobile) {
        CacheKey key = DefUserMobileCacheKeyBuilder.builder(mobile);
        return getDefUser(key, mobile, DefUser::getMobile);
    }

    @Override
    public DefUser getUserByEmail(String email) {
        CacheKey key = DefUserEmailCacheKeyBuilder.builder(email);
        return getDefUser(key, email, DefUser::getEmail);
    }

    @Override
    public DefUser getUserByIdCard(String idCard) {
        CacheKey key = DefUserIdCardCacheKeyBuilder.builder(idCard);
        return getDefUser(key, idCard, DefUser::getIdCard);
    }


    private DefUser getDefUser(CacheKey key, String value, SFunction<DefUser, ?> fun) {
        CacheResult<Long> result = cacheOps.get(key, k -> {
            DefUser defUser = getOne(Wrappers.<DefUser>lambdaQuery().eq(fun, value), false);
            return defUser != null ? defUser.getId() : null;
        });
        return getByIdCache(result.getValue());
    }

    @Override
    public boolean removeById(DefUser entity) {
        delUserCache(Collections.singletonList(entity.getId()));
        return super.removeById(entity);
    }


    @Override
    public boolean removeByIds(Collection<?> list, boolean useFill) {
        delUserCache(list);
        return super.removeByIds(list, useFill);
    }

    @Override
    public boolean removeBatchByIds(Collection<?> list) {
        delUserCache(list);
        return super.removeBatchByIds(list);
    }


    @Override
    public void delUserCache(Collection<?> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        List<Long> idList = new ArrayList<>();
        for (Object o : list) {
            if (o instanceof DefUser user) {
                idList.add(user.getId());
            } else {
                idList.add(Convert.toLong(o));
            }
        }
        List<DefUser> defUsers = listByIds(idList);
        ArgumentAssert.notEmpty(defUsers, "待删除数据不存在");
        List<CacheKey> keyList = new ArrayList<>();
        for (DefUser defUser : defUsers) {
            CacheKey idCardKey = DefUserIdCardCacheKeyBuilder.builder(defUser.getIdCard());
            CacheKey mobileKey = DefUserMobileCacheKeyBuilder.builder(defUser.getMobile());
            CacheKey emailKey = DefUserEmailCacheKeyBuilder.builder(defUser.getEmail());
            CacheKey usernameKey = DefUserUserNameCacheKeyBuilder.builder(defUser.getUsername());
            keyList.add(idCardKey);
            keyList.add(mobileKey);
            keyList.add(emailKey);
            keyList.add(usernameKey);
        }

        cacheOps.del(keyList);
    }
}
