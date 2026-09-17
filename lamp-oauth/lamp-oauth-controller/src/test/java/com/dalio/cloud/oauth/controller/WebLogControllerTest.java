package com.dalio.cloud.oauth.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import com.dalio.basic.base.R;
import com.dalio.basic.model.log.OptLogDTO;
import com.dalio.cloud.base.service.system.BaseOperationLogService;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

/**
 * WebLogController 单元测试
 */
class WebLogControllerTest {

    @Test
    @DisplayName("测试 save 系统访问日志接口")
    void testSave() {
        BaseOperationLogService baseOperationLogService = Mockito.mock(BaseOperationLogService.class);
        WebLogController controller = new WebLogController(baseOperationLogService);

        OptLogDTO dto = new OptLogDTO();
        dto.setRequestIp("127.0.0.1");

        R<Boolean> result = controller.save(dto);
        assertTrue(result.getIsSuccess());
        assertTrue(result.getData());
        verify(baseOperationLogService).save(any());
    }
}
