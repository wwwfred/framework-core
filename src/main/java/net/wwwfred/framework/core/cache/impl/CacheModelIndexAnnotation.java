package net.wwwfred.framework.core.cache.impl;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 缓存对象的字段组成的索引注解
 * @author wangwwy
 * 2014年7月29日 下午8:41:56
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface CacheModelIndexAnnotation {
	String[] value();
}
