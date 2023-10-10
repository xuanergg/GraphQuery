package com.kgvp.web.service.impl;

import com.kgvp.web.dao.impl.KGGraphRepository;
import com.kgvp.web.request.GraphQuery;
import com.kgvp.web.service.KGGraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class KGGraphServiceImpl implements KGGraphService {

    @Autowired
    private KGGraphRepository kgRepository;

    @Override
    public HashMap<String, Object> queryGraphResult(GraphQuery query) {
        return kgRepository.queryGraphResult(query);
    }

    @Override
    public long getRelationNodeCount(String domain, long nodeId) {
        return kgRepository.getRelationNodeCount(domain, nodeId);
    }
    @Override
    public HashMap<String, Object> getMoreRelationNode(String domain, String nodeId, String pageSize) {
        return kgRepository.getMoreRelationNode(domain, nodeId,pageSize);
    }

}
