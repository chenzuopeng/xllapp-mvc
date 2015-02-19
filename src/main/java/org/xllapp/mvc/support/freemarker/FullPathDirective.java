package org.xllapp.mvc.support.freemarker;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.xllapp.mvc.support.RequestContextHolder;
import org.xllapp.mvc.utils.RequestUtils;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

/**
 * 
 * 获取绝对路径(相对于当前应用根的路径).
 * 
 * 例： 
 * 
 *    如果当前应用上下文为：icity-portal-console,那么<@full_path path="bbb.txt"/>,将输出：/icity-portal-console/file/demo/bbb.txt
 * 
 * @author dylan.chen Mar 2, 2013
 * 
 */
public class FullPathDirective implements TemplateDirectiveModel {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {

		String path = DirectiveUtils.getString("path", params);

		HttpServletRequest request = RequestContextHolder.getRequest();

		String fullPath = RequestUtils.getFullPath(request, path);

		env.getOut().write(fullPath);

	}

}
