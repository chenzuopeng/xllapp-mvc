package org.xllapp.mvc.support;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.ffcs.icity.cas.client.PrincipalHolder;

/**
 * 此拦截器用于绑定请求上线文参数到当前的线程.
 * 
 * @author dylan.chen Mar 2, 2013
 * 
 */
public class RequestContextInterceptor extends HandlerInterceptorAdapter {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		RequestContextHolder.setRequest(request);
		RequestContextHolder.setResponse(response);

		MDC.put("USER_NAME", PrincipalHolder.getPrincipal().getUserName());

		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		RequestContextHolder.clear();
	}
}
