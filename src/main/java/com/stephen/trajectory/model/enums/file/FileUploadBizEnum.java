package com.stephen.trajectory.model.enums.file;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件上传业务类型枚举
 *
 * @author stephen qiu
 */
@Getter
@AllArgsConstructor
public enum FileUploadBizEnum {
	
	USER_AVATAR("用户头像", "user_avatar"),
	POST_COVER("帖子封面", "post_cover"),
	POST_IMAGE_COVER("帖子上传图片", "post_image_cover"),
	GENERATE_EXCEL("AI分析上传的Excel文件", "generate_excel");
	
	private final String text;
	
	private final String value;
	
	/**
	 * 获取值列表
	 *
	 * @return {@link List<String>}
	 */
	public static List<String> getValues() {
		return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
	}
	
	/**
	 * 根据 value 获取枚举
	 *
	 * @param value value
	 * @return {@link FileUploadBizEnum}
	 */
	public static FileUploadBizEnum getEnumByValue(String value) {
		if (ObjectUtils.isEmpty(value)) {
			return null;
		}
		for (FileUploadBizEnum anEnum : FileUploadBizEnum.values()) {
			if (anEnum.value.equals(value)) {
				return anEnum;
			}
		}
		return null;
	}
	
}
