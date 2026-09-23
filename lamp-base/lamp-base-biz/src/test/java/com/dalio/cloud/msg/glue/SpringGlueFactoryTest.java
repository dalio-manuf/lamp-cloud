package com.dalio.cloud.msg.glue;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import com.dalio.basic.utils.SpringUtils;
import com.dalio.cloud.msg.glue.impl.SpringGlueFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * SpringGlueFactory 单元测试
 */
class SpringGlueFactoryTest {

    @Test
    @DisplayName("测试 injectService 空对象与无 ApplicationContext 场景")
    void testInjectServiceNullAndNoContext() {
        SpringGlueFactory factory = new SpringGlueFactory();
        assertDoesNotThrow(() -> factory.injectService(null));

        try (MockedStatic<SpringUtils> springMock = Mockito.mockStatic(SpringUtils.class)) {
            springMock.when(SpringUtils::getApplicationContext).thenReturn(null);
            TargetBean target = new TargetBean();
            factory.injectService(target);
            assertNull(target.getServiceNamed());
        }
    }

    @Test
    @DisplayName("测试 injectService 注入各类 @Resource 和 @Autowired 字段")
    void testInjectServiceWithAnnotations() {
        SpringGlueFactory factory = new SpringGlueFactory();

        try (MockedStatic<SpringUtils> springMock = Mockito.mockStatic(SpringUtils.class)) {
            ApplicationContext mockContext = mock(ApplicationContext.class);
            springMock.when(SpringUtils::getApplicationContext).thenReturn(mockContext);

            TestService namedS = new TestService("named");
            TestService fieldS = new TestService("field");
            TestService typeS = new TestService("type");
            TestService qualS = new TestService("qualified");

            springMock.when(() -> SpringUtils.getBean("namedService")).thenReturn(namedS);
            springMock.when(() -> SpringUtils.getBean("serviceByField")).thenReturn(fieldS);
            springMock.when(() -> SpringUtils.getBean("serviceByType")).thenThrow(new RuntimeException("not found"));
            springMock.when(() -> SpringUtils.getBean("qualifiedService")).thenReturn(qualS);
            springMock.when(() -> SpringUtils.getBean(TestService.class)).thenReturn(typeS);

            TargetBean target = new TargetBean();
            factory.injectService(target);

            assertEquals(namedS, target.getServiceNamed());
            assertEquals(fieldS, target.getServiceByField());
            assertEquals(typeS, target.getServiceByType());
            assertEquals(qualS, target.getServiceQualified());
            assertEquals(typeS, target.getServiceAutowiredByType());
            assertNull(target.getPlainField());
        }
    }

    public static class TestService {
        private String name;

        public TestService(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static class TargetBean {
        public static String staticField = "static";

        @Resource(name = "namedService")
        private TestService serviceNamed;

        @Resource
        private TestService serviceByField;

        @Resource
        private TestService serviceByType;

        @Autowired
        @Qualifier("qualifiedService")
        private TestService serviceQualified;

        @Autowired
        private TestService serviceAutowiredByType;

        private String plainField;

        public TestService getServiceNamed() {
            return serviceNamed;
        }

        public TestService getServiceByField() {
            return serviceByField;
        }

        public TestService getServiceByType() {
            return serviceByType;
        }

        public TestService getServiceQualified() {
            return serviceQualified;
        }

        public TestService getServiceAutowiredByType() {
            return serviceAutowiredByType;
        }

        public String getPlainField() {
            return plainField;
        }
    }
}
