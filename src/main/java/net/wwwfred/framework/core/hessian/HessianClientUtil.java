package net.wwwfred.framework.core.hessian;

import java.net.MalformedURLException;

import net.wwwfred.framework.util.properties.PropertiesUtil;

import com.caucho.hessian.HessianException;
import com.caucho.hessian.client.HessianProxyFactory;

public final class HessianClientUtil {
	
    private static final String PROPERTIES_CONFIG_SERVER_FILE_NAME = "config-server.properties";
    /** hessianProxyFactory read timeOut */
    private static long READ_TIME_OUT = Long.parseLong(PropertiesUtil.getValue(PROPERTIES_CONFIG_SERVER_FILE_NAME, "hessian_factory_read_time_out", "30000"));
    /** hessianProxyFactory connect timeOut */
    private static long CONNECT_TIME_OUT = Long.parseLong(PropertiesUtil.getValue(PROPERTIES_CONFIG_SERVER_FILE_NAME, "hessian_factory_connect_time_out", "10000"));

    private static HessianProxyFactory factory;
    
    public static void setREAD_TIME_OUT(long rEAD_TIME_OUT) {
        READ_TIME_OUT = rEAD_TIME_OUT;
    }
    public static long getREAD_TIME_OUT() {
        return READ_TIME_OUT;
    }
    public static void setCONNECT_TIME_OUT(long cONNECT_TIME_OUT) {
        CONNECT_TIME_OUT = cONNECT_TIME_OUT;
    }
    public static long getCONNECT_TIME_OUT() {
        return CONNECT_TIME_OUT;
    }
    public static void initHessianProxyFactory()
    {
        factory = new HessianProxyFactory();
        factory.setReadTimeout(READ_TIME_OUT);
        factory.setConnectTimeout(CONNECT_TIME_OUT);
    }
    
    /**
     * Getting hessian service
     * 
     * @param url
     * @param className
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getHessianService(String url,Class<T> className){
        
        T result;
        
        if(factory==null)
        {
            initHessianProxyFactory();
        }
        try {
            result = (T) factory.create(className, url);
        } 
        catch (MalformedURLException e) {
        	throw new HessianException("HessianProxyFactory create hessianService illegal,className="+className+",url="+url);
        }
        return result;
    }
}
