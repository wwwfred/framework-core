package net.wwwfred.framework.core.cache;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface RedisCache {

	/**
	 * 判断key是否存在
	 * @param key
	 * @return
	 */
	boolean hasKey(String key);
	
	/**
	 * 缓存对象
	 * @param key
	 * @param obj
	 */
	public abstract void setObject(String key, Object obj);
	
	/**
	 * 缓存对象
	 * @param key
	 * @param obj
	 * @param expiredTime 为空表示永不超时，其他就是超时时间点
	 */
	public abstract void setObject(String key, Object obj, Long expiredTime);

	/**
	 * 获取指定key的缓存对象
	 * @param key
	 * @return
	 */
	public abstract Object getObject(String key);

	/**
	 * 删除指定key的缓存对象
	 * @param key
	 */
	public abstract void deleteObject(String key);

	/**
	 * 模糊查询一系列的key，例如用*做模糊匹配条件
	 * @param pattern
	 * @return
	 */
	public abstract Set<String> keys(String pattern);

	/**
	 * 批量缓存一系列对象
	 * @param map
	 */
	public abstract void batchSet(Map<String, Object> map);

	/**
	 * 批量获取一系列key的存储对象
	 * @param key
	 * @return
	 */
	public abstract List<Object> batchGet(Collection<String> key);

	/**
	 * 批量删除一系列key的存储对象
	 * @param key
	 */
	public abstract void batchDelete(Collection<String> key);

}