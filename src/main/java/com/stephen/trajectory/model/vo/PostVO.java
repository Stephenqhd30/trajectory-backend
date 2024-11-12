package com.stephen.trajectory.model.vo;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.stephen.trajectory.model.entity.Post;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 帖子视图
 *
 * @author stephen qiu
 */
@Data
public class PostVO implements Serializable {
	
	private static final long serialVersionUID = -8987057543789654391L;
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
	 * 点赞数
	 */
	private Integer thumbNum;
	
	/**
	 * 收藏数
	 */
	private Integer favourNum;
	
	/**
	 * 创建用户 id
	 */
	private Long userId;
	
	/**
	 * 创建时间
	 */
	private Date createTime;
	
	/**
	 * 更新时间
	 */
	private Date updateTime;
	
	/**
	 * 标签列表
	 */
	private List<String> tags;
	
	/**
	 * 创建人信息
	 */
	private UserVO userVO;
	
	/**
	 * 是否已点赞
	 */
	private Boolean hasThumb;
	
	/**
	 * 是否已收藏
	 */
	private Boolean hasFavour;
	
	/**
	 * 包装类转对象
	 *
	 * @param postVO postVO
	 * @return {@link Post}
	 */
	public static Post voToObj(PostVO postVO) {
		if (postVO == null) {
			return null;
		}
		Post post = new Post();
		BeanUtils.copyProperties(postVO, post);
		List<String> tagList = postVO.getTags();
		if (CollUtil.isNotEmpty(tagList)) {
			post.setTags(JSONUtil.toJsonStr(tagList));
		}
		return post;
	}
	
	/**
	 * 对象转包装类
	 *
	 * @param post post
	 * @return {@link PostVO}
	 */
	public static PostVO objToVo(Post post) {
		if (post == null) {
			return null;
		}
		PostVO postVO = new PostVO();
		BeanUtils.copyProperties(post, postVO);
		if (StringUtils.isNotBlank(post.getTags())) {
			postVO.setTags(JSONUtil.toList(post.getTags(), String.class));
		}
		return postVO;
	}
}
