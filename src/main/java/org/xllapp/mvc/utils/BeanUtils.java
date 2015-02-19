package org.xllapp.mvc.utils;

import java.util.Date;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

/**
 * 此类提供对bean进行操作的便捷方法.
 * 
 * @author dylan.chen Mar 19, 2014
 * 
 */
public abstract class BeanUtils {

	/**
	 * 获取对象指定属性的String形式的值.
	 */
	public static String getPropertyValueAsString(Object bean, String propertyName) {
		if (null == bean) {
			return null;
		}
		BeanWrapper beanWrapper = new BeanWrapperImpl(bean);
		Object value = beanWrapper.getPropertyValue(propertyName);
		if (null == value) {
			return "";
		} else if (value instanceof Date) {
			return DateFormatUtils.format((Date) value, "yyyy-MM-dd HH:mm:ss");
		} else {
			return value.toString();
		}
	}

}
