package net.wwwfred.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.wwwfred.framework.enumeration.FieldTypeEnum;

/**
 * 别名
 * @author wangwwy
 * 2014年7月29日 下午8:41:56
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface FieldTypeAnnotation {
	FieldTypeEnum value() default FieldTypeEnum.simple;
} 
