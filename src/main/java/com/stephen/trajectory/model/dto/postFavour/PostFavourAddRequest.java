package com.stephen.trajectory.model.dto.postFavour;

import lombok.Data;

import java.io.Serializable;

/**
 * 帖子收藏 / 取消收藏请求
 *
 * @author stephen qiu
 */
@Data
public class PostFavourAddRequest implements Serializable {
	
	/**
	 * 帖子 id
	 */
	private Long postId;
	
	private static final long serialVersionUID = 1L;
}