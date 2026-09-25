package com.dalio.cloud.oauth.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.handlers.MybatisEnumTypeHandler;
import com.dalio.basic.context.ContextUtil;
import com.dalio.basic.database.mybatis.conditions.Wraps;
import com.dalio.basic.interfaces.BaseEnum;
import com.dalio.basic.utils.ClassUtils;
import com.dalio.basic.utils.StrPool;
import com.dalio.cloud.common.properties.SystemProperties;
import com.dalio.cloud.model.vo.result.Option;
import com.dalio.cloud.oauth.service.DictService;
import com.dalio.cloud.system.entity.system.DefDict;
import com.dalio.cloud.system.manager.system.DefDictManager;
import com.dalio.cloud.system.vo.result.system.DefDictItemResultVO;
import com.dalio.cloud.system.vo.result.system.DefDictResultVO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author admin
 * @date 2021/10/7 13:27
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DictServiceImpl implements DictService {
    private static final Map<Option, List<Option>> TEMP_ENUM_LIST_MAP = new HashMap<>();
    private static final Predicate<Class<?>> CLASS_FILTER = item -> item != null && item.isEnum()
            && BaseEnum.class.isAssignableFrom(item) && MybatisEnumTypeHandler.isMpEnums(item);
    private final DefDictManager defDictManager;
    private final SystemProperties systemProperties;

    private static void execI18n(Map<Serializable, DefDict> defMap, String locale, HashMap<Serializable, Object> map) {
        defMap.forEach((key, value) -> {
            String name = value.getName();
            if (StrUtil.isNotEmpty(locale)) {
                String i18nJson = value.getI18nJson();
                try {
                    JSONObject i18n = JSONUtil.parseObj(i18nJson);
                    String i18nValue = i18n.getStr(locale);
                    if (StrUtil.isNotEmpty(i18nValue)) {
                        name = i18nValue;
                    }
                } catch (Exception e) {
                    log.debug("解析i18nJson失败: {}", i18nJson, e);
                }
            }
            map.put(key, name);
        });
    }

    @PostConstruct
    public void init() {
        String enumPackage = systemProperties.getEnumPackage();
        if (StrUtil.isEmpty(enumPackage)) {
            log.warn("请在配置文件中配置{}.enumPackage", SystemProperties.PREFIX);
            return;
        }
        Set<Class<?>> enumClass = ClassUtils.scanPackage(enumPackage, CLASS_FILTER);

        StringJoiner enumSb = new StringJoiner(StrPool.COMMA);
        enumClass.forEach(item -> {
            Object[] enumConstants = item.getEnumConstants();
            BaseEnum[] baseEnums = Arrays.stream(enumConstants).map(i -> (BaseEnum) i).toArray(BaseEnum[]::new);

            Option option = new Option();
            option.setValue(item.getSimpleName());

            // 2. 获取Schema注解的title属性
            Schema schemaAnnotation = item.getAnnotation(Schema.class);
            if (schemaAnnotation != null) {
                String description = schemaAnnotation.description();
                String title = schemaAnnotation.title();
                option.setLabel(description);
                if (StrUtil.isNotEmpty(title)) {
                    option.setValue(title);
                }
            } else {
                // 3. 获取类注释的首行内容
                log.warn("{}类上没有@Schema注解", item.getSimpleName());
                option.setLabel(item.getSimpleName());
            }

            TEMP_ENUM_LIST_MAP.put(option, Option.mapOptions(baseEnums));
            enumSb.add(item.getSimpleName());
        });

        log.info("扫描: {} ,共加载了{}个枚举类, 分别为: {}", enumPackage, TEMP_ENUM_LIST_MAP.size(), enumSb);
    }

    /**
     * 查找本服务中，所有的枚举类
     *
     * @return 枚举数据
     */
    public List<DefDictResultVO> findAll() {
        if (CollUtil.isEmpty(TEMP_ENUM_LIST_MAP)) {
            return Collections.emptyList();
        }
        Map<Option, List<Option>> map = new HashMap<>(TEMP_ENUM_LIST_MAP);

        // 将枚举的value值，转为字典key
        List<String> dictKeyList = new ArrayList<>();
        map.forEach((key, value) -> dictKeyList.add(key.getValue()));

        // 查询数据库库中的所有字典和字典项（批量拉取，彻底解决 N+1 查询隐患）
        List<DefDict> existsList = defDictManager.list(Wraps.<DefDict>lbQ().in(DefDict::getKey, dictKeyList));
        List<DefDictResultVO> existsDictList = BeanUtil.copyToList(existsList, DefDictResultVO.class);
        if (CollUtil.isNotEmpty(existsDictList)) {
            List<Long> parentIds = existsDictList.stream().map(DefDictResultVO::getId).filter(Objects::nonNull).toList();
            Map<Long, List<DefDictItemResultVO>> itemMapByParentId = Collections.emptyMap();
            if (CollUtil.isNotEmpty(parentIds)) {
                List<DefDict> allItems = defDictManager.list(Wraps.<DefDict>lbQ().in(DefDict::getParentId, parentIds));
                if (CollUtil.isNotEmpty(allItems)) {
                    itemMapByParentId = allItems.stream()
                            .map(item -> BeanUtil.toBean(item, DefDictItemResultVO.class))
                            .collect(Collectors.groupingBy(DefDictItemResultVO::getParentId));
                }
            }
            for (DefDictResultVO dict : existsDictList) {
                dict.setItemList(itemMapByParentId.getOrDefault(dict.getId(), Collections.emptyList()));
            }
        }

        // 已存在的字典
        Map<String, DefDictResultVO> existingDictMap = new HashMap<>();
        // 已存在的字典项
        Map<String, DefDictItemResultVO> existingDictItemMap = new HashMap<>();
        for (DefDictResultVO sysDict : existsDictList) {
            existingDictMap.put(sysDict.getKey(), sysDict);
            List<DefDictItemResultVO> sysDictItemList = sysDict.getItemList();
            if (CollUtil.isNotEmpty(sysDictItemList)) {
                for (DefDictItemResultVO item : sysDictItemList) {
                    existingDictItemMap.put(sysDict.getKey() + item.getKey(), item);
                }
            }
        }

        List<DefDictResultVO> list = new ArrayList<>();
        map.forEach((key, value) -> {
            boolean exist = existingDictMap.containsKey(key.getValue());
            DefDictResultVO vo = new DefDictResultVO();
            vo.setKey(key.getValue());
            vo.setName(key.getLabel());
            vo.setDataType(key.getRemark());
            vo.setExist(exist);
            List<DefDictItemResultVO> itemList = new ArrayList<>();
            value.forEach(option -> {
                boolean itemExist = existingDictItemMap.containsKey(key.getValue() + option.getValue());
                DefDictItemResultVO item = new DefDictItemResultVO();
                item.setKey(option.getValue());
                item.setName(option.getLabel());
                item.setExist(itemExist);
                itemList.add(item);
            });
            vo.setItemList(itemList);
            list.add(vo);
        });
        return list;
    }

    @Override
    public void syncEnumToDict() {
        defDictManager.syncEnumToDict(TEMP_ENUM_LIST_MAP);
    }

    @Override
    public Map<Serializable, Object> findByIds(Set<Serializable> dictKeys) {
        if (CollUtil.isEmpty(dictKeys)) {
            return Collections.emptyMap();
        }

        String locale = ContextUtil.getLocale();

        // 查询不在base的字典
        Map<Serializable, DefDict> defMap = defDictManager.findByIds(dictKeys);

        HashMap<Serializable, Object> map = MapUtil.newHashMap();
        // 顺序不能乱，一定是base的覆盖def的
        execI18n(defMap, locale, map);

        return map;
    }

    @Override
    public Map<String, List<DefDictItemResultVO>> findDictItemByType(List<String> dictKeys) {
        if (CollUtil.isEmpty(dictKeys)) {
            return Collections.emptyMap();
        }

        // 查询不在base的字典
        Map<String, List<DefDictItemResultVO>> defMap = defDictManager.findDictMapItemListByKey(dictKeys);
        return defMap != null ? defMap : Collections.emptyMap();
    }
}
