package net.wwwfred.framework.core.dao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface TableAnnotaion {
	String value() default "";
	String seqTableName() default "";
	String dbValue() default "";
}
