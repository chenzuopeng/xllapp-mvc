package org.xllapp.mvc.entity;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 实体类的基类.
 *
 * @author dylan.chen Jun 3, 2012
 * 
 */
public abstract class IdEntity implements Serializable{
	
	private static final long serialVersionUID = -2010767870013125046L;
	
	protected Long id;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
}
