package com.gascharge.taemin.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Cache
public @interface CachePut {
    @AliasFor(annotation = Cache.class)
    String value();
    @AliasFor(annotation = Cache.class)
    String key();
}
