package com.cit.logistica.dto;

import java.io.Serializable;

/**
 * Dto base.
 * 
 * @author Romilson
 * 
 */
public abstract class BaseDto<K extends Number> implements Serializable {

	private static final long serialVersionUID = 1L;

	private K id;

	public K getId() {
		return id;
	}
	
	public void setId(K id) {
		this.id = id;
	}
	
}
