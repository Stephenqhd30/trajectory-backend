package com.stephen.trajectory.model.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户信息Excel导出VO
 *
 * @author: stephen qiu
 * @create: 2024-09-26 14:04
 **/
@Data
public class UserExcelVO implements Serializable {
	private static final long serialVersionUID = -4002634298767485839L;
	/**
	 * id
	 */
	@ColumnWidth(25)
	@ExcelProperty("id")
	private String id;
	
	/**
	 * 用户账号
	 */
	@ColumnWidth(20)
	@ExcelProperty(value = "用户账号")
	private String userAccount;
	
	/**
	 * 用户密码
	 */
	@ColumnWidth(40)
	@ExcelProperty(value = "用户密码")
	private String userPassword;
	
	/**
	 * 开放平台id
	 */
	@ColumnWidth(20)
	@ExcelProperty(value = "开放平台id")
	private String unionId;
	
	/**
	 * 公众号openId
	 */
	@ColumnWidth(20)
	@ExcelProperty(value = "公众号openId")
	private String mpOpenId;
	
	/**
	 * 用户昵称
	 */
	@ColumnWidth(20)
	@ExcelProperty(value = "用户昵称")
	private String userName;
	
	/**
	 * 用户头像
	 */
	@ColumnWidth(30)
	@ExcelProperty(value = "用户头像")
	private String userAvatar;
	
	/**
	 * 性别（0-男，1-女，2-保密）
	 */
	@ColumnWidth(30)
	@ExcelProperty("性别（0-男，1-女，2-保密）")
	private String userGender;
	
	/**
	 * 用户简介
	 */
	@ExcelProperty("用户简介")
	@ColumnWidth(20)
	private String userProfile;
	
	/**
	 * 用户角色：user/admin/ban
	 */
	@ExcelProperty("用户角色：user/admin/ban")
	@ColumnWidth(30)
	private String userRole;
	
	/**
	 * 用户邮箱
	 */
	@ExcelProperty("用户邮箱")
	@ColumnWidth(20)
	private String userEmail;
	
	/**
	 * 手机号码
	 */
	@ExcelProperty("手机号码")
	@ColumnWidth(20)
	private String userPhone;
	
}
