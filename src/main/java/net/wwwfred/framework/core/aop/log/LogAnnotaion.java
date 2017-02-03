package net.wwwfred.framework.core.aop.log;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
@Inherited
public @interface LogAnnotaion {
	String value() default "";
	LogLevelEnum levelSuccess() default LogLevelEnum.INFO;
	LogLevelEnum levelFailure() default LogLevelEnum.ERROR;
	String message() default "";
	
}
