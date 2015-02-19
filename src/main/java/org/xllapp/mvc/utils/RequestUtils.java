package org.xllapp.mvc.utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.propertyeditors.CustomDateEditor;

/**
 * 此类提供对请求进行操作的便捷方法.
 * 
 * @author dylan.chen Mar 12, 2014
 * 
 */
public abstract class RequestUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(RequestUtils.class);

	/**
	 * 生成请求ID.
	 */
	public static String generateRequestId(HttpServletRequest request) {
		return DateFormatUtils.format(new Date(), "yyyyMMddHHmmss") + "-" + RandomStringUtils.randomNumeric(2);
	}

	/**
	 * 获取请求参数.
	 * 
	 * @param request 请求对象
	 * @param name 参数名
	 * @param defaultValue 参数默认值,当此参数不存在时,返回的此值
	 * @return String
	 */
	public static String getRequestParam(HttpServletRequest request, String name, String defaultValue) {
		String value = request.getParameter(name);
		return value != null ? value : defaultValue;
	}

	/**
	 * 获取请求参数.
	 * 
	 * @param request 请求对象
	 * @param name 参数名
	 * @param defaultValue 参数默认值,当此参数不存在时,返回的此值
	 * @return int
	 */
	public static int getRequestParam(HttpServletRequest request, String name, int defaultValue) {
		String value = request.getParameter(name);
		return StringUtils.isNotBlank(value) ? Integer.valueOf(value) : defaultValue;
	}

	/**
	 * 解析请求参数,返回一个包含所有参数的map.
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> resolveParams(HttpServletRequest request) {
		Map<String, Object> result = new HashMap<String, Object>();
		for (Enumeration<String> paramNames = request.getParameterNames(); paramNames.hasMoreElements();) {
			String paramName = paramNames.nextElement();
			String[] values = request.getParameterValues(paramName);
			result.put(paramName+"_array", values);
			if (values.length > 1) {
				result.put(paramName, values);
			} else {
				result.put(paramName, values[0]);
			}
		}
		return result;
	}

	/**
	 * 解析请求参数,返回一个包含所有参数的对象.
	 * 
	 * @param clazz 返回对象的类型
	 * @param request 请求对象
	 * @return 保存所有请求参数的对象
	 */
	@SuppressWarnings("unchecked")
	public static <T> T extractObject(Class<?> clazz, HttpServletRequest request) {
		Map<String, Object> params = resolveParams(request);
		BeanWrapperImpl bean = new BeanWrapperImpl(clazz);
		bean.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"), true));
		if (MapUtils.isNotEmpty(params)) {
			PropertyValues propertyValues = new MutablePropertyValues(params);
			bean.setPropertyValues(propertyValues, true);
		}
		return (T) bean.getWrappedInstance();
	}

	/**
     * 如果当前的请求URL为http://localhost:8080/icity-portal-archetype/abc/upload/demo
	 * 
	 * 其中:
	 *     contextPath:/icity-portal-demo
	 *     servletPath:/abc
	 * 
	 * 那么:
	 * 
	 *   RequestUtils.getFullPath(request,"/a.html") = /icity-portal-demo/abc/a.html
	 *   RequestUtils.getFullPath(request,"a.html") = /icity-portal-demo/abc/a.html
	 *   RequestUtils.getFullPath(request,"http://localhost:8080/a.html") = http://localhost:8080/a.html
	 *   RequestUtils.getFullPath(request,"https://localhost:8080/b.html") = https://localhost:8080/b.html
	 *   
	 */
	public static String getFullPath(HttpServletRequest request, String path) {

		if (isHttpUrl(path)) {
			return path;
		}

		if (!StringUtils.startsWith(path, "/")) {
			path = "/" + path;
		}

		return request.getContextPath()+ request.getServletPath() + path;
	}
	
	/**
	 * 如果当前的请求URL为http://localhost:8080/icity-portal-archetype/abc/upload/demo
	 * 
	 * 其中:
	 *     contextPath:/icity-portal-demo
	 *     servletPath:/abc
	 * 
	 * 那么:
	 *   
	 *   RequestUtils.getFullUrl(request,"/a.html") = http://localhost:8080/icity-portal-demo/abc/a.html
	 *   RequestUtils.getFullUrl(request,"a.html") = http://localhost:8080/icity-portal-demo/abc/a.html
	 *   RequestUtils.getFullUrl(request,"http://localhost:8080/a.html") = http://localhost:8080/a.html
	 *   RequestUtils.getFullUrl(request,"https://localhost:8080/b.html") = https://localhost:8080/b.html
	 *   
	 *   
	 */
	public static String getFullUrl(HttpServletRequest request,String path){
		if (isHttpUrl(path)) {
			return path;
		}
		return request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+getFullPath(request, path);
	}
	
	/**
	 * 判断给定的字符串是否url地址
	 */
	public static boolean isHttpUrl(String input){
		String tmp = StringUtils.defaultIfBlank(input, "");
		return StringUtils.startsWithAny(tmp.toLowerCase(), "http:", "https:");
	}
	
	/**
	 * 格式化url地址,主要是去除多余的分割符("/").
	 */
	public static String normalizeUrl(String url) {
		return url.replaceAll("/{2,}", "/");
	}
	
	/**
	 * 将文件路径字符串转换成uri字符串.
	 */
	public static String filePathToUri(String filePath) {
		return filePath.replaceAll(Pattern.quote("\\"), "/");
	}

	/**
	 * 获取请求客户端的ip地址.
	 */
	public static String getIp(HttpServletRequest request) {
		String ip=request.getHeader("X-Real-IP");
		if(null == ip){
			ip=request.getRemoteAddr();
		}
		return ip;
	}
	
	/**
	 * 获取字符串形式的请求体.
	 */
	public static String resolveBody(HttpServletRequest request) {
		String result;
		try {
			result = IOUtils.toString(request.getInputStream());
		} catch (IOException e) {
			logger.error("failure to resolve body", e);
			result = "[failure to resolve body]";
		}
		return result;
	}

}
