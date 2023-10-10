package com.kgvp.web.dao.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kgvp.web.base.util.Neo4jUtil;
import com.kgvp.web.base.util.StringUtil;
import com.kgvp.web.dao.KGGraphDao;
import com.kgvp.web.request.GraphQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class KGGraphRepository implements KGGraphDao {


    @Override
    public HashMap<String, Object> queryGraphResult(GraphQuery query) {
        HashMap<String, Object> nr = new HashMap<>();
        try {
            String domain = query.getDomain();
            String cqr = "";
            List<String> lis = new ArrayList<>();
            if (query.getParameters() != null && query.getParameters().size() > 0) {
                for (Map<String, String> parameter : query.getParameters()) {
                    JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(parameter));
                    String it = String.format("n." + jsonObject.get("key") + " contains('%s')", jsonObject.get("value"));
                    lis.add(it);
                }
                cqr = String.join(" and ", lis);
            }
            String cqWhere = "";
            if (!StringUtil.isBlank(query.getNodeName()) || !StringUtil.isBlank(cqr)) {
                if (!StringUtil.isBlank(query.getNodeName())) {
                    if (query.getMatchType() == 1) {
                        cqWhere = String.format("where n.Name ='%s' ", query.getNodeName());
                    } else {
                        cqWhere = String.format("where n.Name contains('%s')", query.getNodeName());
                    }
                }
                String nodeOnly = cqWhere;
                if (!StringUtil.isBlank(cqr)) {
                    if (StringUtil.isBlank(cqWhere)) {
                        cqWhere = String.format(" where ( %s )", cqr);
                    } else {
                        cqWhere += String.format(" and ( %s )", cqr);
                    }
                }
                String nodeSql;
                if (StringUtils.isNotBlank(domain)) {
                    nodeSql =String.format("MATCH (n:`%s`) <-[r]->(m) %s return * limit %s", domain, cqWhere, query.getPageSize());
                } else {
                    nodeSql = String.format("MATCH (n) <-[r]->(m) %s return * limit %s", cqWhere, query.getPageSize());
                }
                HashMap<String, Object> graphNode = Neo4jUtil.getGraphNodeAndShip(nodeSql);
                Object node = graphNode.get("node");
                if (node != null) {
                    nr.put("node", graphNode.get("node"));
                    nr.put("relationship", graphNode.get("relationship"));
                    return nr;
                }
            }
            String nodeSql;
            if (StringUtils.isNotBlank(domain)) {
                nodeSql = String.format("MATCH (n:`%s`)-[r]-(m) %s RETURN * limit %s", domain, cqWhere, query.getPageSize());
            } else {
                nodeSql = String.format("MATCH (n)-[r]-(m) %s RETURN * limit %s", cqWhere, query.getPageSize());
            }
            nr = Neo4jUtil.getGraphNodeAndShip(nodeSql);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nr;
    }


    @Override
    public long getRelationNodeCount(String domain, long nodeId) {
        long totalCount = 0;
        try {
            if (!StringUtil.isBlank(domain)) {
                String nodeSql = String.format("MATCH (n:`%s`) <-[r]->(m)  where id(n)=%s return count(m)", domain, nodeId);
                totalCount = Neo4jUtil.getGraphValue(nodeSql);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return totalCount;
    }

    @Override
    public HashMap<String, Object> getMoreRelationNode(String domain, String nodeId, String pageSize) {
        HashMap<String, Object> result = new HashMap<>();

        try {
            String cypherSql = String.format("MATCH (n) -[r]-(m) where id(n)=%s  return * limit  %s", nodeId, pageSize);
            result = Neo4jUtil.getGraphNodeAndShip(cypherSql);
//            result.put("relationship", new ArrayList<HashMap<String, Object>>());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
