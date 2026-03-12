package com.cardmanager.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /**
     * 模块
     */
    String module() default "";

    /**
     * 操作类型
     */
    String operation() default "";
}
