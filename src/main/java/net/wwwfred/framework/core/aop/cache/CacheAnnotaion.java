package net.wwwfred.framework.core.aop.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
@Inherited
public @interface CacheAnnotaion {
	String value() default "";
	CacheKeyRuleEnum cacheKeyRule() default CacheKeyRuleEnum.KEYVALUE;
}
