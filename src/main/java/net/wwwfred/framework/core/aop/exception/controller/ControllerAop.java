package net.wwwfred.framework.core.aop.exception.controller;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;

import net.wwwfred.framework.core.exception.TeshehuiRuntimeException;
import net.wwwfred.framework.core.web.HttpServletRequestWrapper;
import net.wwwfred.framework.core.web.ServletUtil;
import net.wwwfred.framework.spi.response.BaseResponse;
import net.wwwfred.framework.util.code.CodeUtil;
import net.wwwfred.framework.util.json.JSONUtil;
import net.wwwfred.framework.util.log.LogUtil;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import com.alibaba.fastjson.JSON;

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
		if (requestPO != null && requestPO.length > 0
				&& !CodeUtil.isEmpty(requestPO[0])
				&& requestPO[0] instanceof HttpServletRequest) {
			HttpServletRequest httpRequest = new HttpServletRequestWrapper(
					(HttpServletRequest) requestPO[0]);
			requestPO[0] = httpRequest;
			beforeRequstPOString = JSON.toJSONString(ServletUtil
					.getMapFromRequest(httpRequest));
		}
	    else
	    {
	    	beforeRequstPOString = JSON.toJSONString(requestPO);
	    }
    	String afterRequestPOString = beforeRequstPOString;
	    
	    long startTime = System.currentTimeMillis();
	    try {
	    	result = pjp.proceed(requestPO);
			if (requestPO != null && requestPO.length > 0
					&& !CodeUtil.isEmpty(requestPO[0])
					&& requestPO[0] instanceof HttpServletRequest) {
				HttpServletRequest httpRequest = new HttpServletRequestWrapper(
						(HttpServletRequest) requestPO[0]);
				requestPO[0] = httpRequest;
				afterRequestPOString = JSON.toJSONString(ServletUtil
						.getMapFromRequest(httpRequest));
			} else {
				afterRequestPOString = JSON.toJSONString(requestPO);
			}
			requestPOChanged = requestPO != null
					&& !afterRequestPOString.equals(beforeRequstPOString);
			try {
				LogUtil.i(tag, "useTime="+(System.currentTimeMillis()-startTime)+",requestPO="+(requestPOChanged?(beforeRequstPOString+"-->"+afterRequestPOString):(beforeRequstPOString))+",responsePO="+JSON.toJSONString(result));
			} catch (Exception e) {
				LogUtil.w("JSON.toJSONString illegal,reuslt="+result, e);
				LogUtil.i(tag, "useTime="+(System.currentTimeMillis()-startTime)+",requestPO="+(requestPOChanged?(beforeRequstPOString+"-->"+afterRequestPOString):(beforeRequstPOString))+",responsePO="+JSONUtil.toString(result));
			}
		}
	    catch (Throwable e) 
	    {
	    	TeshehuiRuntimeException te =  (e instanceof TeshehuiRuntimeException)?(TeshehuiRuntimeException)e:new TeshehuiRuntimeException(e);
	    	result = JSON.toJSONString(new BaseResponse<Object>(te.getCode(), te.getMessage()));
	    	LogUtil.e(tag, "useTime="+(System.currentTimeMillis()-startTime)+",requestPO="+(requestPOChanged?(beforeRequstPOString+"-->"+afterRequestPOString):(beforeRequstPOString))+",responsePO="+JSON.toJSONString(result), e);
	    }
	    
		return result;
	}
	
}
