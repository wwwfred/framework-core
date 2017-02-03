package net.wwwfred.framework.core.cache;

public interface MemCache {
	
	/**
	 * 判断key是否存在
	 * @param key
	 * @return
	 */
	boolean keyExists(String key);

	/**
	 * 缓存对象，默认存储时间为一星期7天
	 * @param key
	 * @param value
	 * @return
	 */
	public abstract boolean cacheObject(String key, Object value);

	/**
	 * 缓存对象，用户自定义存储时间，当为0时表示不会自动失效（但是若缓存重启还是会丢失）
	 * @param key
	 * @param value
	 * @param expiredTime
	 * @return
	 */
	public abstract boolean cacheObject(String key, Object value,
			Long expiredTime);

	/**
	 * 根据指定key获取缓存对象
	 * @param key
	 * @return
	 */
	public abstract Object getObject(String key);

	/**
	 * 释放指定的key缓存对象
	 * @param key
	 * @return
	 */
	public abstract boolean releaseObject(String key);

}