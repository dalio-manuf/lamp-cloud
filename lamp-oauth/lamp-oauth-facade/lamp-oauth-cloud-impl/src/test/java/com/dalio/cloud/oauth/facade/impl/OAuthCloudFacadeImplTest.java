package com.dalio.cloud.oauth.facade.impl;

import com.dalio.basic.base.R;
import com.dalio.basic.model.log.OptLogDTO;
import com.dalio.cloud.oauth.api.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * lamp-oauth-cloud-impl 微服务门面实现类单元测试
 */
class OAuthCloudFacadeImplTest {

    @Test
    @DisplayName("测试 CaptchaFacadeImpl 校验验证码微服务调用")
    void testCaptchaFacadeImpl() {
        CaptchaApi captchaApi = Mockito.mock(CaptchaApi.class);
        CaptchaFacadeImpl facade = new CaptchaFacadeImpl(captchaApi);

        // 分支 1: check == null
        when(captchaApi.check("key1", "code1", "tpl")).thenReturn(null);
        assertFalse(facade.check("key1", "code1", "tpl"));

        // 分支 2: check.getIsSuccess() == false
        when(captchaApi.check("key2", "code2", "tpl")).thenReturn(R.fail("验证码失效"));
        assertFalse(facade.check("key2", "code2", "tpl"));

        // 分支 3: check.getData() == Boolean.FALSE
        when(captchaApi.check("key3", "code3", "tpl")).thenReturn(R.success(false));
        assertFalse(facade.check("key3", "code3", "tpl"));

        // 分支 4: 成功分支
        when(captchaApi.check("key4", "code4", "tpl")).thenReturn(R.success(true));
        assertTrue(facade.check("key4", "code4", "tpl"));
    }

    @Test
    @DisplayName("测试 DictFacadeImpl 字典数据微服务回显")
    void testDictFacadeImpl() {
        DictApi dictApi = Mockito.mock(DictApi.class);
        DictFacadeImpl facade = new DictFacadeImpl(dictApi);

        Set<Serializable> ids = Collections.singleton("DICT_1");
        Map<Serializable, Object> map = new HashMap<>();
        map.put("DICT_1", "字典项名称");
        when(dictApi.findByIds(ids)).thenReturn(map);

        Map<Serializable, Object> result = facade.findByIds(ids);
        assertNotNull(result);
        assertEquals("字典项名称", result.get("DICT_1"));
        verify(dictApi).findByIds(ids);
    }

    @Test
    @DisplayName("测试 LogFacadeImpl 保存日志微服务调用")
    void testLogFacadeImpl() {
        LogApi logApi = Mockito.mock(LogApi.class);
        LogFacadeImpl facade = new LogFacadeImpl(logApi);

        OptLogDTO dto = new OptLogDTO();
        dto.setRequestIp("127.0.0.1");

        facade.save(dto);
        verify(logApi).save(dto);
    }

    @Test
    @DisplayName("测试 OrgFacadeImpl 机构数据微服务回显")
    void testOrgFacadeImpl() {
        OrgApi orgApi = Mockito.mock(OrgApi.class);
        OrgFacadeImpl facade = new OrgFacadeImpl(orgApi);

        Set<Serializable> ids = Collections.singleton(100L);
        Map<Serializable, Object> map = new HashMap<>();
        map.put(100L, "总部");
        when(orgApi.findByIds(ids)).thenReturn(map);

        Map<Serializable, Object> result = facade.findByIds(ids);
        assertEquals(map, result);
        verify(orgApi).findByIds(ids);
    }

    @Test
    @DisplayName("测试 PositionFacadeImpl 岗位数据微服务回显")
    void testPositionFacadeImpl() {
        PositionApi positionApi = Mockito.mock(PositionApi.class);
        PositionFacadeImpl facade = new PositionFacadeImpl(positionApi);

        Set<Serializable> ids = Collections.singleton(200L);
        Map<Serializable, Object> map = new HashMap<>();
        map.put(200L, "技术专家");
        when(positionApi.findByIds(ids)).thenReturn(map);

        Map<Serializable, Object> result = facade.findByIds(ids);
        assertEquals(map, result);
        verify(positionApi).findByIds(ids);
    }
}
