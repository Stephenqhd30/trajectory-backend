package com.stephen.trajectory.model.dto.postThumb;

import com.stephen.trajectory.common.PageRequest;
import com.stephen.trajectory.model.dto.post.PostQueryRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 帖子点赞查询请求
 *
 * @author stephen qiu
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PostThumbQueryRequest extends PageRequest implements Serializable {
	
	/**
	 * 帖子查询请求
	 */
	private PostQueryRequest postQueryRequest;
	
	/**
	 * 用户 id
	 */
	private Long userId;
	
	private static final long serialVersionUID = 1L;
}