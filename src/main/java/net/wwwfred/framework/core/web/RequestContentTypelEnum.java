package net.wwwfred.framework.core.web;

import net.wwwfred.framework.util.code.CodeUtil;

public enum RequestContentTypelEnum {
	normal("application/x-www-form-urlencoded"),file("multipart/form-data"),json("application/json");
	private String flag;
	private RequestContentTypelEnum(String flag)
	{
		this.flag = flag;
	}
	public static RequestContentTypelEnum getInstance(String contentType)
	{
		if(!CodeUtil.isEmpty(contentType))
		{
			RequestContentTypelEnum[] array = RequestContentTypelEnum.values();
			for(RequestContentTypelEnum one : array)
			{
				if(contentType.contains(one.flag))
				{
					return one;
				}
			}
		}
		return normal;
	}
	public String getFlag() {
		return flag;
	}
	public void setFlag(String flag) {
		this.flag = flag;
	}
	
}
