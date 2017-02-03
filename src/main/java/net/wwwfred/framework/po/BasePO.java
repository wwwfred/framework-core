package net.wwwfred.framework.po;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
*<p>Title: BasePO.java </p>
*@Description: 持久层对象
*@Author:kaili
*@version:1.0
*@DATE:2013-8-21上午11:09:35
*@see
*/
public abstract class BasePO implements Serializable{

	private static final long serialVersionUID = 4639209207059429003L;

	private static String OBJECT_START_TAG = "[";
	private static String OBJECT_END_TAG = "]";
	private static String FIELD_NAME_VALUE_SEPARATOR_TAG = "=";
	private static String FIELD_SEPARATOR_TAG = ",";
	
	@Override
	public String toString() {
//		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		return toString(this);
	}
	
	public static String toString(Object o)
	{
		StringBuilder sb = new StringBuilder();
		
		List<Field> fieldList = new ArrayList<Field>();
	
		Class<?> clazz = o.getClass();
		while(!Object.class.equals(clazz))
		{
			Field[] fieldArray = clazz.getDeclaredFields();
			if(fieldArray!=null&&fieldArray.length>0)
			{
				for (Field field : fieldArray) {
					if(!Modifier.isStatic(field.getModifiers()))
					{
						fieldList.add(field);
					}
				}
//				fieldList.addAll(Arrays.asList(fieldArray));
			}
			clazz = clazz.getSuperclass();
		}
		
		int fieldLength = fieldList.size();
		if(fieldLength>0)
		{
			sb.append(o.getClass().getName()).append(OBJECT_START_TAG);
			Field field = fieldList.get(0);
			Object fieldValue = null;
			try {
				field.setAccessible(true);
				fieldValue = field.get(o);
				field.setAccessible(false);
				sb.append(field.getName()).append(FIELD_NAME_VALUE_SEPARATOR_TAG).append(fieldValue==null?"<null>":fieldValue);
				for (int i = 1; i < fieldLength; i++) {
					field = fieldList.get(i);
					field.setAccessible(true);
					fieldValue = field.get(o);
					field.setAccessible(false);
					sb.append(FIELD_SEPARATOR_TAG).append(field.getName()).append(FIELD_NAME_VALUE_SEPARATOR_TAG).append(fieldValue==null?"<null>":fieldValue);
				}
			} catch (Exception e) {
				// TODO: handle exception
				// do nothing
			}
			sb.append(OBJECT_END_TAG);
		}
		else
		{
			sb.append(o.toString());
		}
		
		return sb.toString();
	}
}
