package com.stephen.trajectory.elasticsearch.modal.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 搜索结果VO
 *
 * @param <T> 数据类型
 * @author: stephen qiu
 **/
@Data
public class SearchVO<T> implements Serializable {
	private static final long serialVersionUID = 9065946273183024389L;
	
	/**
	 * 分页数据源对象集合
	 */
	private List<T> dataList;
	
	/**
	 * 总数
	 */
	private Long total;
}
