package net.wwwfred.framework.core.cache.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import net.wwwfred.framework.core.cache.RedisCache;
import net.wwwfred.framework.core.exception.FrameworkRuntimeException;
import net.wwwfred.framework.util.log.LogUtil;
import net.wwwfred.framework.util.serialize.SerializableHandler;
import net.wwwfred.framework.util.string.StringUtil;

import org.springframework.data.redis.core.RedisTemplate;

public class RedisCacheImpl implements RedisCache{
	
private RedisTemplate<Serializable, Serializable> redisTemplate;
    
private static final String tag = "!@#$";
	private String cachedVersion;

   public RedisTemplate<Serializable, Serializable> getRedisTemplate() {
       return redisTemplate;
   }

   public void setRedisTemplate(
           RedisTemplate<Serializable, Serializable> redisTemplate) {
       this.redisTemplate = redisTemplate;
   }
   
   @Override
   public boolean hasKey(String key) {
   	long startTime = System.currentTimeMillis();
   	try {
       	Boolean result = redisTemplate.hasKey(wrapKey(key));
           LogUtil.i("useTime="+(System.currentTimeMillis()-startTime)+",hasKey,key="+wrapKey(key));
           return result==null?false:result;
		} catch (Exception e) {
			String msg = "redisTemplate has illegal,key="+key;
           LogUtil.e(getClass().getName(), msg, e);
           throw new FrameworkRuntimeException(msg,e);
		}

   }
   
   @Override
	public void setObject(String key, Object obj) {
       setObject(key, obj,null);
   }
   
   @Override
   public void setObject(String key, Object obj, Long expiredTime) {
   	if(expiredTime==null)
   	{
   		try {
               String value = obj==null?tag:(obj.toString()+tag+SerializableHandler.objectToString(obj));
               long startTime = System.currentTimeMillis();
               redisTemplate.opsForValue().set(wrapKey(key), value);
               LogUtil.i("useTime="+(System.currentTimeMillis()-startTime)+",setObject,key="+wrapKey(key));
//             redisTemplate.opsForValue().set(key,
//                     SerializableHandler.objectToString(obj));
           } catch (Exception e) {
               String msg = "redisTemplate set illegal,key="+key+",value="+obj;
               LogUtil.e(getClass().getName(), msg, e);
               throw new FrameworkRuntimeException(msg,e);
           }
   	}
   	else
   	{
   		try {
               String value = obj==null?tag:(obj.toString()+tag+SerializableHandler.objectToString(obj));
               long startTime = System.currentTimeMillis();
               redisTemplate.opsForValue().set(wrapKey(key), value, expiredTime, TimeUnit.MILLISECONDS);
               LogUtil.i("useTime="+(System.currentTimeMillis()-startTime)+",setObject,key="+wrapKey(key)+",expiredTime="+expiredTime);
//             redisTemplate.opsForValue().set(key,
//                     SerializableHandler.objectToString(obj));
           } catch (Exception e) {
           	String msg = "redisTemplate set illegal,key="+key+",value="+obj;
               LogUtil.e(getClass().getName(), msg, e);
               throw new FrameworkRuntimeException(msg,e);
           }
   	}
   }

   @Override
	public Object getObject(String key) {
       try {
       	long startTime = System.currentTimeMillis();
           Serializable object = redisTemplate.opsForValue().get(wrapKey(key));
           LogUtil.i("useTime="+(System.currentTimeMillis()-startTime)+",getObject,key="+wrapKey(key));
           if (null != object) {
               String value = object.toString();
               int lastTagIndex = value.lastIndexOf(tag);
               if(lastTagIndex<0)
               {
                   return SerializableHandler.stringToObject(value);
               }
               else if(lastTagIndex==0)
               {
               	return null;
               }
               else
               {
                   return SerializableHandler.stringToObject(value.substring(lastTagIndex+tag.length()));
               }
//             return SerializableHandler.stringToObject(object.toString());
           }
       } catch (Exception e) {
           String msg = "redisTemplate get illegal,key="+key;
           LogUtil.e(getClass().getName(), msg, e);
           throw new FrameworkRuntimeException(msg,e);
       }
       return null;
   }
   
   @Override
	public void deleteObject(String key) {
       try {
       	long startTime = System.currentTimeMillis();
           redisTemplate.delete(wrapKey(key));
           LogUtil.i("useTime="+(System.currentTimeMillis()-startTime)+",deleteObject,key="+wrapKey(key));
       } catch (Exception e) {
           String msg = "redisTemplate delete illegal,key="+key;
           LogUtil.e(getClass().getName(), msg, e);
           throw new FrameworkRuntimeException(msg,e);
       }
   }
   
   @Override
	public Set<String> keys(String pattern)
   {
       Set<String> result = new LinkedHashSet<String>();
       try {
       	long startTime = System.currentTimeMillis();
           Set<Serializable> keySet = redisTemplate.keys(wrapKey(pattern));
           LogUtil.i("useTime="+(System.currentTimeMillis()-startTime)+",keys,pattern="+wrapKey(pattern));
           for (Serializable one : keySet) {
               result.add(StringUtil.toString(one));
           }
           return result;
       } catch (Exception e) {
           String msg = "redisTemplate keys illegal,pattern="+pattern;
           LogUtil.e(getClass().getName(), msg, e);
           throw new FrameworkRuntimeException(msg,e);
       }
   }
   
   @Override
	public void batchSet(Map<String, Object> map)
   {
       try {
           Map<String, String> stringMap = new LinkedHashMap<String, String>();
           Set<Entry<String, Object>> entrySet = map.entrySet();
           for (Entry<String, Object> entry : entrySet) {
               Object obj = entry.getValue();
               if(obj!=null)
               {
               	String value = obj==null?tag:(obj.toString()+tag+SerializableHandler.objectToString(obj));
               	stringMap.put(wrapKey(entry.getKey()), value);
               }
           }
           long startTime = System.currentTimeMillis();
           redisTemplate.opsForValue().multiSet(stringMap);
           LogUtil.i("useTime="+(System.currentTimeMillis()-startTime)+",batchSet,key="+stringMap.keySet());
//         redisTemplate.opsForValue().set(key,
//                 SerializableHandler.objectToString(obj));
       } catch (Exception e) {
           String msg = "redisTemplate batchSet illegal,map="+map;
           LogUtil.e(getClass().getName(), msg, e);
           throw new FrameworkRuntimeException(msg,e);
       }
   }
   
	@Override
	public List<Object> batchGet(Collection<String> key) {
		List<Object> list = new ArrayList<Object>();
		try {
			List<Serializable> keyList = new ArrayList<Serializable>();
			for (String oneKey : key) {
				keyList.add(wrapKey(oneKey));
			}
			long startTime = System.currentTimeMillis();
			List<Serializable> object = redisTemplate.opsForValue().multiGet(keyList);
			LogUtil.i("useTime=" + (System.currentTimeMillis() - startTime) + ",batchGet,key=" + keyList);
			if (null != object && object.size() != 0) {
				for (Serializable serializable : object) {
					if (serializable != null) {
						String value = serializable.toString();
						int lastTagIndex = value.lastIndexOf(tag);
						if (lastTagIndex < 0) {
							list.add(SerializableHandler.stringToObject(value));
						} else if (lastTagIndex == 0) {
							list.add(null);
						} else {
							list.add(SerializableHandler.stringToObject(value.substring(lastTagIndex + tag.length())));
						}						
					}
				}
			}
		} catch (Exception e) {
			String msg = "redisTemplate batchGet illegal,key=" + key;
			LogUtil.e(getClass().getName(), msg, e);
			throw new FrameworkRuntimeException(msg, e);
		}
		return list;
	}
   
   @Override
	public void batchDelete(Collection<String> key)
   {
       try {
           Set<Serializable> keySet = new HashSet<Serializable>();
//           keySet.addAll(key);
           for (String oneKey : key) {
				keySet.add(wrapKey(oneKey));
			}
           long startTime = System.currentTimeMillis(); 
           redisTemplate.delete(keySet);
           LogUtil.i("useTime="+(System.currentTimeMillis()-startTime)+",batchDelete,key="+keySet);
       } catch (Exception e) {
           String msg = "redisTemplate delete illegal,key="+key;
           LogUtil.e(getClass().getName(), msg, e);
           throw new FrameworkRuntimeException(msg,e);
       }
   }
	
	private String wrapKey(String key){
//		return this.getBusinessType().concat(this.getCachedVersion()).concat(key);
	    return (this.getCachedVersion()==null?"":getCachedVersion()).concat(key);
	}
   
	public String getCachedVersion() {
		return cachedVersion;
	}

	public void setCachedVersion(String cachedVersion) {
		this.cachedVersion = cachedVersion;
	}
	
}
