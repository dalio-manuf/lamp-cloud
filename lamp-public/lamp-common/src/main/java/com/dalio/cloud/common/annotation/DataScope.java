package com.dalio.cloud.common.annotation;

import java.lang.annotation.*;

/**
 * @author admin
 * @date 2022/1/9 22:45
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface DataScope {
    DataField[] value() default {};

    boolean ignore() default false;
}
