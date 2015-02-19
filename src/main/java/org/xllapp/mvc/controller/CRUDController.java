package org.xllapp.mvc.controller;

import static org.xllapp.mvc.utils.RequestUtils.extractObject;
import static org.xllapp.mvc.utils.RequestUtils.getRequestParam;
import static org.xllapp.mvc.utils.RequestUtils.resolveParams;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.xllapp.mvc.dao.CRUDDao;
import org.xllapp.mvc.entity.IdEntity;

import org.xllapp.mybatis.Page;

/**
 * 此类提供了对实体对象进行增删改查操作的封装.
 * 
 * @author dylan.chen Mar 14, 2014
 * 
 */
public abstract class CRUDController<T extends IdEntity> {

	private static final Logger logger = LoggerFactory.getLogger(CRUDController.class);

	public final static int DEFAULT_PAGE_NO = 1;

	public final static int DEFAULT_PAGE_SIZE = 10;

	public enum Act {
		DELETE, SAVE, QUERY, SHOW_FORM
	}

	@RequestMapping(method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView handleRequest(HttpServletRequest req, HttpServletResponse resp) throws Exception {

		ModelAndView mv = new ModelAndView();
		Object view = null;
		Act act = null;

		try {
			String origAct = getRequestParam(req, "act", Act.QUERY.name());
			act = Act.valueOf(origAct.toUpperCase());
		} catch (Exception e) {
			throw new Exception("invalid act[" + act + "]");
		}

		switch (act) {
		case SAVE: {
			view = doSave(req, resp, mv);
			break;
		}
		case DELETE: {
			view = doDelete(req, resp, mv);
			break;
		}
		case SHOW_FORM: {
			view = showForm(req, resp, mv);
			break;
		}
		case QUERY: {
			view = doQuery(req, resp, mv);
			break;
		}
		}

		if (null != view) {
			if (view instanceof String) {
				mv.setViewName((String) view);
			} else if (view instanceof View) {
				mv.setView((View) view);
			} else if (view instanceof ModelAndView){ 
				mv= (ModelAndView) view;
			} else {

				logger.error("invalid view[" + view + "]");

				throw new Exception("invalid view[" + view + "]");
			}
		}

		return mv;
	}
	
	protected Object doSave(HttpServletRequest req, HttpServletResponse resp, ModelAndView model) {
		Object view = null;
		try {
			T entity = extractEntity(req);
			
			verifyEntity(entity, model);

			CRUDDao<T> dao = getDao();
			if (((IdEntity) entity).getId() == null) {
				dao.insert(entity);
			} else {
				dao.update(entity);
			}
			view = getSaveView(true, null, entity, model);
		} catch (Throwable throwable) {

			logger.error("failure to save in request[" + resolveParams(req) + "].caused by:" + throwable.getLocalizedMessage(), throwable);

			view = getSaveView(false, throwable, null, model);
		}
		return view;
	}
	
	/**
	 * 子类覆盖进行实体验证的操作
	 */
	protected void verifyEntity(T entity,ModelAndView model) throws InvalidEntityException {
	}
	
	/**
	 * 子类实现返回实体保存成功后需要展示的视图
	 * @return 视图
	 *   说明:
	 *      可以返回的对象类型:
	 *        String 视图名
	 *        View   视图对象
	 *        ModelAndView  
	 */
	protected abstract Object getSaveView(boolean success, Throwable throwable, T entity, ModelAndView model);

	protected Object doDelete(HttpServletRequest req, HttpServletResponse resp, ModelAndView model) {
		Object view = null;
		try {
			String[] origIds = req.getParameterValues("ids");
			Long[] ids = null;
			if (!ArrayUtils.isEmpty(origIds)) {
				ids = toLongArray(origIds);
				CRUDDao<T> dao = getDao();
				if (ids.length > 1) {
					dao.deletes(ids);
				} else if (ids.length == 1) {
					dao.delete(ids[0]);
				}
			}
			view = getDeleteView(true, null, ids, model);
		} catch (Throwable throwable) {

			logger.error("failure to delete in request[" + resolveParams(req) + "].caused by:" + throwable.getLocalizedMessage(), throwable);

			view = getDeleteView(false, throwable, null, model);
		}
		return view;
	}

	/**
	 * 子类实现返回实体删除成功后需要展示的视图
	 * @return 视图
	 *   说明:
	 *      可以返回的对象类型:
	 *        String 视图名
	 *        View   视图对象
	 *        ModelAndView  
	 */
	protected abstract Object getDeleteView(boolean success, Throwable throwable, Long[] ids, ModelAndView model);

	protected Object showForm(HttpServletRequest req, HttpServletResponse resp, ModelAndView model) {
		Object view = null;
		try {
			T entity = loadEntity(req);
			if (entity != null) {
				model.addObject("entity", entity);
			}
			view = getFormView(true, null, entity, model);
		} catch (Throwable throwable) {

			logger.error("failure to show form in request[" + resolveParams(req) + "].caused by:" + throwable.getLocalizedMessage(), throwable);

			view = getFormView(false, throwable, null, model);
		}
		return view;
	}

	/**
	 * 子类实现返回录入表单页面的视图
	 * @return 视图
	 *   说明:
	 *      可以返回的对象类型:
	 *        String 视图名
	 *        View   视图对象
	 *        ModelAndView  
	 */
	protected abstract Object getFormView(boolean success, Throwable throwable, T entity, ModelAndView model);

	protected Object doQuery(HttpServletRequest req, HttpServletResponse resp, ModelAndView model) {
		Object view = null;
		try {
			Map<String, Object> queryParams = extractQueryParams(req);
			Page<T> page = getPage(queryParams, req);
			model.addObject("page", page);
			view = getQueryView(true, null, queryParams, model);
		} catch (Throwable throwable) {

			logger.error("failure to query in request[" + resolveParams(req) + "].caused by:" + throwable.getLocalizedMessage(), throwable);

			view = getQueryView(false, throwable, null, model);
		}
		return view;
	}

	protected Page<T> getPage(Map<String, Object> params, HttpServletRequest req) {
		int pageNo = extractPageNo(req);
		int pageSize = extractPageSize(req);
		Page<T> page = new Page<T>(pageNo, pageSize);
		CRUDDao<T> dao = getDao();
		dao.query(params, page);
		return page;
	}

	protected int extractPageNo(HttpServletRequest req) {
		return getRequestParam(req, "pageNo", getDefaultPageNo());
	}

	protected int extractPageSize(HttpServletRequest req) {
		return getRequestParam(req, "pageSize", getDefaultPageSize());
	}

	/**
	 * 子类实现返回列表页面的视图
	 * @return 视图
	 *   说明:
	 *      可以返回的对象类型:
	 *        String 视图名
	 *        View   视图对象
	 *        ModelAndView  
	 */
	protected abstract Object getQueryView(boolean success, Throwable throwable, Map<String, Object> queryParams, ModelAndView model);

	protected int getDefaultPageNo() {
		return DEFAULT_PAGE_NO;
	}

	protected int getDefaultPageSize() {
		return DEFAULT_PAGE_SIZE;
	}

	protected Long[] toLongArray(String[] array) {
		Long[] result = new Long[array.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = Long.valueOf(array[i]);
		}
		return result;
	}

	protected Long extractEntityId(HttpServletRequest req) {
		String origId = req.getParameter("id");
		if (StringUtils.isNotBlank(origId)) {
			return Long.valueOf(origId);
		}
		return null;
	}

	protected Map<String, Object> extractQueryParams(HttpServletRequest req) {
		Map<String, Object> params = resolveParams(req);
		logger.debug("extract param[{}]", params);
		return params;
	}

	protected T extractEntity(HttpServletRequest req) {

		Class<?> clazz = getEntityType();

		logger.debug("entity type: {}", clazz);

		T entity = extractObject(clazz, req);

		logger.debug("extract entity[{}]", entity);

		return entity;
	}

	/**
	 * 获取泛型的类型
	 */
	protected Class<?> getEntityType() {
		Type genType = this.getClass().getGenericSuperclass();
		Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
		return (Class<?>) params[0];
	}

	protected T loadEntity(HttpServletRequest req) {
		CRUDDao<T> dao = getDao();
		Long id = extractEntityId(req);
		if (null != id) {
			return dao.get(id);
		}
		return null;
	}

	/**
	 * 子类实现获取dao对象
	 */
	protected abstract CRUDDao<T> getDao();

	public static class InvalidEntityException extends Exception{

		private static final long serialVersionUID = -3928337037486979669L;

		public InvalidEntityException() {
			super();
		}

		public InvalidEntityException(String message, Throwable cause) {
			super(message, cause);
		}

		public InvalidEntityException(String message) {
			super(message);
		}

		public InvalidEntityException(Throwable cause) {
			super(cause);
		}
		
	}
	
}
