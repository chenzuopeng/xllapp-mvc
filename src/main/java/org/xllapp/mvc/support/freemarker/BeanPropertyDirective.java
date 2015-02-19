package org.xllapp.mvc.support.freemarker;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.xllapp.mvc.utils.BeanUtils;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

/**
 * 获取对象属性值.
 * 
 * 基于Spring的BeanWrapper实现.
 * 
 * @author dylan.chen Mar 18, 2014
 * 
 */
public class BeanPropertyDirective implements TemplateDirectiveModel {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
		Object bean = DirectiveUtils.getObject("bean", params);
		String propertyName = DirectiveUtils.getString("propertyName", params);
		if (null != bean && StringUtils.isNotBlank(propertyName)) {
			String value = BeanUtils.getPropertyValueAsString(bean, propertyName);
			env.getOut().write(value);
		}
	}

}
