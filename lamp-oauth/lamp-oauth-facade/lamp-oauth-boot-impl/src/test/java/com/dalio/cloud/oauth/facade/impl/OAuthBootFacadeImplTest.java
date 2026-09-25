package com.dalio.cloud.oauth.facade.impl;

import com.dalio.basic.base.R;
import com.dalio.basic.model.log.OptLogDTO;
import com.dalio.cloud.base.service.system.BaseOperationLogService;
import com.dalio.cloud.base.service.user.BaseOrgService;
import com.dalio.cloud.base.service.user.BasePositionService;
import com.dalio.cloud.oauth.service.CaptchaService;
import com.dalio.cloud.oauth.service.DictService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * lamp-oauth-boot-impl 门面实现类单元测试
 */
class OAuthBootFacadeImplTest {

    @Test
    @DisplayName("测试 CaptchaFacadeImpl 验证码校验全分支")
    void testCaptchaFacadeImpl() {
        CaptchaService captchaService = Mockito.mock(CaptchaService.class);
        CaptchaFacadeImpl facade = new CaptchaFacadeImpl(captchaService);

        // 分支 1: result == null
        when(captchaService.checkCaptcha("key1", "tpl", "code1")).thenReturn(null);
        assertFalse(facade.check("key1", "code1", "tpl"));

        // 分支 2: result.getIsSuccess() == false
        when(captchaService.checkCaptcha("key2", "tpl", "code2")).thenReturn(R.fail("验证码错误"));
        assertFalse(facade.check("key2", "code2", "tpl"));

        // 分支 3: result.getData() == Boolean.FALSE
        when(captchaService.checkCaptcha("key3", "tpl", "code3")).thenReturn(R.success(false));
        assertFalse(facade.check("key3", "code3", "tpl"));

        // 分支 4: 校验成功 result.getData() == Boolean.TRUE
        when(captchaService.checkCaptcha("key4", "tpl", "code4")).thenReturn(R.success(true));
        assertTrue(facade.check("key4", "code4", "tpl"));
    }

    @Test
    @DisplayName("测试 DictFacadeImpl 字典回显查询")
    void testDictFacadeImpl() {
        DictService dictService = Mockito.mock(DictService.class);
        DictFacadeImpl facade = new DictFacadeImpl(dictService);

        Set<Serializable> ids = Collections.singleton("DICT_KEY");
        Map<Serializable, Object> map = new HashMap<>();
        map.put("DICT_KEY", "字典值");
        when(dictService.findByIds(ids)).thenReturn(map);

        Map<Serializable, Object> result = facade.findByIds(ids);
        assertNotNull(result);
        assertEquals("字典值", result.get("DICT_KEY"));
        verify(dictService).findByIds(ids);
    }

    @Test
    @DisplayName("测试 LogFacadeImpl 保存操作日志")
    void testLogFacadeImpl() {
        BaseOperationLogService baseOperationLogService = Mockito.mock(BaseOperationLogService.class);
        LogFacadeImpl facade = new LogFacadeImpl(baseOperationLogService);

        OptLogDTO dto = new OptLogDTO();
        dto.setRequestIp("127.0.0.1");

        facade.save(dto);
        verify(baseOperationLogService).save(any());
    }

    @Test
    @DisplayName("测试 OrgFacadeImpl 机构数据回显")
    void testOrgFacadeImpl() {
        BaseOrgService baseOrgService = Mockito.mock(BaseOrgService.class);
        OrgFacadeImpl facade = new OrgFacadeImpl(baseOrgService);

        Set<Serializable> ids = Collections.singleton(1L);
        Map<Serializable, Object> map = new HashMap<>();
        map.put(1L, "研发部");
        when(baseOrgService.findByIds(ids)).thenReturn(map);

        Map<Serializable, Object> result = facade.findByIds(ids);
        assertEquals(map, result);
        verify(baseOrgService).findByIds(ids);
    }

    @Test
    @DisplayName("测试 PositionFacadeImpl 岗位数据回显")
    void testPositionFacadeImpl() {
        BasePositionService basePositionService = Mockito.mock(BasePositionService.class);
        PositionFacadeImpl facade = new PositionFacadeImpl(basePositionService);

        Set<Serializable> ids = Collections.singleton(1L);
        Map<Serializable, Object> map = new HashMap<>();
        map.put(1L, "工程师");
        when(basePositionService.findByIds(ids)).thenReturn(map);

        Map<Serializable, Object> result = facade.findByIds(ids);
        assertEquals(map, result);
        verify(basePositionService).findByIds(ids);
    }
}
