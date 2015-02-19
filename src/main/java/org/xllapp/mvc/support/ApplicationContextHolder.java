package org.xllapp.mvc.support;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * 此类保存全局的ApplicationContext对象实例.
 *
 * @author dylan.chen Aug 20, 2014
 * 
 */
@Lazy(false)
@Component
public class ApplicationContextHolder implements ApplicationContextAware{

	private static ApplicationContext applicationContext;

	public static ApplicationContext getApplicationContext() {
		return ApplicationContextHolder.applicationContext;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		ApplicationContextHolder.applicationContext=applicationContext;		
	}
	
}
