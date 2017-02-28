package net.wwwfred.framework.core.aop.exception.manager;

import java.lang.reflect.Method;

import net.wwwfred.framework.core.exception.FrameworkRuntimeException;
import net.wwwfred.framework.spi.response.BaseResponse;
import net.wwwfred.framework.util.json.JSONUtil;
import net.wwwfred.framework.util.log.LogUtil;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import com.alibaba.fastjson.JSON;
//import com.teshehui.util.json.JSONUtil;

/**
 * spring AOP manager代理
 * @author Administrator
 *
 */
public class ManagerAop {
	
	/** 定义切面 */
	@SuppressWarnings("unused")
	private void managerPointCut(){};
	
	public Object managerProxy(ProceedingJoinPoint pjp) throws Throwable
	{
		Object result = null;
	    Object[] requestPO = pjp.getArgs();
	    Object target = pjp.getTarget();
	    Class<?> targetClazz = target.getClass();
	    Method method = ((MethodSignature) pjp.getSignature()).getMethod();
	    
	    String tag = targetClazz.getName()+"."+method.getName();
	    boolean requestPOChanged = false;
	    String beforeRequstPOString = JSON.toJSONString(requestPO);
	    String afterRequestPOString = beforeRequstPOString;
	    long startTime = System.currentTimeMillis();
	    try {
	    	result = pjp.proceed(requestPO);
	    	afterRequestPOString = JSON.toJSONString(requestPO);
	    	requestPOChanged = requestPO!=null&&!JSON.toJSONString(requestPO).equals(beforeRequstPOString);
	    }
	    catch (FrameworkRuntimeException e)
	    {
	    	LogUtil.w(tag, "useTime="+(System.currentTimeMillis()-startTime)+",requestPO="+(requestPOChanged?(beforeRequstPOString+"-->"+afterRequestPOString):(beforeRequstPOString))+",responsePO="+JSON.toJSONString(result), e);
	    	result = new BaseResponse<Object>(e.getCode(), e.getMessage());
	    }
	    catch (Throwable e) 
	    {
	    	LogUtil.e(tag, "useTime="+(System.currentTimeMillis()-startTime)+",requestPO="+(requestPOChanged?(beforeRequstPOString+"-->"+afterRequestPOString):(beforeRequstPOString))+",responsePO="+JSON.toJSONString(result), e);
	    	FrameworkRuntimeException te = new FrameworkRuntimeException(e);
	    	result = new BaseResponse<Object>(te.getCode(), te.getMessage());
	    }
		try {
			LogUtil.i(tag, "useTime="+(System.currentTimeMillis()-startTime)+",requestPO="+(requestPOChanged?(beforeRequstPOString+"-->"+afterRequestPOString):(beforeRequstPOString))+",responsePO="+JSON.toJSONString(result));
		} catch (Exception e) {
			LogUtil.w("JSON.toJSONString illegal,reuslt="+result, e);
			LogUtil.i(tag, "useTime="+(System.currentTimeMillis()-startTime)+",requestPO="+(requestPOChanged?(beforeRequstPOString+"-->"+afterRequestPOString):(beforeRequstPOString))+",responsePO="+JSONUtil.toString(result));
		}
		return result;
	}
	
}
