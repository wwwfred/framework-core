package net.wwwfred.framework.core.aop.log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.wwwfred.framework.core.common.CommonConstant;
import net.wwwfred.framework.core.web.HttpServletRequestWrapper;
import net.wwwfred.framework.core.web.ServletUtil;
import net.wwwfred.framework.util.code.CodeUtil;
import net.wwwfred.framework.util.date.DatetimeFormat;
import net.wwwfred.framework.util.date.DatetimeUtil;
import net.wwwfred.framework.util.json.JSONUtil;
import net.wwwfred.framework.util.log.LogUtil;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
//import com.teshehui.util.json.JSONUtil;

/**
 * spring AOP 日志代理
 * @author Administrator
 *
 */
//@Aspect 
//@Component
public class LogAop {
	
	/** 定义切面 */
//  @Pointcut("execution(* com.teshehui..*(..))")
//	@Pointcut("@annotation(com.teshehui.util.log.LogAnnotaion)") 
	@SuppressWarnings("unused")
	private void logPointCut(){};
	
//	@Around("logPointCut()")
	public Object logProxy(ProceedingJoinPoint pjp) throws Throwable
	{
		Object result = null;
	    Object[] requestPO = pjp.getArgs();
	    Object target = pjp.getTarget();
	    Class<?> targetClazz = target.getClass();
	    Method method = ((MethodSignature) pjp.getSignature()).getMethod();
	    Class<LogAnnotaion> annotationClass = LogAnnotaion.class;
	    if (targetClazz.isAnnotationPresent(annotationClass)||method.isAnnotationPresent(annotationClass))
	    {
	    	LogAnnotaion annotation = null;
	    	if(targetClazz.isAnnotationPresent(annotationClass))
	    	{
	    		annotation = targetClazz.getAnnotation(annotationClass);
	    	}
	    	
	    	if(method.isAnnotationPresent(annotationClass))
	    	{
	    		annotation = method.getAnnotation(annotationClass);
	    	}
	    	
	    	String tag=annotation.value();
	    	tag = (tag==null||"".equals(tag.trim()))?("at "+getLocation(targetClazz.getName())):tag;
	    	
	    	String message=annotation.message();
	    	message = (message==null||"".equals(message.trim()))?"":(","+message);
	    	
	    	if(!LogLevelEnum.OFF.equals(annotation.levelSuccess())&&!LogLevelEnum.OFF.equals(annotation.levelFailure()))
	    	{

		    	boolean requestPOChanged = false;
		    	
		    	String beforeRequstPOString;
			    if(requestPO!=null&&requestPO.length>0&&!CodeUtil.isEmpty(requestPO[0])&&requestPO[0] instanceof HttpServletRequest)
			    {
			    	HttpServletRequest httpRequest = new HttpServletRequestWrapper((HttpServletRequest) requestPO[0]);
			    	requestPO[0] = httpRequest;
			    	Map<String,List<Object>> requestMap = ServletUtil.getMapFromRequest(httpRequest);
			    	beforeRequstPOString = JSONUtil.toString(requestMap);
			    }
			    else
			    {
			    	beforeRequstPOString = JSONUtil.toString(requestPO);
			    }
		    	String afterRequestPOString = beforeRequstPOString;
		    	long startTime = System.currentTimeMillis();
		    	LogLevelEnum logLevel;
		    	try {
		    		LogUtil.d(tag, "startTime="+(DatetimeUtil.longToDateTimeString(startTime, DatetimeFormat.STANDARED_DATE_TIME_FORMAT))+message+",requestPO="+beforeRequstPOString);
		    		result = pjp.proceed(requestPO);
		    		logLevel = annotation.levelSuccess();
				    if(requestPO!=null&&requestPO.length>0&&requestPO.getClass().isArray()&&!CodeUtil.isEmpty(requestPO[0])&&requestPO[0] instanceof HttpServletRequest)
				    {
				    	HttpServletRequest httpRequest = new HttpServletRequestWrapper((HttpServletRequest) requestPO[0]);
				    	requestPO[0] = httpRequest;
				    	Map<String,List<Object>> requestMap = ServletUtil.getMapFromRequest(httpRequest);
				    	afterRequestPOString = JSONUtil.toString(requestMap);
				    }
				    else
				    {
				    	afterRequestPOString = JSONUtil.toString(requestPO);
				    }
		    		requestPOChanged = requestPO!=null&&!afterRequestPOString.equals(beforeRequstPOString);
		        }catch (Throwable e) {
		        	logLevel = annotation.levelFailure();
		        	if(LogLevelEnum.WARN.equals(logLevel))
		        	{
		        		LogUtil.w(tag, "useTime="+(System.currentTimeMillis()-startTime)+message+",requestPO="+(requestPOChanged?(beforeRequstPOString+"-->"+afterRequestPOString):(beforeRequstPOString))+",responsePO="+JSONUtil.toString(result), e);
		        	}
		        	else
		        	{
		        		LogUtil.e(tag, "useTime="+(System.currentTimeMillis()-startTime)+message+",requestPO="+(requestPOChanged?(beforeRequstPOString+"-->"+afterRequestPOString):(beforeRequstPOString))+",responsePO="+JSONUtil.toString(result), e);
		        	}
		        	throw e;
		        }
		    	if(LogLevelEnum.DEBUG.equals(logLevel))
	    		{
	    			LogUtil.d(tag, "useTime="+(System.currentTimeMillis()-startTime)+message+",requestPO="+(requestPOChanged?(beforeRequstPOString+"-->"+afterRequestPOString):(beforeRequstPOString))+",responsePO="+JSONUtil.toString(result));
	    		}
	    		else
	    		{
	    			LogUtil.i(tag, "useTime="+(System.currentTimeMillis()-startTime)+message+",requestPO="+(requestPOChanged?(beforeRequstPOString+"-->"+afterRequestPOString):(beforeRequstPOString))+",responsePO="+JSONUtil.toString(result));
	    		}
	    	}
	    	else
	    	{
	    		result = pjp.proceed(requestPO);
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
}
