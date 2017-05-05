package net.wwwfred.framework.core.aop.exception.controller;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;

import net.wwwfred.framework.core.exception.FrameworkRuntimeException;
import net.wwwfred.framework.core.web.HttpServletRequestWrapper;
import net.wwwfred.framework.core.web.ServletUtil;
import net.wwwfred.framework.spi.response.BaseResponse;
import net.wwwfred.framework.util.code.CodeUtil;
import net.wwwfred.framework.util.json.JSONUtil;
import net.wwwfred.framework.util.log.LogUtil;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * spring AOP controller代理
 * @author Administrator
 *
 */
public class ControllerAop {
	
	/** 定义切面 */
	@SuppressWarnings("unused")
	private void controllerPointCut(){};
	
	public Object controllerProxy(ProceedingJoinPoint pjp) throws Throwable
	{
		Object result = null;
	    Object[] requestPO = pjp.getArgs();
	    Object target = pjp.getTarget();
	    Class<?> targetClazz = target.getClass();
	    Method method = ((MethodSignature) pjp.getSignature()).getMethod();
	    
	    String tag = targetClazz.getName()+"."+method.getName();
	    
	    boolean requestPOChanged = false;
    	String beforeRequstPOString;
//	    String requstPOString;
    	if(requestPO!=null&&requestPO.length>0&&requestPO.getClass().isArray()&&!CodeUtil.isEmpty(requestPO[0])&&requestPO[0] instanceof HttpServletRequest)
	    {
	    	HttpServletRequest httpRequest = new HttpServletRequestWrapper((HttpServletRequest) requestPO[0]);
	    	requestPO[0] = httpRequest;
	    	beforeRequstPOString = JSONUtil.toString(ServletUtil.getMapFromRequest(httpRequest));
	    }
	    else
	    {
	    	beforeRequstPOString = JSONUtil.toString(requestPO);
	    }
	    String afterRequestPOString = beforeRequstPOString;
	    String responsePOString = null;
	    long startTime = System.currentTimeMillis();
	    try {
	    	result = pjp.proceed(requestPO);
	    	responsePOString = JSONUtil.toString(result);
	    	if(requestPO!=null&&requestPO.length>0&&requestPO.getClass().isArray()&&!CodeUtil.isEmpty(requestPO[0])&&requestPO[0] instanceof HttpServletRequest)
		    {
		    	HttpServletRequest httpRequest = new HttpServletRequestWrapper((HttpServletRequest) requestPO[0]);
		    	requestPO[0] = httpRequest;
		    	afterRequestPOString = JSONUtil.toString(ServletUtil.getMapFromRequest(httpRequest));
		    }
		    else
		    {
		    	afterRequestPOString = JSONUtil.toString(requestPO);
		    }
    		requestPOChanged = afterRequestPOString!=null&&!afterRequestPOString.equals(beforeRequstPOString);
	    }
	    catch (FrameworkRuntimeException e)
	    {
	    	LogUtil.w(tag, "useTime="+(System.currentTimeMillis()-startTime)+",requestPO="+beforeRequstPOString+(requestPOChanged?(",changedRequestPO="+afterRequestPOString):"")+",responsePO="+responsePOString, e);
	    	result = JSONUtil.toString(new BaseResponse<Object>(e.getCode(), e.getMessage()));
	    }
	    catch (Throwable e) 
	    {
	    	LogUtil.e(tag, "useTime="+(System.currentTimeMillis()-startTime)+",requestPO="+beforeRequstPOString+(requestPOChanged?(",changedRequestPO="+afterRequestPOString):"")+",responsePO="+responsePOString, e);
	    	FrameworkRuntimeException te = new FrameworkRuntimeException(e);
	    	result = JSONUtil.toString(new BaseResponse<Object>(te.getCode(), te.getMessage()));
	    }
	    LogUtil.i(tag, "useTime="+(System.currentTimeMillis()-startTime)+",requestPO="+beforeRequstPOString+(requestPOChanged?(",changedRequestPO="+afterRequestPOString):"")+",responsePO="+responsePOString);

		return result;
	}
	
}
