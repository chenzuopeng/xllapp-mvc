package org.xllapp.mvc.support.freemarker;

import static org.springframework.web.servlet.view.AbstractTemplateView.SPRING_MACRO_REQUEST_CONTEXT_ATTRIBUTE;

import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.support.RequestContext;

import freemarker.core.Environment;
import freemarker.ext.beans.BeanModel;
import freemarker.template.AdapterTemplateModel;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateNumberModel;
import freemarker.template.TemplateScalarModel;

/**
 * Freemarker标签工具类
 * 
 * @author liufang
 * 
 */
public abstract class DirectiveUtils {

	/**
	 * 非布尔参数异常
	 * 
	 */
	@SuppressWarnings("serial")
	private static class MustBooleanException extends TemplateModelException {
		public MustBooleanException(String paramName) {
			super("The \"" + paramName + "\" parameter must be a boolean.");
		}
	}

	/**
	 * 非布尔参数异常
	 * 
	 */
	@SuppressWarnings("serial")
	private static class MustDateException extends TemplateModelException {
		public MustDateException(String paramName) {
			super("The \"" + paramName + "\" parameter must be a date.");
		}
	}

	/**
	 * 非数字参数异常
	 * 
	 */
	@SuppressWarnings("serial")
	private static class MustNumberException extends TemplateModelException {
		public MustNumberException(String paramName) {
			super("The \"" + paramName + "\" parameter must be a number.");
		}
	}

	/**
	 * 非数字参数异常
	 * 
	 */
	@SuppressWarnings("serial")
	private static class MustSplitNumberException extends TemplateModelException {

		@SuppressWarnings("unused")
		public MustSplitNumberException(String paramName) {
			super("The \"" + paramName + "\" parameter must be a number split by ','");
		}

		public MustSplitNumberException(String paramName, Exception cause) {
			super("The \"" + paramName + "\" parameter must be a number split by ','", cause);
		}
	}

	/**
	 * 非数字参数异常
	 * 
	 */
	@SuppressWarnings("serial")
	private static class MustStringException extends TemplateModelException {
		public MustStringException(String paramName) {
			super("The \"" + paramName + "\" parameter must be a string.");
		}
	}
	
	/**
	 * 非法的对象参数异常
	 * 
	 */
	@SuppressWarnings("serial")
	private static class MustObjectException extends TemplateModelException {
		public MustObjectException(String paramName) {
			super("The \"" + paramName + "\" parameter must be a object.");
		}
	}

	/**
	 * 获得RequestContext
	 * 
	 * ViewResolver中的exposeSpringMacroHelpers必须为true
	 * 
	 * @param env
	 * @return
	 * @throws TemplateException
	 */
	public static RequestContext getContext(Environment env) throws TemplateException {
		TemplateModel ctx = env.getGlobalVariable(SPRING_MACRO_REQUEST_CONTEXT_ATTRIBUTE);
		if (ctx instanceof AdapterTemplateModel) {
			return (RequestContext) ((AdapterTemplateModel) ctx).getAdaptedObject(RequestContext.class);
		} else {
			throw new TemplateModelException("RequestContext '" + SPRING_MACRO_REQUEST_CONTEXT_ATTRIBUTE + "' not found in DataModel.");
		}
	}

	public static String getString(String name, Map<String, TemplateModel> params) throws TemplateException {
		TemplateModel model = params.get(name);
		if (model == null) {
			return null;
		}
		if (model instanceof TemplateScalarModel) {
			return ((TemplateScalarModel) model).getAsString();
		} else if (model instanceof TemplateNumberModel) {
			return ((TemplateNumberModel) model).getAsNumber().toString();
		} else {
			throw new MustStringException(name);
		}
	}
	
	public static String getString(String name,String defValue,Map<String, TemplateModel> params) throws TemplateException {
		String value=getString(name, params);
		if(null==value){
			return defValue;
		}
		return null==value?defValue:value;
	}

	public static Long getLong(String name, Map<String, TemplateModel> params) throws TemplateException {
		TemplateModel model = params.get(name);
		if (model == null) {
			return null;
		}
		if (model instanceof TemplateScalarModel) {
			String s = ((TemplateScalarModel) model).getAsString();
			if (StringUtils.isBlank(s)) {
				return null;
			}
			try {
				return Long.parseLong(s);
			} catch (NumberFormatException e) {
				throw new MustNumberException(name);
			}
		} else if (model instanceof TemplateNumberModel) {
			return ((TemplateNumberModel) model).getAsNumber().longValue();
		} else {
			throw new MustNumberException(name);
		}
	}

	public static Integer getInt(String name, Map<String, TemplateModel> params) throws TemplateException {
		TemplateModel model = params.get(name);
		if (model == null) {
			return null;
		}
		if (model instanceof TemplateScalarModel) {
			String s = ((TemplateScalarModel) model).getAsString();
			if (StringUtils.isBlank(s)) {
				return null;
			}
			try {
				return Integer.parseInt(s);
			} catch (NumberFormatException e) {
				throw new MustNumberException(name);
			}
		} else if (model instanceof TemplateNumberModel) {
			return ((TemplateNumberModel) model).getAsNumber().intValue();
		} else {
			throw new MustNumberException(name);
		}
	}

	public static Integer[] getIntArray(String name, Map<String, TemplateModel> params) throws TemplateException {
		String str = DirectiveUtils.getString(name, params);
		if (StringUtils.isBlank(str)) {
			return null;
		}
		String[] arr = StringUtils.split(str, ',');
		Integer[] ids = new Integer[arr.length];
		int i = 0;
		try {
			for (String s : arr) {
				ids[i++] = Integer.valueOf(s);
			}
			return ids;
		} catch (NumberFormatException e) {
			throw new MustSplitNumberException(name, e);
		}
	}

	public static Boolean getBool(String name, Map<String, TemplateModel> params) throws TemplateException {
		TemplateModel model = params.get(name);
		if (model == null) {
			return null;
		}
		if (model instanceof TemplateBooleanModel) {
			return ((TemplateBooleanModel) model).getAsBoolean();
		} else if (model instanceof TemplateNumberModel) {
			return !(((TemplateNumberModel) model).getAsNumber().intValue() == 0);
		} else if (model instanceof TemplateScalarModel) {
			String s = ((TemplateScalarModel) model).getAsString();
			// 空串应该返回null还是true呢？
			if (!StringUtils.isBlank(s)) {
				return !(s.equals("0") || s.equalsIgnoreCase("false") || s.equalsIgnoreCase("f"));
			} else {
				return null;
			}
		} else {
			throw new MustBooleanException(name);
		}
	}
	
	public static Boolean getBool(String name,boolean defValue, Map<String, TemplateModel> params) throws TemplateException{
		Boolean b=getBool(name, params);
	    return b==null?defValue:b;
	}
	

	public static Date getDate(String name, Map<String, TemplateModel> params) throws TemplateException {
		TemplateModel model = params.get(name);
		if (model == null) {
			return null;
		}
		if (model instanceof TemplateDateModel) {
			return ((TemplateDateModel) model).getAsDate();
		} else if (model instanceof TemplateScalarModel) {
			DateTypeEditor editor = new DateTypeEditor();
			editor.setAsText(((TemplateScalarModel) model).getAsString());
			return (Date) editor.getValue();
		} else {
			throw new MustDateException(name);
		}
	}
	
	public static Object getObject(String name, Map<String, TemplateModel> params) throws TemplateException{
		TemplateModel model = params.get(name);
		if (model instanceof BeanModel) {
			return ((BeanModel) model).getWrappedObject();
		}else{
			throw new MustObjectException(name);
		}
	}

}
