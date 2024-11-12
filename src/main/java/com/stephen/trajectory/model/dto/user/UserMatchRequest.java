package com.stephen.trajectory.model.dto.user;

import com.stephen.trajectory.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author: stephen qiu
 * @create: 2024-10-06 16:05
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class UserMatchRequest extends PageRequest implements Serializable {
	
	private static final long serialVersionUID = 1347725051010460959L;
	/**
	 * 一次需要匹配的人数
	 */
	private int number;
}
