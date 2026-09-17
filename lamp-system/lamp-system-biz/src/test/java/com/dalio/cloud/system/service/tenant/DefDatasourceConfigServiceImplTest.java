package com.dalio.cloud.system.service.tenant;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import com.dalio.basic.exception.ArgumentException;
import com.dalio.basic.exception.BizException;
import com.dalio.cloud.system.entity.tenant.DefDatasourceConfig;
import com.dalio.cloud.system.manager.tenant.DefDatasourceConfigManager;
import com.dalio.cloud.system.service.tenant.impl.DefDatasourceConfigServiceImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DefDatasourceConfigServiceImplTest {

    @BeforeAll
    static void init() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, DefDatasourceConfig.class);
    }

    @Test
    @DisplayName("测试 testConnection 校验与连接失败捕获")
    void testTestConnection() {
        DefDatasourceConfigManager manager = mock(DefDatasourceConfigManager.class);
        DefDatasourceConfigServiceImpl service = new DefDatasourceConfigServiceImpl();
        ReflectionTestUtils.setField(service, "superManager", manager);
        ReflectionTestUtils.setField(service, "validationQuery", "SELECT 1");

        // 1. id 为空
        assertThrows(ArgumentException.class, () -> service.testConnection(null));

        // 2. config 为空
        when(manager.getById(1L)).thenReturn(null);
        assertThrows(ArgumentException.class, () -> service.testConnection(1L));

        // 3. 配置存在但连接不可用，捕获异常转为 BizException
        DefDatasourceConfig config = new DefDatasourceConfig();
        config.setId(1L);
        config.setName("invalid_ds");
        config.setUrl("jdbc:mysql://127.0.0.1:54321/invalid_db");
        config.setUsername("root");
        config.setPassword("password");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        when(manager.getById(1L)).thenReturn(config);

        assertThrows(BizException.class, () -> service.testConnection(1L));
    }
}
