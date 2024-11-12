package com.stephen.trajectory.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 文件上传日志记录表
 *
 * @author stephen qiu
 * @TableName log_files
 */
@TableName(value = "log_files")
@Data
public class LogFiles implements Serializable {
	/**
	 * id
	 */
	@TableId(type = IdType.ASSIGN_ID)
	private Long id;
	
	/**
	 * 文件唯一摘要值
	 */
	private String fileKey;
	
	/**
	 * 文件存储名称
	 */
	private String fileName;
	
	/**
	 * 文件原名称
	 */
	private String fileOriginalName;
	
	/**
	 * 文件扩展名
	 */
	private String fileSuffix;
	
	/**
	 * 文件大小
	 */
	private Long fileSize;
	
	/**
	 * 文件地址
	 */
	private String fileUrl;
	
	/**
	 * 文件OSS类型
	 */
	private String fileOssType;
	
	/**
	 * 创建时间
	 */
	private Date createTime;
	
	@TableField(exist = false)
	private static final long serialVersionUID = 1L;
}