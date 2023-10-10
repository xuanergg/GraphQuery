package com.kgvp.web.request;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GraphQuery{

	private int domainId;
	private Integer type;
	private Integer commend;
	private String domain;
	private String nodeName;
	private String[] relation;
	private List<Map<String,String>> parameters;
	private int matchType;
    private int pageSize = 10;
    private int pageIndex = 1;

}
