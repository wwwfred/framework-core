package net.wwwfred.framework.core.aop.exception.manager;

import java.lang.reflect.Method;

import net.wwwfred.framework.core.exception.FrameworkRuntimeException;
import net.wwwfred.framework.spi.response.BaseResponse;
import net.wwwfred.framework.util.json.JSONUtil;
import net.wwwfred.framework.util.log.LogUtil;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
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
	    String beforeRequstPOString = JSONUtil.toString(requestPO);
	    String afterRequestPOString = beforeRequstPOString;
	    long startTime = System.currentTimeMillis();
	    try {
	    	result = pjp.proceed(requestPO);
	    	afterRequestPOString = JSONUtil.toString(requestPO);
	    	requestPOChanged = requestPO!=null&&!afterRequestPOString.equals(beforeRequstPOString);
	    }
	    catch (FrameworkRuntimeException e)
	    {
	    	LogUtil.w(tag, "useTime="+(System.currentTimeMillis()-startTime)+",requestPO="+(requestPOChanged?(beforeRequstPOString+"-->"+afterRequestPOString):(beforeRequstPOString))+",responsePO="+JSONUtil.toString(result), e);
	    	result = new BaseResponse<Object>(e.getCode(), e.getMessage());
	    }
	    catch (Throwable e) 
	    {
	    	LogUtil.e(tag, "useTime="+(System.currentTimeMillis()-startTime)+",requestPO="+(requestPOChanged?(beforeRequstPOString+"-->"+afterRequestPOString):(beforeRequstPOString))+",responsePO="+JSONUtil.toString(result), e);
	    	FrameworkRuntimeException te = new FrameworkRuntimeException(e);
	    	result = new BaseResponse<Object>(te.getCode(), te.getMessage());
	    }
	    LogUtil.i(tag, "useTime="+(System.currentTimeMillis()-startTime)+",requestPO="+(requestPOChanged?(beforeRequstPOString+"-->"+afterRequestPOString):(beforeRequstPOString))+",responsePO="+JSONUtil.toString(result));
		return result;
	}
	
}
