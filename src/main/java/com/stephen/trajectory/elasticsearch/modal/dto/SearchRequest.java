package com.stephen.trajectory.elasticsearch.modal.dto;

import com.stephen.trajectory.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询请求
 *
 * @author stephen qiu
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SearchRequest extends PageRequest implements Serializable {
	
	
	private static final long serialVersionUID = 8341366765860156611L;
	/**
	 * 搜索词
	 */
	private String searchText;
	
	/**
	 * 分类
	 */
	private String type;
	
}