package com.stephen.trajectory.model.vo;

import cn.hutool.json.JSONUtil;
import com.stephen.trajectory.model.entity.PostComment;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import cn.hutool.core.collection.CollUtil;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 帖子评论视图
 *
 * @author stephen
 */
@Data
public class PostCommentVO implements Serializable {
    
    private static final long serialVersionUID = -1638615686988963726L;
    /**
     * id
     */
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
     * 创建用户信息
     */
    private UserVO userVO;

    /**
     * 封装类转对象
     *
     * @param postCommentVO postCommentVO
     * @return {@link PostComment}
     */
    public static PostComment voToObj(PostCommentVO postCommentVO) {
        if (postCommentVO == null) {
            return null;
        }
        PostComment postComment = new PostComment();
        BeanUtils.copyProperties(postCommentVO, postComment);
        return postComment;
    }

    /**
     * 对象转封装类
     *
     * @param postComment postComment
     * @return {@link PostCommentVO}
     */
    public static PostCommentVO objToVo(PostComment postComment) {
        if (postComment == null) {
            return null;
        }
        PostCommentVO postCommentVO = new PostCommentVO();
        BeanUtils.copyProperties(postComment, postCommentVO);
        return postCommentVO;
    }
}
