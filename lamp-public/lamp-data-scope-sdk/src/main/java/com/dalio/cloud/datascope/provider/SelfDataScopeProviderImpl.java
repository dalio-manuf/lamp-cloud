package com.dalio.cloud.datascope.provider;

import com.dalio.basic.base.entity.SuperEntity;
import com.dalio.basic.context.ContextUtil;
import com.dalio.cloud.datascope.model.DataFieldProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 个人的数据
 *
 * @author admin
 * @date 2022/1/9 23:29
 */
@Slf4j
@RequiredArgsConstructor
@Component("DATA_SCOPE_06")
public class SelfDataScopeProviderImpl implements DataScopeProvider {

    @Override
    public List<DataFieldProperty> findDataFieldProperty(List<DataFieldProperty> fsp) {
        List<Long> employeeIdList = Collections.singletonList(ContextUtil.getUserId());
        fsp.forEach(item -> {
            item.setField(SuperEntity.CREATED_BY_FIELD);
            item.setValues(employeeIdList);
        });
        return fsp;
    }
}
