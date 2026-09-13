package com.dalio.cloud.oauth.service;

import com.dalio.basic.interfaces.echo.LoadService;
import com.dalio.cloud.system.vo.result.system.DefDictItemResultVO;
import com.dalio.cloud.system.vo.result.system.DefDictResultVO;

import java.util.List;
import java.util.Map;

/**
 * 字典查询服务
 *
 * @author admin
 * @date 2021/10/7 13:27
 */
public interface DictService extends LoadService {
    List<DefDictResultVO> findAll();

    Map<String, List<DefDictItemResultVO>> findDictItemByType(List<String> query);

    void syncEnumToDict();
}
