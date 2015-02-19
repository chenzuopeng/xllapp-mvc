package org.xllapp.mvc.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UrlPathHelper;
import org.xllapp.mvc.entity.IdEntity;
import org.xllapp.mvc.support.RequestContextHolder;
import org.xllapp.mvc.utils.ResponseUtils;

/**
 * 此类是CRUDController的便捷实现,支持同步和异步方式进行CRUD操作.
 * 
 * @author dylan.chen Mar 18, 2014
 * 
 */
public abstract class SimpleController<T extends IdEntity> extends CRUDController<T> {

	public final static String LIST_VIEW_NAME = "list";

	public final static String FORM_VIEW_NAME = "form";

	private String viewDir;

	{
		this.viewDir = getEntityName();
	}

	protected String getEntityName() {
		Class<?> clazz = getEntityType();
		String entityName = clazz.getSimpleName();
		return entityName.toLowerCase();
	}
	
	protected Object getErrorView(Throwable throwable) {
		if (isResponseByJson()) {
			return ResponseUtils.getJsonView(false, throwable.getLocalizedMessage()); 
		} else {
			throw new RuntimeException(throwable.getLocalizedMessage(),throwable);
		}
	}

	@Override
	protected Object getSaveView(boolean success, Throwable throwable, T entity, ModelAndView model) {
		
		if(!success){
			return getErrorView(throwable);
		}
		
		if(isResponseByJson()){
			return ResponseUtils.getJsonView(true, "保存成功");
		}
		
		return getRedirectView();
	}

	@Override
	protected Object getDeleteView(boolean success, Throwable throwable, Long[] ids, ModelAndView model) {
		
		if(!success){
			return getErrorView(throwable);
		}
		
		if(isResponseByJson()){
			return ResponseUtils.getJsonView(true, "删除成功");
		}
		
		return getRedirectView();
	}

	@Override
	protected Object getFormView(boolean success, Throwable throwable, T entity, ModelAndView model) {
				
		if(!success){
			return getErrorView(throwable);
		}
		
		if(isResponseByJson()){
			return ResponseUtils.getJsonView(true, null);
		}
		
		return getFullViewName(FORM_VIEW_NAME);
	}

	@Override
	protected Object getQueryView(boolean success, Throwable throwable, Map<String, Object> queryParams, ModelAndView model) {

		if(!success){
			return getErrorView(throwable);
		}
		
		if(isResponseByJson()){
			return ResponseUtils.getJsonView(true, "查询成功");
		}

		return getFullViewName(LIST_VIEW_NAME);
	}

	protected String getFullViewName(String viewName) {
		return this.viewDir + "/" + viewName;
	}

	protected String getRedirectView() {
		HttpServletRequest req = RequestContextHolder.getRequest();
		String mappingPath = new UrlPathHelper().getPathWithinApplication(req);
		return "redirect:" + mappingPath;
	}
	
	protected boolean isResponseByJson(){
		HttpServletRequest request = RequestContextHolder.getRequest();
		return StringUtils.equalsIgnoreCase(request.getParameter("_responseContentType"), "json")
				|| StringUtils.equalsIgnoreCase(request.getHeader("X-Requested-With"), "XMLHttpRequest");
	}

}
