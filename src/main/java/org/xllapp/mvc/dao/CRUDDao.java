package org.xllapp.mvc.dao;

import java.util.List;
import java.util.Map;

import org.xllapp.mybatis.Page;

/**
 * Dao基类,声明了CRUD操作的方法.
 * 
 * @author dylan.chen Jun 3, 2012
 * 
 */
public interface CRUDDao<T> {

	T get(Long id);

	void insert(T t);

	void update(T t);

	void delete(Long id);

	void deletes(Long[] ids);

	List<T> query(Map<String, Object> parameters,Page<T> page);

}
