package com.dalio.cloud.oauth.facade;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.dalio.basic.interfaces.echo.LoadService;
import com.dalio.basic.model.log.OptLogDTO;

import java.lang.reflect.Method;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 门面接口契约测试
 */
class OAuthFacadeInterfaceTest {

    @Test
    @DisplayName("测试 Facade 接口契约定义与方法继承规范")
    void testFacadeInterfaces() throws NoSuchMethodException {
        // CaptchaFacade
        assertTrue(CaptchaFacade.class.isInterface());
        Method checkMethod = CaptchaFacade.class.getMethod("check", String.class, String.class, String.class);
        assertNotNull(checkMethod);

        // DictFacade 继承 LoadService
        assertTrue(DictFacade.class.isInterface());
        assertTrue(LoadService.class.isAssignableFrom(DictFacade.class));
        Method dictFindByIds = DictFacade.class.getMethod("findByIds", Set.class);
        assertNotNull(dictFindByIds);

        // LogFacade
        assertTrue(LogFacade.class.isInterface());
        Method logSave = LogFacade.class.getMethod("save", OptLogDTO.class);
        assertNotNull(logSave);

        // OrgFacade 继承 LoadService
        assertTrue(OrgFacade.class.isInterface());
        assertTrue(LoadService.class.isAssignableFrom(OrgFacade.class));
        Method orgFindByIds = OrgFacade.class.getMethod("findByIds", Set.class);
        assertNotNull(orgFindByIds);

        // PositionFacade 继承 LoadService
        assertTrue(PositionFacade.class.isInterface());
        assertTrue(LoadService.class.isAssignableFrom(PositionFacade.class));
        Method positionFindByIds = PositionFacade.class.getMethod("findByIds", Set.class);
        assertNotNull(positionFindByIds);
    }
}
