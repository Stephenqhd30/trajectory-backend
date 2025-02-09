package com.stephen.trajectory.model.dto.postComment;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建帖子评论请求
 *
 * @author stephen qiu
 */
@Data
public class PostCommentAddRequest implements Serializable {
	
	/**
	 * 帖子id
	 */
	private Long postId;
	
	/**
	 * 根评论id
	 */
	private Long rootId;
	
	/**
	 * 评论内容
	 */
	private String content;
	
	/**
	 * 评论人id
	 */
	private Long userId;
	
	/**
	 * 被评论人id
	 */
	private Long toUid;
	
	/**
	 * 被评论的评论id
	 */
	private Long toCommentId;
	
	private static final long serialVersionUID = 1L;
}