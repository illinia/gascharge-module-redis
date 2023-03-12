package com.gascharge.taemin.redis.annotation;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Cache {
    String value() default "";
    String key() default "";
}
