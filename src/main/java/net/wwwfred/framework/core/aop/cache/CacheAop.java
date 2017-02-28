package net.wwwfred.framework.core.aop.cache;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.wwwfred.framework.core.cache.MemCache;
import net.wwwfred.framework.core.cache.RedisCache;
import net.wwwfred.framework.core.common.CommonConstant;
import net.wwwfred.framework.core.exception.FrameworkRuntimeException;
import net.wwwfred.framework.util.code.CodeUtil;
import net.wwwfred.framework.util.json.JSONUtil;
import net.wwwfred.framework.util.log.LogUtil;
import net.wwwfred.framework.util.string.StringUtil;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import com.alibaba.fastjson.JSON;

/**
 * spring AOP 日志代理
 * @author Administrator
 *
 */
//@Aspect 
//@Component
public class CacheAop {
	
	/** 定义切面 */
//  @Pointcut("execution(* com.teshehui..*(..))")
//	@Pointcut("@annotation(com.teshehui.util.log.LogAnnotaion)") 
	@SuppressWarnings("unused")
	private void cachePointCut(){};
	
	private MemCache memCache;
	
	private RedisCache redisCache;
	
	private Long cacheValueValidTime = 30*24*60*60*1000L;
	
//	@Around("logPointCut()")
	public Object cacheProxy(ProceedingJoinPoint pjp) throws Throwable
	{
		Object result = null;
	    Object[] requestPO = pjp.getArgs();
	    Object target = pjp.getTarget();
	    Class<?> targetClazz = target.getClass();
	    Method method = ((MethodSignature) pjp.getSignature()).getMethod();
	    Class<CacheAnnotaion> annotationClass = CacheAnnotaion.class;
	    if (targetClazz.isAnnotationPresent(annotationClass)||method.isAnnotationPresent(annotationClass))
	    {
	    	CacheAnnotaion annotation = null;
	    	if(targetClazz.isAnnotationPresent(annotationClass))
	    	{
	    		annotation = targetClazz.getAnnotation(annotationClass);
	    	}
	    	
	    	if(method.isAnnotationPresent(annotationClass))
	    	{
	    		annotation = method.getAnnotation(annotationClass);
	    	}
	    	
	    	String tag = "at "+getLocation(targetClazz.getName());
	    	
	    	String cacheKeyPrefix = CodeUtil.isEmpty(annotation.value())?(targetClazz.getName()+"."+method.getName()):annotation.value();
	    	
	    	// 是否从缓存读取数据，根据第一个参数判断（null原生的不读缓存数据不覆写缓存，true不从缓存读数读原生据并且写覆写缓存，false只读缓存数据读取不到时读原生并写缓存）
	    	Boolean getDataNoCache;
	    	String cacheKey = cacheKeyPrefix;
	    	int length = requestPO.length;
	    	if(requestPO!=null&&length>0&&!CodeUtil.isEmpty(requestPO[0])&&(Boolean.class.isAssignableFrom(requestPO[0].getClass())||boolean.class.isAssignableFrom(requestPO[0].getClass())))
	    	{
	    		getDataNoCache = Boolean.parseBoolean(StringUtil.toString(requestPO[0]));
	    		Object[] requestPONoCache = new Object[length-1];
	    		for (int i = 0; i < length-1; i++) {
					requestPONoCache[i] = requestPO[i+1];
				}
	    		CacheKeyRuleEnum cacheKeyRule = annotation.cacheKeyRule();
	    		cacheKey += cacheKeyRule.getCacheKey(requestPONoCache);
	    	}
	    	else
	    	{
	    		getDataNoCache = null ;
	    	}
	    	
	    	// 原生的不读缓存数据不覆写缓存
	    	if(getDataNoCache==null)
	    	{
	    		result = pjp.proceed(requestPO);
	    	}
	    	else
	    	{
	    		// 读缓存数据
	    		boolean cacheKeyExist = false;
	    		Object cacheResult = null;
	    		if(!getDataNoCache)
		    	{
		    		long getDataFromCacheStartTime = System.currentTimeMillis();
			    	try
			    	{
			    		cacheKeyExist = memCache!=null?memCache.keyExists(cacheKey):redisCache.hasKey(cacheKey);
			    		if(cacheKeyExist)
			    		{
			    			cacheResult = memCache!=null?memCache.getObject(cacheKey):redisCache.getObject(cacheKey);
//			    			LogUtil.d(tag,"useTime="+(System.currentTimeMillis()-getDataFromCacheStartTime)+",getData from cache success,cacheKey="+cacheKey+",cacheValue="+JSON.toJSONString(result));
			    			LogUtil.d(tag,"useTime="+(System.currentTimeMillis()-getDataFromCacheStartTime)+",getData from cache success,cacheKey="+cacheKey+",cacheValue="+JSON.toJSONString(result));
			    		}
			    	}
			    	catch(Throwable e)
			    	{
			    		LogUtil.w(tag,"useTime="+(System.currentTimeMillis()-getDataFromCacheStartTime)+",getData from cache illegal,cacheKey="+cacheKey+",cacheValue="+JSON.toJSONString(result),e);
			    	}
		    	}
	    		
	    		// 读缓存数据,存在即读到直接返回
	    		if(!getDataNoCache&&cacheKeyExist)
	    		{
	    			result = cacheResult;
	    		}
	    		// 读原生数据并写缓存；缓存数据读取不到时也是要读原生数据并写缓存的
	    		else
	    		{
	    			long getDataStartTime = System.currentTimeMillis();
		    		try {
		    			result = pjp.proceed(requestPO);
		    		}catch(Throwable e)
		    		{
		    			LogUtil.e(tag,"useTime="+(System.currentTimeMillis()-getDataStartTime)+",getData not from cache illegal,cacheKey="+cacheKey+",noCacheValue="+JSON.toJSONString(result),e);
		    			throw e;
		    		}
		    		String resultString;
		    		try {
		    			resultString = JSON.toJSONString(result);
					} catch (Exception e) {
						LogUtil.w("JSON.toJSONString illegal,reuslt="+result, e);
						resultString = JSONUtil.toString(result);
					}
		    		LogUtil.i(tag,"useTime="+(System.currentTimeMillis()-getDataStartTime)+",getData not from cache success,cacheKey="+cacheKey+",noCacheValue="+resultString);
//		    		if(cacheResult==null||!cacheResult.equals(result))
//		    		if(!CodeUtil.isEmpty(cacheKey,result))
		    		if(getDataNoCache||!cacheKeyExist)
		    		{
		    			long cacheDataStartTime = System.currentTimeMillis();
			    		try
			    		{
			    			if(memCache!=null)
			    			{
			    				if(!memCache.cacheObject(cacheKey, result,cacheValueValidTime))
			    				{
			    					throw new FrameworkRuntimeException("memcache object result false.");
			    				}
			    			}
			    			else
			    				redisCache.setObject(cacheKey, result,cacheValueValidTime);
			    		}
			    		catch(Throwable e)
			    		{
			    			LogUtil.w(tag,"useTime="+(System.currentTimeMillis()-cacheDataStartTime)+",cacheData illegal,cacheKey="+cacheKey+",cacheValue="+resultString,e);
			    		}
			    		LogUtil.d(tag,"useTime="+(System.currentTimeMillis()-cacheDataStartTime)+",cacheData success,cacheKey="+cacheKey+",cacheValue="+resultString);
		    		}
	    		}
	    	}
	    	
		} else {
			result = pjp.proceed(requestPO);
		}

		return result;
	}
	
    /** 获取当前线程的调用的代码的方法函数及位置 */
    public static StackTraceElement getLocationStackTrace(String className)
    {
    	StackTraceElement[] stackArray = Thread.currentThread().getStackTrace();
        if(stackArray!=null)
        {
        	int len = stackArray.length;
        	boolean matched = false;
        	List<Integer> invokeClassNameList = new ArrayList<Integer>();
        	for (int i = len-1; i >= 0; i--) {
				String oneClassName = stackArray[i].getClassName();
				if(oneClassName.startsWith(CommonConstant.CLASS_NAME_PACKAGE_PREFIX))
				{
					invokeClassNameList.add(i);
				}
				if(oneClassName.equals(className)||(oneClassName.contains(className)&&oneClassName.charAt(oneClassName.indexOf(className)+className.length()+1)=='$'))
				{
					matched = true;
				}
				
				if(matched)
				{
					if(invokeClassNameList.size()>=2)
					{
						return stackArray[invokeClassNameList.get(invokeClassNameList.size()-2)];
					}
					else if(invokeClassNameList.size()>=1)
					{
						return stackArray[invokeClassNameList.get(invokeClassNameList.size()-1)];
					}
					break;
				}
        	}
        }
        return null;
    }
    
    /** 获取当前线程的调用的代码的方法函数及位置 */
    public static String getLocation(String className)
    {
        StackTraceElement stack = getLocationStackTrace(className);
        return stack==null?null:(stack.getClassName()+"."+stack.getMethodName()+"("+stack.getFileName()+":"+stack.getLineNumber()+")");
    }

	public MemCache getMemCache() {
		return memCache;
	}

	public void setMemCache(MemCache memCache) {
		this.memCache = memCache;
	}
    
    public RedisCache getRedisCache() {
		return redisCache;
	}

	public void setRedisCache(RedisCache redisCache) {
		this.redisCache = redisCache;
	}

	public static void main(String[] args) {
    	boolean b = true;
    	Boolean b2 = new Boolean(true);
    	Object b1 = b;
    	
    	System.out.println(b1.getClass());
    	System.out.println(b2.getClass());
		
    	System.out.println(Boolean.class.isAssignableFrom(b1.getClass()));
		System.out.println(b1.getClass().isAssignableFrom(Boolean.class));
		System.out.println(boolean.class.isAssignableFrom(b1.getClass()));
		System.out.println(b1.getClass().isAssignableFrom(boolean.class));
		
		System.out.println(Boolean.class.isAssignableFrom(b2.getClass()));
		System.out.println(b2.getClass().isAssignableFrom(Boolean.class));
		System.out.println(boolean.class.isAssignableFrom(b2.getClass()));
		System.out.println(b2.getClass().isAssignableFrom(boolean.class));
	
		
		System.out.println(Number.class.isAssignableFrom(new Integer(3).getClass()));
		System.out.println(new Integer(3).getClass().isAssignableFrom(Number.class));
		
	}
}
