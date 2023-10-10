package com.kgvp.web.service;

import com.kgvp.web.request.GraphQuery;

import java.util.HashMap;

public interface KGGraphService {

	/**
	 * 查询图谱节点和关系
	 *
	 * @param query
	 * @return node relationship
	 */
	HashMap<String, Object> queryGraphResult(GraphQuery query);

	/**
	 * 获取某个领域指定节点拥有的上下级的节点数
	 *
	 * @param domain
	 * @param nodeId
	 * @return long 数值
	 */
	long getRelationNodeCount(String domain, long nodeId);

	/**
	 * 获取/展开更多节点,找到和该节点有关系的节点
	 *
	 * @param domain
	 * @param nodeId
	 * @param pageSize
	 * @return
	 */
	HashMap<String, Object> getMoreRelationNode(String domain, String nodeId, String pageSize);


}
