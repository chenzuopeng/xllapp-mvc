package org.xllapp.mvc.utils;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * 此类提供对JSON进行操作的便捷方法.
 *
 * @author dylan.chen Sep 21, 2013
 * 
 */
public class JSONHelper {

	private final static ObjectMapper OBJECT_MAPPER=new ObjectMapper();
	
	/**
	 * 将对象转换成json字符串.
	 */
	public static String toJSONString(Object object) throws Exception{
		return OBJECT_MAPPER.writeValueAsString(object);
	}
	
	/**
	 * 类似toJSONString()方法，除了在转换过程出现异常时,返回""(空串).
	 */
	public static String toJSONStringQuietly(Object object) {
		try {
			return OBJECT_MAPPER.writeValueAsString(object);
		} catch (Exception e) {
			return "";
		}
	}
	
}
