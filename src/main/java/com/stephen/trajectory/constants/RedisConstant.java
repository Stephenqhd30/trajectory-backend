package com.stephen.trajectory.constants;

/**
 * @author: stephen qiu
 * @create: 2024-09-23 13:49
 **/
public interface RedisConstant {
	
	/**
	 * Redis key 文件上传路径前缀
	 */
	String FILE_NAME = "stephen:trajectory:";
	
	/**
	 * Redis key 标签树
	 */
	String TAG_TREE_KEY = "tag_tree";
	
	/**
	 * Redis key 匹配用户
	 */
	String MATCH_USER = "match_user";
}
