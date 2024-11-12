package com.stephen.trajectory.model.dto.post;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 更新请求
 *
 * @author stephen qiu
 */
@Data
public class PostUpdateRequest implements Serializable {
	
	/**
	 * id
	 */
	private Long id;
	
	/**
	 * 标题
	 */
	private String title;
	
	/**
	 * 内容
	 */
	private String content;
	
	
	/**
	 * 封面
	 */
	private String cover;
	
	/**
	 * 标签列表
	 */
	private List<String> tags;
	
	private static final long serialVersionUID = 1L;
}