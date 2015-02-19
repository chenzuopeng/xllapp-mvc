package org.xllapp.mvc.support.freemarker;

import javax.servlet.ServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.context.request.NativeWebRequest;

import org.xllapp.mybatis.Page;

/**
 * 处理com.ffcs.icity.mybatis.Page类型的参数.
 *
 * @author dylan.chen Mar 8, 2013
 * 
 */
public class PageArgumentResolver implements WebArgumentResolver {
	
	private final static Logger logger=LoggerFactory.getLogger(PageArgumentResolver.class);

	@Override
	public Object resolveArgument(MethodParameter methodParameter, NativeWebRequest webRequest) throws Exception {
		if (methodParameter.getParameterType().equals(Page.class)) {
			ServletRequest servletRequest =(ServletRequest) webRequest.getNativeRequest();
			Page<Object> page=new Page<Object>();
			String pageNo=(String)servletRequest.getParameter("pageNo");
			logger.debug("请求中原始的pageNo:{}",pageNo);
			if(StringUtils.isNumeric(pageNo)){
				page.setPageNo(Integer.valueOf(pageNo));
			}
			String pageSize=(String)servletRequest.getParameter("pageSize");
			logger.debug("请求中原始的pageSize:{}",pageSize);
			if(StringUtils.isNumeric(pageSize)){
				page.setPageSize(Integer.valueOf(pageSize));
			}
			logger.debug("最终生产的Page对象:{}",page);
			servletRequest.setAttribute("page",page);
			return page;
		}
		return UNRESOLVED;
	}

}
