package org.xllapp.mvc.support.freemarker;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.InitializingBean;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.TemplateHashModel;

/**
 * 此类用于注册需要在模板文件中使用到的静态方法.
 * 
 * @author dylan.chen Mar 19, 2014
 * 
 */
public class StaticModels extends Properties implements InitializingBean {

	private static final long serialVersionUID = 2435323422889055260L;

	private Map<String, String> classes;

	public void setClasses(Map<String, String> classes) {
		this.classes = classes;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (MapUtils.isNotEmpty(classes)) {
			Set<String> keys = classes.keySet();
			for (String key : keys) {
				BeansWrapper wrapper = BeansWrapper.getDefaultInstance();
				TemplateHashModel staticModels = wrapper.getStaticModels();
				TemplateHashModel fileStatics = (TemplateHashModel) staticModels.get(classes.get(key));
				put(key, fileStatics);
			}
		}

	}

}
