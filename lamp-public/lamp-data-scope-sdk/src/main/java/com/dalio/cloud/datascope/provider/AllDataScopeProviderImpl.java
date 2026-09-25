package com.dalio.cloud.datascope.provider;

import com.dalio.cloud.datascope.model.DataFieldProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 全部
 *
 * @author admin
 * @date 2022/1/9 23:29
 */
@Slf4j
@RequiredArgsConstructor
@Component("DATA_SCOPE_01")
public class AllDataScopeProviderImpl implements DataScopeProvider {

    @Override
    public List<DataFieldProperty> findDataFieldProperty(List<DataFieldProperty> fsp) {
        return Collections.emptyList();
    }
}
