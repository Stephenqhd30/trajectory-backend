package com.stephen.trajectory.model.dto.postComment;

import com.stephen.trajectory.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询帖子评论请求
 *
 * @author stephen qiu
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PostCommentQueryRequest extends PageRequest implements Serializable {
	
	/**
	 * id
	 */
	private Long id;
	
	/**
	 * id
	 */
	private Long notId;
	
	/**
	 * 搜索词
	 */
	private String searchText;
	
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