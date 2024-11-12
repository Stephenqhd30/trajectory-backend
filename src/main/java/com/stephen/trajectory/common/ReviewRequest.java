package com.stephen.trajectory.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: stephen qiu
 * @create: 2024-08-09 14:44
 **/
@Data
public class ReviewRequest implements Serializable {
	
	private static final long serialVersionUID = 1973973718590531783L;
	/**
	 * id
	 */
	private Long id;
	
	
	/**
	 * 审核状态 0-待审核 1-通过 2-拒绝
	 */
	private Integer reviewStatus;
	
	
	/**
	 * 审核信息
	 */
	private String reviewMessage;
	
	/**
	 * id列表
	 */
	private Long idList;
}
