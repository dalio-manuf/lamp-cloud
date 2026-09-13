package com.dalio.cloud.system.manager.system;

import com.dalio.basic.base.manager.SuperManager;
import com.dalio.cloud.model.vo.result.Option;
import com.dalio.cloud.system.entity.system.DefDict;
import com.dalio.cloud.system.vo.result.system.DefDictItemResultVO;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 字典管理
 *
 * @author admin
 * @version v1.0
 * @date 2021/9/29 1:26 下午
 * @create [2021/9/29 1:26 下午 ] [admin] [初始创建]
 */
public interface DefDictManager extends SuperManager<DefDict> {
    Map<Serializable, DefDict> findByIds(Set<Serializable> dictKeys);

    void syncEnumToDict(Map<Option, List<Option>> ennumMap);

    /**
     * 根据字典key查询系统默认的字典条目
     *
     * @param dictKeys 字典key
     * @return key： 字典key  value: item list
     */
    Map<String, List<DefDictItemResultVO>> findDictMapItemListByKey(List<String> dictKeys);

    /**
     * 删除字典条目
     *
     * @param idList idList
     * @return boolean
     * @author admin
     * @date 2022/4/18 1:34 PM
     * @create [2022/4/18 1:34 PM ] [admin] [初始创建]
     */
    boolean removeItemByIds(Collection<Long> idList);
}
