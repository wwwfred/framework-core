package net.wwwfred.framework.core.aop.cache;

import java.util.ArrayList;
import java.util.List;

import net.wwwfred.framework.core.web.ServletUtil;
import net.wwwfred.framework.util.code.CodeUtil;
import net.wwwfred.framework.util.json.JSONUtil;
import net.wwwfred.framework.util.string.StringUtil;

public enum CacheKeyRuleEnum {
	STRING, KEYVALUE, JSON;

	@SuppressWarnings("unchecked")
	public String getCacheKey(Object po)
	{
		String cacheKey;
		switch (this) {
		case KEYVALUE:
		{
			if(po instanceof Object[] || po instanceof List<?>)
			{
				List<Object> list;
				if(po instanceof Object[])
				{
					list = new ArrayList<Object>();
					for (Object one : (Object[])po) {
						list.add(one);
					}
				}
				else
				{
					list = (List<Object>) po;
				}
				cacheKey = StringUtil.listToString(list,"`!@#$");
			}
			else
			{
				cacheKey = ServletUtil.modelToPostParam(po);
			}
		}
		break;
		case JSON:
		{
			cacheKey = JSONUtil.toString(po);
		}
		break;
		default:
		{
			cacheKey = po.toString();
		}
		break;
		}
		cacheKey = CodeUtil.isEmpty(cacheKey)?po.toString():cacheKey;
		return cacheKey;
	}

}
