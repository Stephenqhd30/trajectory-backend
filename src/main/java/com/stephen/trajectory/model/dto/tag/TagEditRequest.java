package com.stephen.trajectory.model.dto.tag;

import lombok.Data;

import java.io.Serializable;

/**
 * 编辑标签请求
 *
 * @author stephen qiu
 */
@Data
public class TagEditRequest implements Serializable {
	
	/**
	 * id
	 */
	private Long id;
	
	/**
	 * 标签名称
	 */
	private String tagName;
	
	
	private static final long serialVersionUID = 1L;
}