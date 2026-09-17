package com.dalio.cloud.system.service.system;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lionsoul.ip2region.service.Ip2Region;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import com.dalio.cloud.system.entity.system.DefLoginLog;
import com.dalio.cloud.system.entity.tenant.DefUser;
import com.dalio.cloud.system.manager.system.DefLoginLogManager;
import com.dalio.cloud.system.manager.tenant.DefUserManager;
import com.dalio.cloud.system.service.system.impl.DefLoginLogServiceImpl;
import com.dalio.cloud.system.vo.save.system.DefLoginLogSaveVO;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DefLoginLogServiceImplTest {

    @BeforeAll
    static void init() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, DefLoginLog.class);
    }

    @Test
    @DisplayName("测试 saveBefore 包含 UA 解析、IP 解析与不同用户查找分支")
    void testSaveBefore() throws Exception {
        Ip2Region ip2Region = mock(Ip2Region.class);
        DefUserManager userManager = mock(DefUserManager.class);
        DefLoginLogManager logManager = mock(DefLoginLogManager.class);

        DefLoginLogServiceImpl service = new DefLoginLogServiceImpl(ip2Region, userManager);
        ReflectionTestUtils.setField(service, "superManager", logManager);

        when(ip2Region.search("8.8.8.8")).thenReturn("美国");

        DefUser user = new DefUser();
        user.setId(100L);
        user.setUsername("alice");
        user.setNickName("Alice");

        // 1. 分支一：userId != null
        DefLoginLogSaveVO vo1 = new DefLoginLogSaveVO();
        vo1.setUserId(100L);
        vo1.setRequestIp("127.0.0.1"); // 本地回环
        vo1.setUa("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        when(userManager.getByIdCache(100L)).thenReturn(user);

        DefLoginLog log1 = ReflectionTestUtils.invokeMethod(service, "saveBefore", vo1);
        assertNotNull(log1);
        assertEquals("Chrome", log1.getBrowser());
        assertEquals("OSX", log1.getOperatingSystem());
        assertEquals("", log1.getLocation()); // 127.0.0.1 为空
        assertEquals("alice", log1.getUsername());

        // 2. 分支二：userId 为空但 mobile != null
        DefLoginLogSaveVO vo2 = new DefLoginLogSaveVO();
        vo2.setMobile("13800000000");
        vo2.setRequestIp("8.8.8.8");
        vo2.setUa("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:109.0) Gecko/20100101 Firefox/115.0");

        when(userManager.getUserByMobile("13800000000")).thenReturn(user);

        DefLoginLog log2 = ReflectionTestUtils.invokeMethod(service, "saveBefore", vo2);
        assertNotNull(log2);
        assertEquals("Firefox", log2.getBrowser());
        assertEquals("Windows 10", log2.getOperatingSystem());
        assertEquals("美国", log2.getLocation());

        // 3. 分支三：username 查找与非法 IP 异常保护
        DefLoginLogSaveVO vo3 = new DefLoginLogSaveVO();
        vo3.setUsername("alice");
        vo3.setRequestIp("invalid-ip-999.999.999.999");
        vo3.setUa("UnknownUA/1.0");

        when(userManager.getUserByUsername("alice")).thenReturn(user);
        when(ip2Region.search(anyString())).thenThrow(new RuntimeException("ip parse error"));

        DefLoginLog log3 = ReflectionTestUtils.invokeMethod(service, "saveBefore", vo3);
        assertNotNull(log3);
        assertNull(log3.getLocation());
    }

    @Test
    @DisplayName("测试 clearLog 日志清理")
    void testClearLog() {
        Ip2Region ip2Region = mock(Ip2Region.class);
        DefUserManager userManager = mock(DefUserManager.class);
        DefLoginLogManager logManager = mock(DefLoginLogManager.class);

        DefLoginLogServiceImpl service = new DefLoginLogServiceImpl(ip2Region, userManager);
        ReflectionTestUtils.setField(service, "superManager", logManager);

        when(logManager.clearLog(any(), any())).thenReturn(10L);
        assertTrue(service.clearLog(LocalDateTime.now(), 30));

        when(logManager.clearLog(any(), any())).thenReturn(0L);
        assertFalse(service.clearLog(LocalDateTime.now(), 30));
    }
}
