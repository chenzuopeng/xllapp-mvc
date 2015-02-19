package org.xllapp.mvc.utils;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

/**
 * 此类提供对响应进行操作的便捷方法.
 *
 * @author dylan.chen Dec 19, 2014
 * 
 */
public abstract class ResponseUtils {

	private final static Logger LOGGER = LoggerFactory.getLogger(ResponseUtils.class);
	
	/**
	 * 获取返回json数据的视图.
	 * 
	 * 返回的json格式:
	 * 
	 *    {
     *       message: "123",
     *       data: {
     *               aaa: "123"
     *             },
     *       success: true
     *      }
	 * 
	 */
	public static View getJsonView(final boolean success,final String message){
		return new MappingJacksonJsonView(){
			@Override
			protected Object filterModel(Map<String, Object> model) {
				Map<String, Object> data = new HashMap<String, Object>(model.size());
				for (Map.Entry<String, Object> entry : model.entrySet()) {
					if(!StringUtils.startsWith(entry.getKey(), "org.springframework")){
					    data.put(entry.getKey(), entry.getValue());
					}
				}
				return getResponseContent(success, message, data);
			}
		};
	}
	
	public static void outJson(HttpServletResponse response,boolean success,String message){
		outJson(response, success, message, null);
	}
	
	public static void outJson(HttpServletResponse response,boolean success,String message,Map<String,Object> data){
		
		try {
			out(response, JSONHelper.toJSONString(getResponseContent(success, message, data)));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	
	public static Map<String,Object> getResponseContent(boolean success,String message,Object data){
		Map<String,Object> map=new HashMap<String, Object>();
		map.put("success", success);
		map.put("message", message);
		if (null != data) {
			map.put("data", data);
		}
		return map;
	}
	
	public static void out(HttpServletResponse response, String content){
		out(HttpServletResponse.SC_OK,response, content);
	}
	
	public static void out(int status,HttpServletResponse response, String content){
		LOGGER.debug("response:{}", content);
		response.setContentType("text/html;charset=utf-8");
		response.setCharacterEncoding("UTF-8");
		response.setStatus(status);
		PrintWriter out = null;
		try {
			out = response.getWriter();
			out.write(content);
		} catch (Exception e) {
			LOGGER.error("failure to send response data.caused by:" + e.getLocalizedMessage(), e);
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

}
