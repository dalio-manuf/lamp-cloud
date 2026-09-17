package com.dalio.cloud.system.facade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DefResourceFacade 与 DefUserFacade 接口定义规范测试
 */
class DefFacadeInterfaceTest {

    @Test
    @DisplayName("测试 Facade 接口契约定义完整性")
    void testFacadeInterfaceMethods() throws NoSuchMethodException {
        assertTrue(DefResourceFacade.class.isInterface());
        Method listAllApi = DefResourceFacade.class.getMethod("listAllApi");
        assertNotNull(listAllApi);

        assertTrue(DefUserFacade.class.isInterface());
        Method findAllUserId = DefUserFacade.class.getMethod("findAllUserId");
        assertNotNull(findAllUserId);

        Method findByIds = DefUserFacade.class.getMethod("findByIds", Set.class);
        assertNotNull(findByIds);
    }
}
