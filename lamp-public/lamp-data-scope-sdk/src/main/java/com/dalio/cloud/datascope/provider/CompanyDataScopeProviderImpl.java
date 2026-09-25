package com.dalio.cloud.datascope.provider;

import com.dalio.basic.base.entity.SuperEntity;
import com.dalio.basic.context.ContextUtil;
import com.dalio.cloud.datascope.model.DataFieldProperty;
import com.dalio.cloud.datascope.service.OrgHelperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 本单位
 *
 * @author admin
 * @date 2022/1/9 23:29
 */
@Slf4j
@RequiredArgsConstructor
@Component("DATA_SCOPE_03")
public class CompanyDataScopeProviderImpl implements DataScopeProvider {

    private final OrgHelperService orgHelperService;

    @Override
    public List<DataFieldProperty> findDataFieldProperty(List<DataFieldProperty> fsp) {
        Long mainCompanyId = orgHelperService.getMainCompanyIdByEmployeeId(ContextUtil.getEmployeeId());
        if (mainCompanyId == null) {
            return Collections.emptyList();
        }
        List<Long> orgIdList = Collections.singletonList(mainCompanyId);
        fsp.forEach(item -> {
            item.setField(SuperEntity.CREATED_ORG_ID_FIELD);
            item.setValues(orgIdList);
        });
        return fsp;
    }
}
