package com.stephen.trajectory.model.dto.tag;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * TagDTO 封装类
 *
 * @author: stephen qiu
 * @create: 2024-09-22 20:22
 **/
@Data
public class TagDTO {
	
	/**
	 * 标签 id
	 */
	private Long id;
	/**
	 * 标签名称
	 */
	private String tagName;
	/**
	 * 标签子节点
	 */
	private List<TagChildren> children;
	
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class TagChildren {
		/**
		 * 标签子节点 id
		 */
		private Long id;
		/**
		 * 标签名称
		 */
		private String tagName;
		
		/**
		 * 标签子节点子节点
		 */
		private List<TagChildren> children;
	}
}
