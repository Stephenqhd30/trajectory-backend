package com.stephen.trajectory.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 帖子评论
 *
 * @author stephen qiu
 * @TableName post_comment
 */
@TableName(value = "post_comment")
@Data
public class PostComment implements Serializable {
	/**
	 * id
	 */
	@TableId(type = IdType.ASSIGN_ID)
	private Long id;
	
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
	
	/**
	 * 点赞数
	 */
	private Integer thumbCount;
	
	/**
	 * 创建时间
	 */
	private Date createTime;
	
	/**
	 * 是否删除
	 */
	@TableLogic
	private Integer isDelete;
	
	@TableField(exist = false)
	private static final long serialVersionUID = 1L;
}