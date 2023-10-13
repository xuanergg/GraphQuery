package com.kgvp.web.controller;

import com.kgvp.web.base.util.Neo4jUtil;
import com.kgvp.web.base.util.R;
import com.kgvp.web.base.util.StringUtil;
import com.kgvp.web.request.GraphQuery;
import com.kgvp.web.service.KGGraphService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
@Slf4j
@RequestMapping(value = "/")
public class KGController {

    @Autowired
    private KGGraphService kgGraphService;

    @PostMapping(value = "/queryGraphResult")
    public R queryGraphResult(@RequestBody GraphQuery query) {
        try {
            HashMap<String, Object> graphData = kgGraphService.queryGraphResult(query);
            return R.success(graphData);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error(e.getMessage());
        }

    }

    @RequestMapping(value = "/getCypherResult")
    public R getCypherResult(String cypher) {
        try {
            HashMap<String, Object> graphData = Neo4jUtil.getGraphNodeAndShip(cypher);
            return R.success(graphData);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/getRelationNodeCount")
    public R<String> getRelationNodeCount(String domain, long nodeId) {
        try {
            long totalCount = 0;
                totalCount = kgGraphService.getRelationNodeCount(domain, nodeId);
                return R.success(totalCount);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/getMoreRelationNode")
    public R<HashMap<String, Object>> getMoreRelationNode(String domain, String nodeId,String pageSize) {
        try {
                HashMap<String, Object> graphModel = kgGraphService.getMoreRelationNode(domain, nodeId,pageSize);
                if (graphModel != null) {
                    return R.success(graphModel);
                }
        } catch (Exception e) {
            e.printStackTrace();
            return R.error(e.getMessage());
        }
        return R.error("没有更多数据了");
    }


}
