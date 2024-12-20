package com.stephen.trajectory.model.dto.postComment;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

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