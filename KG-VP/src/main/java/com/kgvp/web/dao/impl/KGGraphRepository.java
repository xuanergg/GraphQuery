package com.kgvp.web.dao.impl;

import com.kgvp.web.base.util.Neo4jUtil;
import com.kgvp.web.base.util.StringUtil;
import com.kgvp.web.dao.KGGraphDao;
import com.kgvp.web.request.GraphQuery;
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
            if (!StringUtil.isBlank(domain)) {
                String cqr = "";
                List<String> lis = new ArrayList<>();
                if (query.getParameters() != null && query.getParameters().size() > 0) {
                    for (Map<String, String> parameter : query.getParameters()) {
                        for (Map.Entry<String, String> stringStringEntry : parameter.entrySet()) {
                            String key = stringStringEntry.getKey();
                            String it = String.format("n."+key+" contains('%s')", parameter.get(key));
                            lis.add(it);
                        }
                    }
                    cqr = String.join(" or ", lis);
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
                    String nodeSql = String.format("MATCH (n:`%s`) <-[r]->(m) %s return * limit %s", domain, cqWhere, query.getPageSize());
                    HashMap<String, Object> graphNode = Neo4jUtil.getGraphNodeAndShip(nodeSql);
                    Object node = graphNode.get("node");
                    if (node != null) {
                        nr.put("node", graphNode.get("node"));
                        nr.put("relationship", graphNode.get("relationship"));
                    } else {
                        String nodecql = String.format("MATCH (n:`%s`) %s RETURN distinct(n) limit %s", domain, nodeOnly, query.getPageSize());
                        List<HashMap<String, Object>> nodeItem = Neo4jUtil.getGraphNode(nodecql);
                        nr.put("node", nodeItem);
                        nr.put("relationship", new ArrayList<HashMap<String, Object>>());
                    }
                } else {
                    String nodeSql = String.format("MATCH (n:`%s`)-[r]-(m) %s RETURN * limit %s", domain, cqWhere, query.getPageSize());
                    nr = Neo4jUtil.getGraphNodeAndShip(nodeSql);
                }
            }
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
            String cypherSql = String.format("MATCH (n) -[r]-(m) where id(n)=%s  return * limit  %s", nodeId,pageSize);
            result = Neo4jUtil.getGraphNodeAndShip(cypherSql);
//            result.put("relationship", new ArrayList<HashMap<String, Object>>());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

}
