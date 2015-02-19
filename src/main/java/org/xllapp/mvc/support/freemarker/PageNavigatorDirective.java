package org.xllapp.mvc.support.freemarker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xllapp.mvc.support.RequestContextHolder;

import org.xllapp.mybatis.Page;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

/**
 * 分页标签.
 * 
 * @author dylan.chen Mar 5, 2013
 * 
 */
public class PageNavigatorDirective implements TemplateDirectiveModel {

	private final static Logger logger = LoggerFactory.getLogger(PageNavigatorDirective.class);

	/**
	 * 排除的请求参数的名字列表,小写
	 */
	private List<String> excludeReqParams = new ArrayList<String>() {

		private static final long serialVersionUID = 8330080263534294807L;

		{
			add("msg"); // 此参数当前用于传递提示消息到页面
			add("pagesize");
			add("pageno");
		}
	};

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
		HttpServletRequest request = RequestContextHolder.getRequest();
		Page<?> page = getPage(params);
		if (page == null) {
			return;
		}

		boolean isPost = DirectiveUtils.getBool("post", true, params);
		StringBuilder bar = new StringBuilder();
		bar.append("<form style=\"margin:0px;padding:0px;\" id='pager' name='pager' action='" + this.getRequestPath(request) + (isPost ? "" : getQueryString(request)) + "' method='" + (isPost ? "post" : "get") + "'> \n");
		bar.append("<input id='pageNo' name='pageNo' type='hidden' value='" + page.getPageNo() + "'/> \n");
		bar.append("<input id='pageSize' name='pageSize' type='hidden' value='" + page.getPageSize() + "'/> \n");
		if (isPost) {
			String paramName = null;
			String[] paramValues = null;
			for (Enumeration<String> paramNames = request.getParameterNames(); paramNames.hasMoreElements();) {
				paramName = paramNames.nextElement();
				if (excludedParam(paramName)) {
					logger.debug("排除参数 - {},排除列表 - {}", paramName, this.excludeReqParams);
					continue;
				}
				paramValues = request.getParameterValues(paramName);
				for (String paramValue : paramValues) {
					bar.append("<input name='" + paramName + "' type='hidden' value='" + paramValue + "'/> \n");
				}
			}
		}

		String btnClass = DirectiveUtils.getString("btnClass", "btn", params);

		bar.append("每页 \n");
		bar.append("<input id='newPageSize' type='text' maxlength='2' size='2' onkeyup='if(this.value.replace(/[\\d+]/ig,\"\").length>0){this.value=\"" + page.getPageSize() + "\"}' value='" + page.getPageSize() + "'/>\n");
		bar.append("条记录|共<font color=\"red\">" + page.getTotalPages() + "</font>页/<font color=\"red\">" + page.getTotalCount() + "</font>条记录");
		bar.append("<input value='首页' type='button' class='" + btnClass + "' onclick=\"javascript:document.pager.pageNo.value='" + page.getFirstPage() + "';document.pager.submit();return false;\">");
		bar.append("<input value='上一页' type='button' class='" + btnClass + "' onclick=\"javascript:document.pager.pageNo.value='" + page.getPrePage() + "';document.pager.submit();return false;\">");
		bar.append("<input value='下一页' type='button' class='" + btnClass + "' onclick=\"javascript:document.pager.pageNo.value='" + page.getNextPage() + "';document.pager.submit();return false;\">");
		bar.append("<input value='尾页' type='button' class='" + btnClass + "' onclick=\"javascript:document.pager.pageNo.value='" + page.getLastPage() + "';document.pager.submit();return false;\">");
		bar.append("第");
		bar.append("<input id='goPageNo' type='text' size='2' value='" + page.getPageNo() + "' onkeyup='if(this.value.replace(/[\\d+]/ig,\"\").length>0){this.value=\"" + page.getPageNo() + "\"}'/>");
		bar.append("页");
		bar.append("<input value='转到' type='button' class='" + btnClass + "' onclick=\"this.form.pageSize.value=this.form.newPageSize.value;this.form.pageNo.value=this.form.goPageNo.value;this.form.submit();\">");
		bar.append("</form>");
		env.getOut().write(bar.toString());
	}

	private boolean excludedParam(String paramName) {
		return this.excludeReqParams.contains(paramName.toLowerCase());
	}

	/**
	 * 获取Page对象.
	 * 
	 * @return Page page对象
	 */
	@SuppressWarnings("rawtypes")
	private Page<?> getPage(Map params) {
		return (Page<?>) RequestContextHolder.getRequest().getAttribute("page");
	}

	/**
	 * 获取客户端请求的操作的URL.
	 * 
	 * @param request
	 *            请求对象
	 * @return String
	 */
	private String getRequestPath(HttpServletRequest request) {
		String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
		String requestUri = request.getRequestURI();
		return basePath + (StringUtils.isBlank(requestUri) ? "" : requestUri);
	}

	/**
	 * 获取查询参数.
	 * 
	 * @param request
	 *            请求对象
	 * @return String
	 */
	private String getQueryString(HttpServletRequest request) {
		String queryString = request.getQueryString();
		return StringUtils.isNotBlank(queryString) ? "?" + queryString : "";
	}

}
