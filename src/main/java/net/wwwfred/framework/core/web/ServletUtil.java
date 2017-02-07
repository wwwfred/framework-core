package net.wwwfred.framework.core.web;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.wwwfred.framework.core.exception.TeshehuiRuntimeException;
import net.wwwfred.framework.util.code.CodeUtil;
import net.wwwfred.framework.util.io.IOUtil;
import net.wwwfred.framework.util.json.JSONUtil;
import net.wwwfred.framework.util.log.LogUtil;
import net.wwwfred.framework.util.properties.PropertiesUtil;
import net.wwwfred.framework.util.reflect.ReflectUtil;
import net.wwwfred.framework.util.string.StringUtil;
import net.wwwfred.framework.util.xml.XmlUtil;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class ServletUtil
{
    public static String PROPERTIES_CONFIG_FILE_NAME = "config.properties";

	private static String REQUEST_DEFAULT_ENCODING = PropertiesUtil.getValue(PROPERTIES_CONFIG_FILE_NAME, "request_default_encoding", "UTF-8");
	private static String FILE_UPLOAD_TEMP_SAVE_DIRECTORY = PropertiesUtil.getValue(PROPERTIES_CONFIG_FILE_NAME, "file_upload_temp_save_directory", "/tempUploadDirectory");
	private static String FILE_UPLOAD_FILE_PATH_SEPARATOR = PropertiesUtil.getValue(PROPERTIES_CONFIG_FILE_NAME, "file_upload_file_path_separator", "\\");
	
	/* 暂时缓存HttpServletRequest */
	private static HttpServletRequest request;
	
	/* 缓存HTTP 数据于Map中 */
	private static Map<String,List<Object>> requestDataListMap;

	/* 比较调用的HttpServletRequest是否发生变化，并当request发生变化时实时更新系统中缓存的request */
	protected static boolean isNewReqeust(HttpServletRequest request)
	{
		boolean result;
		if (ServletUtil.request == null)
		{
			result = true;
		} else
		{
			result = !ServletUtil.request.equals(request);
		}

		if (result)
		{
			ServletUtil.request = request;
		}
		return result;
	}
	
	/**
	 * 从HttpServletRequest中获取指定类型的JAVABean对象
	 * @author wangwwy
	 * createdDatetime 2014年9月5日 下午5:11:34
	 * @param request
	 * @param clazz
	 * @return
	 */
	public static <T> T getModelFromRequest(HttpServletRequest request, Class<T> clazz)
	{
		if(CodeUtil.isEmpty(request,clazz))
			throw new WebException("param empty,at " + CodeUtil.getLocation());
		
		T result = null;
		result = ReflectUtil.newModel(clazz);
		
		String requestEncoding = request.getCharacterEncoding();
		requestEncoding = CodeUtil.isEmpty(requestEncoding)?REQUEST_DEFAULT_ENCODING:requestEncoding;
		
		RequestContentTypelEnum type = RequestContentTypelEnum.getInstance(request.getContentType());
		switch (type) {
		case normal:
		{
			Map<String, List<String>> parameterMap = getHttpRequestParameterMap(request,requestEncoding);
			Map<String, List<Object>> dataMap = parameterMapToDataMap(parameterMap);
		    initModelFromRequestDataMap(dataMap, result);
		}
		break;
		case file:
		{
			Map<String, List<Object>> dataMap = getListMapFromFileUploadRequest(request,requestEncoding);
			initModelFromRequestDataMap(dataMap, result);
		}
		break;
		case json:
		{
			InputStream in;
            try {
                in = request.getInputStream();
            } catch (IOException e) {
                throw new TeshehuiRuntimeException("getModelFromRequest contentType isJSONData getInputStream illegal",e);
            }
            String jsonString = new String(IOUtil.getByteArrayFromInputStream(in),Charset.forName(requestEncoding));
            IOUtil.closeInputStream(in);
            result = JSONUtil.toModel(jsonString, clazz);
		}
		break;
		}

		return result;
	}

	private static Map<String, List<Object>> parameterMapToDataMap(
			Map<String, List<String>> parameterMap) {
		Map<String, List<Object>> map = new HashMap<String, List<Object>>();
		Set<Entry<String, List<String>>> entrySet = parameterMap.entrySet();
		for(Entry<String, List<String>> entry : entrySet)
		{
			String key = entry.getKey();
			List<String> value = entry.getValue();
			List<Object> list = new ArrayList<Object>(value);
			map.put(key, list);
		}
		return map;
	}
	
	public static Map<String, Object> getParamMapFromRequest(HttpServletRequest request)
	{
		Map<String,Object> map = new HashMap<String, Object>();
		Map<String,List<Object>> paramMaps = getMapFromRequest(request);
		for (String key : paramMaps.keySet()) {
			Object value = CodeUtil.isEmpty(paramMaps.get(key))?null:paramMaps.get(key).get(0);
			map.put(null, value);
		}
		return map;
	}
	
	/** 根据HttpServletRequest 获取请求Map */
	public static Map<String, List<Object>> getMapFromRequest(HttpServletRequest request)
	{
		if(CodeUtil.isEmpty(request))
			throw new WebException("param empty,at " + CodeUtil.getLocation());
		
		Map<String, List<Object>> result = new HashMap<String, List<Object>>();
		
		String requestEncoding = request.getCharacterEncoding();
		requestEncoding = CodeUtil.isEmpty(requestEncoding)?REQUEST_DEFAULT_ENCODING:requestEncoding;
		
		RequestContentTypelEnum type = RequestContentTypelEnum.getInstance(request.getContentType());
		switch (type) {
		case normal:
		{
			Map<String, List<String>> parameterMap = getHttpRequestParameterMap(request,requestEncoding);
			result.putAll(parameterMapToDataMap(parameterMap));
		}
		break;
		case file:
		{
			Map<String, List<Object>> dataMap = getListMapFromFileUploadRequest(request,requestEncoding);
			result.putAll(dataMap);
		}
		break;
		case json:
		{
			InputStream in;
            try {
                in = request.getInputStream();
            } catch (IOException e) {
                throw new TeshehuiRuntimeException("getModelFromRequest contentType isJSONData getInputStream illegal",e);
            }
            String jsonString = new String(IOUtil.getByteArrayFromInputStream(in),Charset.forName(requestEncoding));
            IOUtil.closeInputStream(in);
        	Object obj = JSONUtil.toObject(jsonString, null);
        	if(obj instanceof Map)
        	{
        		@SuppressWarnings("unchecked")
    			Map<String, List<Object>> map = (Map<String, List<Object>>) JSONUtil.toObject(jsonString, null);
                result.putAll(map);
        	}
        	else
        	{
        		// TODO: handle exception
				LogUtil.w("request get json data not support,jsonString="+jsonString, null);
        	}	
		}
		break;
		}
		
        return result;
	}
	
	private static void initModelFromRequestDataMap(Map<String, List<Object>> dataMap, Object model)
	{
        if(!CodeUtil.isEmpty(dataMap))
        {
        	// get filed and setMethod map
            Class<?> clazz = model.getClass();
            Map<Field, Method> fieldAndSetmethodMap = ReflectUtil.getFieldAndSetmethodMap(clazz);

            // initialize value
        	for (Entry<Field, Method> entry : fieldAndSetmethodMap.entrySet())
            {
                Field field = entry.getKey();
                String mapKey = ReflectUtil.getFieldSetMethodAliasName(clazz,field);
                
                Class<?> fieldType = field.getType();

                Object fieldValue = null;
                List<?> tempList = dataMap.get(mapKey);
                if(!CodeUtil.isEmpty(tempList))
                {
                    int tempListSize = tempList.size();
                    if(fieldType.isArray())
                    {
                        Class<?> oneClass = fieldType.getComponentType();
                        Object[] objArray = (Object[]) Array.newInstance(oneClass, tempListSize);
                        
                        for (int i = 0; i < tempListSize; i++) {
                        	Object one = tempList.get(i);
                        	if(UploadFilePO.class.isAssignableFrom(oneClass))
                            {
                            	objArray[i] = one;
                            }
                            else
                            {
                                objArray[i] = getFieldValueByFieldType(StringUtil.toString(one), field, oneClass);
                            }
						}
                        fieldValue = objArray;
                    }
                    else if(Collection.class.isAssignableFrom(fieldType))
                    {
                        Class<?> oneClass = (Class<?>) ((ParameterizedType) field
                                .getGenericType()).getActualTypeArguments()[0];
                        Collection<Object> collection = new ArrayList<Object>();
                        
                        for (int i = 0; i < tempListSize; i++) {
                        	Object one = tempList.get(i);
                        	if(UploadFilePO.class.isAssignableFrom(oneClass))
                            {
                            	collection.add(one);
                            }
                            else
                            {
                                collection.add(getFieldValueByFieldType(StringUtil.toString(one), field, oneClass));
                            }
						}
                        fieldValue = collection;
                    }
                    else 
                    {
                        if(tempListSize>=1)
                        {
                        	Object one = tempList.get(0);
                        	if(UploadFilePO.class.isAssignableFrom(fieldType))
                            {
                            	fieldValue = one;
                            }
                            else
                            {
                                fieldValue = getFieldValueByFieldType(StringUtil.toString(one), field, fieldType);
                            }
                        }
                    }
                }
                
                if(fieldValue!=null)
                {
                    ReflectUtil.setMethodInvoke(model, mapKey, fieldValue);
                }
            }
        }
	}

	/**
	 * @author wangwwy
	 * createdDatetime 2014年9月5日 下午4:43:05
	 * @param string
	 * @param fieldType
	 * @param objectType
	 * @return
	 */
	private static Object getFieldValueByFieldType(String stringValue, Field field,
			Class<?> valueType) {
		
		FieldTypeEnum fieldType = FieldTypeEnum.simple;
		Class<FieldTypeAnnotation> annotationClass = FieldTypeAnnotation.class;
		if(field.isAnnotationPresent(annotationClass))
		{
			fieldType = field.getAnnotation(annotationClass).value();
		}
		switch (fieldType) {
		case json:
			return JSONUtil.toObject(stringValue, valueType);
		case xml:
			return XmlUtil.toObject(stringValue, valueType);
		default:
		    if(CodeUtil.isEmpty(stringValue))
		    {
		        if(String.class.isAssignableFrom(valueType))
		        {
		            return stringValue;
		        }
		        else
		        {
		            return null;
		        }
		    }
		    else
		    {
		        return StringUtil.getValueFromString(stringValue, valueType);
		    }
		}
	}
	
	protected static Map<String, List<Object>> getListMapFromFileUploadRequest(HttpServletRequest request,String requestEncoding)
	{
		if (isNewReqeust(request))
		{
			initPostRequestDataListMap(request,requestEncoding);
		}
		return requestDataListMap;
	}
	
	private static void initPostRequestDataListMap(HttpServletRequest request,String requestEncoding)
	{
		requestDataListMap = new HashMap<String, List<Object>>();
		try
		{
			/*
			 * 设置编码
			 */
			request.setCharacterEncoding(requestEncoding);

			/*
			 * 获得磁盘文件条目工厂
			 */
			DiskFileItemFactory factory = new DiskFileItemFactory();

			/*
			 * 如果没以下两行设置的话，上传大的 文件 会占用 很多内存，设置暂时存放的 存储室 , 这个存储室，可以和 最终存储文件
			 * 的目录不同原理 它是先存到 暂时存储室，然后在真正写到 对应目录的硬盘上， 按理来说
			 * 当上传一个文件时，其实是上传了两份，第一个是以 .tem 格式的 然后再将其真正写到 对应目录的硬盘上
			 */
			String tempDirectory = request.getSession().getServletContext()
					.getRealPath(FILE_UPLOAD_TEMP_SAVE_DIRECTORY);
			File tempFileDirectory = new File(tempDirectory);
			if (!tempFileDirectory.exists())
				tempFileDirectory.mkdirs();
			factory.setRepository(tempFileDirectory);
			/*
			 * 设置 缓存的大小，当上传文件的容量超过该缓存时，直接放到 暂时存储室
			 */
			factory.setSizeThreshold(1024 * 1024);

			// 高水平的API文件上传处理
			ServletFileUpload upload = new ServletFileUpload(factory);

			/*
			 * 可以上传多个文件
			 */
			@SuppressWarnings("unchecked")
			List<FileItem> list = upload.parseRequest(request);

			for (FileItem item : list)
			{
				/*
				 * 获取表单的属性名字
				 */
				String itemName = item.getFieldName();
				if(!requestDataListMap.containsKey(itemName))
				{
					requestDataListMap.put(itemName, new ArrayList<Object>());
				}

				/*
				 * 如果获取的 表单信息是普通的 文本 信息
				 */
				if (item.isFormField())
				{
					/*
					 * 获取用户具体输入的字符串 ，名字起得挺好，因为表单提交过来的是 字符串类型的
					 */
					String value = item.getString(requestEncoding);
					requestDataListMap.get(itemName).add(value);
				}
				/*
				 * 对传入的非 简单的字符串进行处理 ，比如说二进制的 图片，电影这些
				 */
				else
				{
					/*
					 * 以下三步，主要获取 上传文件的名字
					 */
					/*
					 * 获取路径名
					 */
					String value = item.getName();
					/*
					 * 索引到最后一个反斜杠
					 */
					int start = value.lastIndexOf(FILE_UPLOAD_FILE_PATH_SEPARATOR);
					/*
					 * 截取 上传文件的 字符串名字，加1是 去掉反斜杠，
					 */
					String filename = value.substring(start + FILE_UPLOAD_FILE_PATH_SEPARATOR.length());
					/*
					 * 读取文件内容
					 */
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					InputStream in = item.getInputStream();
					int data = 0;
					while (true)
					{
						data = in.read();
						if (data == -1)
							break;
						baos.write(data);
					}
					in.close();
					baos.close();
					byte[] fileData = baos.toByteArray();
					requestDataListMap.get(itemName).add(new UploadFilePO(fileData,filename));
				}
			}

			LogUtil.d("fileUpload success,requestDataListMap="+JSONUtil.toString(requestDataListMap));
		}
		catch (Exception e)
		{
			LogUtil.e("FileUploadException", e);
		}
	}

	public static void response(HttpServletRequest request,
			byte[] responseData, HttpServletResponse response)
	{

		// HttpServletResponse
		try
		{
			// response.addHeader("Access-Control-Allow-Origin", "*");
			// response.addHeader("Access-Control-Allow-Headers",
			// "POWERED-BY-WANGWWY");
			// response.addHeader("Access-Control-Allow-Methods",
			// "POST, GET, OPTIONS");
			// response.addHeader("Access-Control-Max-Age", "30");

			// response.setContentType("application/x-www-form-urlencoded");
			// response.setContentType("text/javascript");

			// download data
			// response.setContentType("application/*");
			// response.setHeader("Content-Disposition",
			// "attachment;filename=temp.txt");

			BufferedOutputStream bos = new BufferedOutputStream(
					response.getOutputStream());
			bos.write(responseData);
			bos.close();
		} catch (IOException e)
		{
			LogUtil.w("Response IOException occured.", e);
			throw new WebException("网络异常");
		}
	}

	public static void responseByString(HttpServletRequest request,
			String responseStringData, String responseEncoding,
			HttpServletResponse response)
	{
		response.setContentType("text/html;charset="+responseEncoding);
		response.setCharacterEncoding(responseEncoding);

		BufferedOutputStream bos = null;
		try
		{
			byte[] responseData = responseStringData.getBytes(responseEncoding);

			bos = new BufferedOutputStream(
					response.getOutputStream());
			bos.write(responseData);
		} catch (Exception e)
		{
			LogUtil.w(
					"Response by string Exception occured," + e.getMessage(), e);
			throw new WebException("网络异常");
		}
		finally
		{
			IOUtil.closeOutputStream(bos);
		}
	}
	
	/**
	 * 获取httpRequestParameterMap
	 * @author wangwenwu
	 * 2014年12月3日  下午11:54:20
	 */
	protected static Map<String, List<String>> getHttpRequestParameterMap(HttpServletRequest httpRequest,String requestEncoding)
	{
		Map<String, List<String>> result = new HashMap<String, List<String>>();
		
		// put queryString
		String queryString = httpRequest.getQueryString();
		parseKeyValueParam(result, queryString, requestEncoding);
		
		// put postData String
		InputStream in = null;
        try {
            in = httpRequest.getInputStream();
            byte[] postData = IOUtil.getByteArrayFromInputStream(in);
            String s = new String(postData,requestEncoding);
            parseKeyValueParam(result, s, requestEncoding);
        } catch (Exception e) {
            throw new TeshehuiRuntimeException("getHttpRequestParameterMap from httpRequest inputStream illegal",e);
        }
		finally
		{
		    IOUtil.closeInputStream(in);
		}
		
		return result;
	}
	
	private static void parseKeyValueParam(Map<String, List<String>> map,String s, String encoding)
	{
	    if(!CodeUtil.isEmpty(s))
        {
            List<String> list = StringUtil.stringToList(s, "&");
            for (String one : list) {
                List<String> keyValueList = StringUtil.stringToList(one, "=");
                if(keyValueList.size()==2)
                {
                    String key = keyValueList.get(0);
                    try {
                        key=URLDecoder.decode(key, encoding);
                    } catch (UnsupportedEncodingException e) {
//                        throw new TeshehuiRuntimeException("URLDecoder decode characterEncoding illegal,encoding="+encoding);
                        // 2015-07-31当遇到URLDecoder异常，value为原始字符,不做处理
                        LogUtil.w(ServletUtil.class.getName(), "URLDecoder.decode illegal key="+key+",encoding="+encoding, e);
                    }
                    String value = keyValueList.get(1);
                    try {
                        value=URLDecoder.decode(value, encoding);
                    } catch (Exception e) {
                        //throw new TeshehuiRuntimeException("URLDecoder decode characterEncoding illegal,encoding="+encoding);
                        // 2015-07-31当遇到URLDecoder异常，value为原始字符,不做处理
                        LogUtil.w(ServletUtil.class.getName(), "URLDecoder.decode illegal value="+value+",encoding="+encoding, e);
                    }
                    List<String> oldValueList = map.get(key);
                    if(oldValueList==null)
                    {
                        map.put(key, new ArrayList<String>());
                    }
                    map.get(key).add(value);
                }
            }
        }
	}
	
	   /** 将模型转换为普通HTTP POST请求体内容 */
    public static String modelToPostParam(Object obj,String encoding)
    {
        StringBuffer sb = new StringBuffer();
        
        if(obj==null)
            return sb.toString();
        
        boolean valueEncode = CodeUtil.isEmpty(encoding)?false:true;
        Class<?> clazz = obj.getClass();
        Map<String, Method> fieldNameAndGetmethodMap = ReflectUtil.getFieldNameAndGetmethodMap(clazz);
        Set<Entry<String, Method>> entrySet = fieldNameAndGetmethodMap.entrySet();
        for(Entry<String,Method> entry : entrySet)
        {
            String key = entry.getKey();
            Object fieldValue = ReflectUtil.getMethodInvoke(obj, entry.getKey());
            
            if(!CodeUtil.isEmpty(key)&&fieldValue!=null)
            {
                Class<?> fieldValueClass = fieldValue.getClass();
                if(ReflectUtil.isArray(fieldValueClass, fieldValue))
                {
                    Object[] array = ReflectUtil.getArray(fieldValue);
                    for (Object one : array) {
                        String oneValue = one.toString();
                        oneValue = valueEncode?getEncodeValue(oneValue,encoding):oneValue;
                        sb.append("&").append(key).append("=").append(oneValue);
                    }
                }
                else if(Collections.class.isAssignableFrom(fieldValueClass))
                {
                    Collection<?> collection = (Collection<?>) fieldValue; 
                    for (Object one : collection) {
                        String oneValue = one.toString();
                        oneValue = valueEncode?getEncodeValue(oneValue,encoding):oneValue;
                        sb.append("&").append(key).append("=").append(oneValue);
                    }
                }
                else
                {
                    String oneValue = fieldValue.toString();
                    oneValue = valueEncode?getEncodeValue(oneValue,encoding):oneValue;
                    sb.append("&").append(key).append("=").append(oneValue);
                }
            }
        }
        
        String result = sb.toString();
        if(result.startsWith("&"))
        {
            result = result.substring("&".length());
        }
        return result;
    }
    
    private static String getEncodeValue(String oneValue, String encoding) {
        try {
            oneValue = URLEncoder.encode(oneValue, encoding);
        } catch (Exception e) {
            LogUtil.w(ServletUtil.class.getName(), "getEncodingValue illegal,encoding="+encoding, e);
        }
        return oneValue;
    }

    /** 将模型转换为普通HTTP POST请求体内容 */
    public static String modelToPostParam(Object obj)
    {
        return modelToPostParam(obj,null);
    }
    
    public static String mapToPostParam(Map<String, ?> map, String encoding)
    {
        StringBuffer sb = new StringBuffer();
        
        if(map==null)
            return sb.toString();
        
        boolean valueEncode = CodeUtil.isEmpty(encoding)?false:true;
        Set<String> keySet = map.keySet();
        for(String key : keySet)
        {
            Object fieldValue = map.get(key);
            
            if(!CodeUtil.isEmpty(key)&&fieldValue!=null)
            {
                Class<?> fieldValueClass = fieldValue.getClass();
                if(ReflectUtil.isArray(fieldValueClass, fieldValue))
                {
                    Object[] array = ReflectUtil.getArray(fieldValue);
                    for (Object one : array) {
                        String oneValue = one.toString();
                        oneValue = valueEncode?getEncodeValue(oneValue,encoding):oneValue;
                        sb.append("&").append(key).append("=").append(oneValue);
                    }
                }
                else if(Collections.class.isAssignableFrom(fieldValueClass))
                {
                    Collection<?> collection = (Collection<?>) fieldValue; 
                    for (Object one : collection) {
                        String oneValue = one.toString();
                        oneValue = valueEncode?getEncodeValue(oneValue,encoding):oneValue;
                        sb.append("&").append(key).append("=").append(oneValue);
                    }
                }
                else
                {
                    String oneValue = fieldValue.toString();
                    oneValue = valueEncode?getEncodeValue(oneValue,encoding):oneValue;
                    sb.append("&").append(key).append("=").append(oneValue);
                }
            }
        }
        
        String result = sb.toString();
        if(result.startsWith("&"))
        {
            result = result.substring("&".length());
        }
        return result;
    }
    
    /** 获取请求的IP地址 */
    public static String getRequestRemoteAddress(HttpServletRequest request){
        String ip = request.getHeader("x-forwarded-for");
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
            ip = request.getHeader("Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
            ip = request.getRemoteAddr();
        }
        if(ip!=null&&ip.equals("0:0:0:0:0:0:0:1"))
        {
            ip = "127.0.0.1";
        }
        if(ip!=null&&ip.contains(","))
        {
            ip = ip.substring(0, ip.indexOf(","));
        }
        return ip;
    }
	
	/** 请求转发 */   
    public static void requestForward(HttpServletRequest httpRequest,HttpServletResponse response)
    {
        // request
//        String requestEncoding = httpRequest.getCharacterEncoding();
        
        // queryString httpUrl
        String queryString = httpRequest.getQueryString();
        int indexOf = queryString.indexOf("httpUrl=");
        if(indexOf<0)
        {
            throw new TeshehuiRuntimeException("getRemoteService httpUrl not exist");
        }
        String httpUrl = queryString.substring(indexOf+"httpUrl=".length());
//        String httpUrlNoParam = null;
//        String httpUrlParam = null;
//        int indexOf1 = queryString.indexOf("?",indexOf);
//        if(indexOf1<0)
//        {
//            httpUrlNoParam = httpUrl;
//            httpUrlParam = "";
//        }
//        else
//        {
//            httpUrlNoParam = httpUrl.substring(0,indexOf1);
//            httpUrlParam = queryString.substring(indexOf1);
//        }
        
        // request header
        List<Object[]> requestHeaderList = new ArrayList<Object[]>();
        Enumeration<?> headers = httpRequest.getHeaderNames();
        while (headers.hasMoreElements()) {
            String name = headers.nextElement().toString();
            requestHeaderList.add(new Object[]{name,httpRequest.getHeader(name)});
        }
        int length = requestHeaderList.size();
        Object[][] requestHeaderProperties = new Object[length][];
        for (int i = 0; i < length; i++) {
            requestHeaderProperties[i] = requestHeaderList.get(i);
        }
        
        // requestData
        InputStream in;
        try {
             in = httpRequest.getInputStream();
        } catch (IOException e) {
            throw new TeshehuiRuntimeException("获取输入流异常",e);
        }
        byte[] data = IOUtil.getByteArrayFromInputStream(in);
        try {
            in.close();
        } catch (IOException e) {
            throw new TeshehuiRuntimeException("关闭输入流异常",e);
        }
        byte[] requestData = data;
        
        byte[] responseData = IOUtil.getByteArrayFromHttpUrl(httpUrl, requestData, requestHeaderProperties);
        ServletUtil.response(httpRequest, responseData, response);
    }
    
}
