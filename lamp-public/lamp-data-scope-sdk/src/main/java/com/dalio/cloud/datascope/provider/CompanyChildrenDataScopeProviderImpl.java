package com.dalio.cloud.datascope.provider;

import cn.hutool.core.collection.CollUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.dalio.basic.base.entity.SuperEntity;
import com.dalio.basic.context.ContextUtil;
import com.dalio.cloud.datascope.model.DataFieldProperty;
import com.dalio.cloud.datascope.service.OrgHelperService;

import java.util.Collections;
import java.util.List;

/**
 * 本单位及子级
 *
 * @author admin
 * @date 2022/1/9 23:29
 */
@Slf4j
@RequiredArgsConstructor
@Component("DATA_SCOPE_02")
public class CompanyChildrenDataScopeProviderImpl implements DataScopeProvider {
    private final OrgHelperService orgHelperService;

    @Override
    public List<DataFieldProperty> findDataFieldProperty(List<DataFieldProperty> fsp) {
        List<Long> employeeIdList = orgHelperService.findCompanyAndChildrenIdByEmployeeId(ContextUtil.getEmployeeId());
        if (CollUtil.isEmpty(employeeIdList)) {
            return Collections.emptyList();
        }
        fsp.forEach(item -> {
            item.setField(SuperEntity.CREATED_ORG_ID_FIELD);
            item.setValues(employeeIdList);
        });
        return fsp;
    }
}
