package com.kgvp.web.dao;


import com.kgvp.web.request.GraphQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;


@Mapper
public interface KGGraphDao {
	HashMap<String, Object> queryGraphResult(GraphQuery query);

	long getRelationNodeCount(String domain, long nodeId);

	HashMap<String, Object> getMoreRelationNode(String domain, String nodeId, String pageSize);

}
