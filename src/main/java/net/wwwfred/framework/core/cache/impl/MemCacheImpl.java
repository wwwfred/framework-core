package net.wwwfred.framework.core.cache.impl;

import java.util.Date;

import net.wwwfred.framework.core.cache.MemCache;
import net.wwwfred.framework.core.cache.RedisCache;
import net.wwwfred.framework.util.log.LogUtil;
import net.wwwfred.framework.util.string.StringUtil;

import com.danga.MemCached.MemCachedClient;

public class MemCacheImpl implements MemCache {
	//默认缓存7天失效。
	private final static Long DEFAULT_EXPIRED_TIME = 1000l * 60 * 60 * 24 * 7;
	//缓存客户端
	private MemCachedClient client;
	private String businessType;
	private String cachedVersion;
	
	private RedisCache redisCache;
	
	@Override
	public boolean keyExists(String key) {
		if(redisCache!=null)
		{
			return redisCache.hasKey(key);
		}
		else
		{
			long startTime = System.currentTimeMillis();
			boolean result = client.keyExists(wrapKey(key));
			LogUtil.i("useTime="+(System.currentTimeMillis()-startTime)+",keyExists,key="+wrapKey(key));
			return result;
		}
	}
	
	@Override
	public boolean cacheObject(String key,Object value){
		return this.cacheObject(key,value,DEFAULT_EXPIRED_TIME);
	}
	
	@Override
	public boolean cacheObject(String key,Object value, Long expiredTime){
		if(StringUtil.isEmpty(key)||value==null){
			return false;
		}
		
//		boolean result = client.set(wrapKey(key), value, new Date(System.currentTimeMillis() + expiredTime));
		if(redisCache!=null)
		{
			redisCache.setObject(key, value, expiredTime);
			return true;
		}
		else
		{
			long startTime = System.currentTimeMillis();
			boolean result = client.set(wrapKey(key), value, new Date(expiredTime));
			LogUtil.i("useTime="+(System.currentTimeMillis()-startTime)+",cacheObject,key="+wrapKey(key));
			return result;
		}
		//		return client.replace(wrapKey(key), value, new Date(System.currentTimeMillis() + expiredTime));
//		return this.client.add(this.wrapKey(key), value, new Date(System.currentTimeMillis() + expiredTime));
	}
	
	@Override
	public Object getObject(String key) {
		if(redisCache!=null)
			return redisCache.getObject(key);
		else
		{
			long startTime = System.currentTimeMillis();
			Object result = this.client.get(this.wrapKey(key));
			LogUtil.i("useTime="+(System.currentTimeMillis()-startTime)+",getObject,key="+wrapKey(key));
			return result;
		}
	}
	
	@Override
	public boolean releaseObject(String key){
		if(StringUtil.isEmpty(key)){
			return false;
		}
		if(redisCache!=null)
		{
			redisCache.deleteObject(key);
			return true;
		}
		else
		{
			long startTime = System.currentTimeMillis();
			boolean result = client.delete(this.wrapKey(key));
			LogUtil.i("useTime="+(System.currentTimeMillis()-startTime)+",releaseObject,key="+wrapKey(key));
			return result;
		}
	}
	
	private String wrapKey(String key){
//		return this.getBusinessType().concat(this.getCachedVersion()).concat(key);
	    return this.getCachedVersion().concat(key);
	}

	/**
	 * 
	 * 
	 * @param client
	 */
	public void setClient(MemCachedClient client) {
		this.client = client;
	}

	public String getBusinessType() {
		return businessType==null?"":businessType;
	}

	public void setBusinessType(String businessType) {
		this.businessType = businessType;
	}

	public String getCachedVersion() {
		return cachedVersion==null?"":cachedVersion;
	}

	public void setCachedVersion(String cachedVersion) {
		this.cachedVersion = cachedVersion;
	}

	public RedisCache getRedisCache() {
		return redisCache;
	}

	public void setRedisCache(RedisCache redisCache) {
		this.redisCache = redisCache;
	}

	public MemCachedClient getClient() {
		return client;
	}
	
	
}
