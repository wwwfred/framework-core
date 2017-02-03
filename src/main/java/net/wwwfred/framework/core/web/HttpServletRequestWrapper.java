package net.wwwfred.framework.core.web;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import net.wwwfred.framework.util.code.CodeUtil;
import net.wwwfred.framework.util.io.IOUtil;
import net.wwwfred.framework.util.string.StringUtil;

public class HttpServletRequestWrapper extends javax.servlet.http.HttpServletRequestWrapper
 {
//	protected HttpServletRequest request;
	protected byte[] inputData;
	protected Map<String,List<Object>> requestDataMap;
	protected Enumeration<String> requestParameterNames;

	public HttpServletRequestWrapper(HttpServletRequest request) {
		super(request);
		if(!(request instanceof HttpServletRequestWrapper)&&inputData==null&&requestDataMap==null)
		{
//			this.request = new javax.servlet.http.HttpServletRequestWrapper(request);
			HttpServletRequest inputDataRequest = new javax.servlet.http.HttpServletRequestWrapper(request);
			InputStream in = null;
			try {
				in = inputDataRequest.getInputStream();
				inputData = IOUtil.getByteArrayFromInputStream(in);
				requestDataMap = ServletUtil.getMapFromRequest(this);
			} catch (IOException e) {
				throw new WebException(
						"MyHttpServletRequestWrapper construct illegal", e);
			} finally {
				IOUtil.closeInputStream(in);
			}
		}
		else
		{
			HttpServletRequestWrapper oldRequest = (HttpServletRequestWrapper)request;
//			this.request = oldRequest.request;
			this.inputData = oldRequest.inputData;
			this.requestDataMap = oldRequest.requestDataMap;
		}
	}

	public BufferedReader getReader() throws IOException {
		return new BufferedReader(new InputStreamReader(getInputStream()));
	}

	public ServletInputStream getInputStream() throws IOException {
		final ByteArrayInputStream bais = new ByteArrayInputStream(inputData);
		return new ServletInputStream() {

			@Override
			public int read() throws IOException {
				return bais.read();
			}
		};
	}
	
	@Override
	public String getParameter(String name) {
		// TODO Auto-generated method stub
//		return request.getParameter(name);
		List<Object> list = requestDataMap.get(name);
		if(!CodeUtil.isEmpty(list))
		{
			return StringUtil.toString(list.get(0));
		}
		return null;
	}
	
	@Override
	public Map<String, String[]> getParameterMap() {
		// TODO Auto-generated method stub
//		return request.getParameterMap();
		Map<String, String[]> parameterMap = new LinkedHashMap<String, String[]>();
		Set<Entry<String,List<Object>>> entrySet = requestDataMap.entrySet();
		for (Entry<String, List<Object>> entry : entrySet) {
			String key = entry.getKey();
			if(parameterMap.containsKey(key))
			{
				List<Object> list = requestDataMap.get(key);
				if(!CodeUtil.isEmpty(list))
				{
					int size = list.size();
					String[] value = new String[size];
					for (int i = 0; i < size; i++) {
						value[i] = StringUtil.toString(list.get(i));
					}
					parameterMap.put(key, value);
				}
			}
		}
		return parameterMap;
	}
	
	@Override
	public Enumeration<String> getParameterNames() {
		// TODO Auto-generated method stub
//		return request.getParameterNames();
		
		if(requestParameterNames==null)
		{
			final Map<String,List<Object>> newRequestDataMap = new HashMap<String, List<Object>>(requestDataMap);
			
			requestParameterNames = new Enumeration<String>() {
	
				@Override
				public boolean hasMoreElements() {
					// TODO Auto-generated method stub
//					return iterator.hasNext();
					return newRequestDataMap.keySet().iterator().hasNext();
				}
	
				@Override
				public String nextElement() {
					// TODO Auto-generated method stub
//					return iterator.next();
					return newRequestDataMap.keySet().iterator().next();
				}
			};
		}
		return requestParameterNames;
	}
	
}