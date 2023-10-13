package com.kgvp.web.base.util;

import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import org.neo4j.driver.types.Relationship;
import org.neo4j.driver.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Lazy(false)
public class Neo4jUtil implements AutoCloseable {

    private static Driver neo4jDriver;

    private static final Logger log = LoggerFactory.getLogger(Neo4jUtil.class);

    @Autowired
    @Lazy
    public void setNeo4jDriver(Driver neo4jDriver) {
        Neo4jUtil.neo4jDriver = neo4jDriver;
    }


    public static boolean isNeo4jOpen() {
        try (Session session = neo4jDriver.session()) {
            log.debug("连接成功：" + session.isOpen());
            return session.isOpen();
        }
    }

    public static void runCypherSql(String cypherSql) {
        try (Session session = neo4jDriver.session()) {
            log.debug(cypherSql);
            session.run(cypherSql);
        }
    }

    public <T> List<T> readCyphers(String cypherSql, Function<Record, T> mapper) {
        try (Session session = neo4jDriver.session()) {
            log.debug(cypherSql);
            Result result = session.run(cypherSql);
            return result.list(mapper);
        }
    }

    public static List<HashMap<String, Object>> getGraphNode(String cypherSql) {
        List<HashMap<String, Object>> ents = new ArrayList<HashMap<String, Object>>();
        try (Session session = neo4jDriver.session()) {
            log.debug(cypherSql);
            Result result = session.run(cypherSql);
            if (result.hasNext()) {
                List<Record> records = result.list();
                for (Record recordItem : records) {
                    List<Pair<String, Value>> f = recordItem.fields();
                    for (Pair<String, Value> pair : f) {
                        HashMap<String, Object> rss = new HashMap<String, Object>();
                        String typeName = pair.value().type().name();
                        if (typeName.equals("NODE")) {
                            Node noe4jNode = pair.value().asNode();
                            String uuid = String.valueOf(noe4jNode.id());
                            Map<String, Object> map = noe4jNode.asMap();
                            for (Entry<String, Object> entry : map.entrySet()) {
                                String key = entry.getKey();
                                rss.put(key, entry.getValue());
                            }
                            rss.put("uuid", uuid);
                            ents.add(rss);
                        }
                    }

                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return ents;
    }


    public static List<HashMap<String, Object>> getGraphIndex() {
        List<HashMap<String, Object>> ents = new ArrayList<HashMap<String, Object>>();
        try (Session session = neo4jDriver.session()) {
            String cypherSql="call db.indexes";
            Result result = session.run(cypherSql);
            if (result.hasNext()) {
                List<Record> records = result.list();
                for (Record recordItem : records) {
                    List<Pair<String, Value>> f = recordItem.fields();
                    HashMap<String, Object> rss = new HashMap<String, Object>();
                    for (Pair<String, Value> pair : f) {
                        String key = pair.key();
                        Value value = pair.value();
                        if(key.equalsIgnoreCase("labelsOrTypes")){
                            String objects = value.asList().stream().map(n->n.toString()).collect(Collectors.joining(","));
                            rss.put(key, objects);
                        }else{
                            rss.put(key, value);
                        }
                    }
                    ents.add(rss);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return ents;
    }
    public static List<HashMap<String, Object>> getGraphLabels() {
        List<HashMap<String, Object>> ents = new ArrayList<HashMap<String, Object>>();
        try (Session session = neo4jDriver.session()) {
            String cypherSql="call db.labels";
            Result result = session.run(cypherSql);
            if (result.hasNext()) {
                List<Record> records = result.list();
                for (Record recordItem : records) {
                    List<Pair<String, Value>> f = recordItem.fields();
                    HashMap<String, Object> rss = new HashMap<String, Object>();
                    for (Pair<String, Value> pair : f) {
                        String key = pair.key();
                        Value value = pair.value();
                        if(key.equalsIgnoreCase("label")){
                            String objects =value.toString().replace("\"","");
                            rss.put(key, objects);
                        }else{
                            rss.put(key, value);
                        }
                    }
                    ents.add(rss);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return ents;
    }
    public static  Map<String,Object> getLabelsInfo() {
        Map<String,Object> ent = new HashMap<>();
        try (Session session = neo4jDriver.session()) {
            String cypherSql="CALL apoc.meta.stats() YIELD labels RETURN labels";
            Result result = session.run(cypherSql);
            if (result.hasNext()) {
                Record record = result.single();
                Map<String, Object> mp = record.asMap();
                ent = (Map<String, Object>) mp.get("labels");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return ent;
    }

    public static void deleteIndex(String label) {
        try (Session session = neo4jDriver.session()) {
            String cypherSql=String.format("DROP INDEX ON :`%s`(name)",label);
            session.run(cypherSql);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }


    public static void createIndex(String label,String prop) {
        try (Session session = neo4jDriver.session()) {
            String cypherSql=String.format("CREATE INDEX ON :`%s`(%s)",label,prop);
            session.run(cypherSql);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
    public static HashMap<String, Object> getSingleGraphNode(String cypherSql) {
        List<HashMap<String, Object>> ent = getGraphNode(cypherSql);
        if (ent.size() > 0) {
            return ent.get(0);
        }
        return null;
    }


    public static List<HashMap<String, Object>> getGraphTable(String cypherSql) {
        List<HashMap<String, Object>> resultData = new ArrayList<HashMap<String, Object>>();
        try (Session session = neo4jDriver.session()) {
            log.debug(cypherSql);
            Result result = session.run(cypherSql);
            if (result.hasNext()) {
                List<Record> records = result.list();
                for (Record recordItem : records) {
                    List<Pair<String, Value>> f = recordItem.fields();
                    HashMap<String, Object> rss = new HashMap<String, Object>();
                    for (Pair<String, Value> pair : f) {
                        String key = pair.key();
                        Value value = pair.value();
                        rss.put(key, value);
                    }
                    resultData.add(rss);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return resultData;
    }

    public static List<HashMap<String, Object>> getGraphRelationShip(String cypherSql) {
        List<HashMap<String, Object>> ents = new ArrayList<HashMap<String, Object>>();
        try (Session session = neo4jDriver.session()) {
            log.debug(cypherSql);
            Result result = session.run(cypherSql);
            if (result.hasNext()) {
                List<Record> records = result.list();
                for (Record recordItem : records) {
                    List<Pair<String, Value>> f = recordItem.fields();
                    for (Pair<String, Value> pair : f) {
                        HashMap<String, Object> rss = new HashMap<String, Object>();
                        String typeName = pair.value().type().name();
                        if (typeName.equals("RELATIONSHIP")) {
                            Relationship rship = pair.value().asRelationship();
                            String uuid = String.valueOf(rship.id());
                            String sourceId = String.valueOf(rship.startNodeId());
                            String targetId = String.valueOf(rship.endNodeId());
                            rss.put("type", rship.type());
                            Map<String, Object> map = rship.asMap();
                            for (Entry<String, Object> entry : map.entrySet()) {
                                String key = entry.getKey();
                                rss.put(key, entry.getValue());
                            }
                            rss.put("uuid", uuid);
                            rss.put("sourceId", sourceId);
                            rss.put("targetId", targetId);
                            ents.add(rss);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return ents;
    }



    public static long getGraphValue(String cypherSql) {
        long val = 0;
        try (Session session = neo4jDriver.session()) {
            log.debug(cypherSql);
            Result cypherResult = session.run(cypherSql);
            if (cypherResult.hasNext()) {
                Record record = cypherResult.next();
                for (Value value : record.values()) {
                    val = value.asLong();
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return val;
    }

    public static HashMap<String, Object> getGraphNodeAndShip(String cypherSql) {
        HashMap<String, Object> mo = new HashMap<String, Object>();
        try (Session session = neo4jDriver.session()) {
            log.info(cypherSql);
            Result result = session.run(cypherSql);
            if (result.hasNext()) {
                List<Record> records = result.list();
                List<HashMap<String, Object>> ents = new ArrayList<>();
                List<HashMap<String, Object>> ships = new ArrayList<>();
                List<String> uuids = new ArrayList<>();
                for (Record recordItem : records) {
                    List<Pair<String, Value>> f = recordItem.fields();
                    for (Pair<String, Value> pair : f) {
                        HashMap<String, Object> rShips = new HashMap<String, Object>();
                        HashMap<String, Object> rss = new HashMap<String, Object>();
                        String typeName = pair.value().type().name();
                        if ("NULL".equals(typeName)) {
                            continue;
                        }
                        if ("NODE".equals(typeName)) {
                            Node noe4jNode = pair.value().asNode();
                            List<String> labels = (List<String>) noe4jNode.labels();
                            String labelsName = labels.get(0);
                            Map<String, Object> map = noe4jNode.asMap();
                            String uuid = String.valueOf(noe4jNode.id());
                            if (!uuids.contains(uuid)) {
                                for (Entry<String, Object> entry : map.entrySet()) {
                                    String key = entry.getKey();
                                    rss.put(key, entry.getValue());
                                }
                                rss.put("labelsName", labelsName);
                                rss.put("uuid", uuid);
                                uuids.add(uuid);
                            }
                            if (!rss.isEmpty()) {
                                ents.add(rss);
                            }
                        } else if ("RELATIONSHIP".equals(typeName)) {
                            Relationship rship = pair.value().asRelationship();
                            String uuid = String.valueOf(rship.id());
                            String sourceId = String.valueOf(rship.startNodeId());
                            String targetId = String.valueOf(rship.endNodeId());
                            Map<String, Object> map = rship.asMap();
                            for (Entry<String, Object> entry : map.entrySet()) {
                                String key = entry.getKey();
                                rShips.put(key, entry.getValue());
                            }
                            rShips.put("type", rship.type());
                            rShips.put("uuid", uuid);
                            rShips.put("sourceId", sourceId);
                            rShips.put("targetId", targetId);
                            ships.add(rShips);
                        } else if ("PATH".equals(typeName)) {
                            Path path = pair.value().asPath();
                            for (Node nodeItem : path.nodes()) {
                                Map<String, Object> map = nodeItem.asMap();
                                String uuid = String.valueOf(nodeItem.id());
                                rss = new HashMap<String, Object>();
                                if (!uuids.contains(uuid)) {
                                    for (Entry<String, Object> entry : map.entrySet()) {
                                        String key = entry.getKey();
                                        rss.put(key, entry.getValue());
                                    }
                                    rss.put("uuid", uuid);
                                    uuids.add(uuid);
                                }
                                if (!rss.isEmpty()) {
                                    ents.add(rss);
                                }
                            }
                            for (Relationship next : path.relationships()) {
                                rShips = new HashMap<>();
                                String uuid = String.valueOf(next.id());
                                String sourceId = String.valueOf(next.startNodeId());
                                String targetId = String.valueOf(next.endNodeId());
                                Map<String, Object> map = next.asMap();
                                for (Entry<String, Object> entry : map.entrySet()) {
                                    String key = entry.getKey();
                                    rShips.put(key, entry.getValue());
                                }
                                rShips.put("uuid", uuid);
                                rShips.put("sourceId", sourceId);
                                rShips.put("targetId", targetId);
                                ships.add(rShips);
                            }
                        } else if (typeName.contains("LIST")) {
                            Iterable<Value> val = pair.value().values();
                            Value next = val.iterator().next();
                            String type = next.type().name();
                            if ("RELATIONSHIP".equals(type)) {
                                Relationship rship = next.asRelationship();
                                String uuid = String.valueOf(rship.id());
                                String sourceId = String.valueOf(rship.startNodeId());
                                String targetId = String.valueOf(rship.endNodeId());
                                Map<String, Object> map = rship.asMap();
                                for (Entry<String, Object> entry : map.entrySet()) {
                                    String key = entry.getKey();
                                    rShips.put(key, entry.getValue());
                                }
                                rShips.put("uuid", uuid);
                                rShips.put("sourceId", sourceId);
                                rShips.put("targetId", targetId);
                                ships.add(rShips);
                            }
                        } else if (typeName.contains("MAP")) {
                            rss.put(pair.key(), pair.value().asMap());
                        } else {
                            rss.put(pair.key(), pair.value().toString());
                            ents.add(rss);
                        }
                    }
                }
                mo.put("node", ents);
                mo.put("relationship", toDistinctList(ships));
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return mo;
    }



    public static String getFilterPropertiesJson(String jsonStr) {
        return jsonStr.replaceAll("\"(\\w+)\"(\\s*:\\s*)", "$1$2"); // 去掉key的引号
    }


    public static <T> String getKeyValCyphersql(T obj) {
        Map<String, Object> map = new HashMap<String, Object>();
        List<String> sqlList = new ArrayList<String>();
        // 得到类对象
        Class userCla = obj.getClass();
        /* 得到类中的所有属性集合 */
        Field[] fs = userCla.getDeclaredFields();
        for (int i = 0; i < fs.length; i++) {
            Field f = fs[i];
            Class type = f.getType();

            f.setAccessible(true); // 设置些属性是可以访问的
            Object val = new Object();
            try {
                val = f.get(obj);
                if (val == null) {
                    val = "";
                }
                String sql = "";
                String key = f.getName();
                if (val instanceof String[]) {
                    //如果为true则强转成String数组
                    String[] arr = (String[]) val;
                    String v = "";
                    for (int j = 0; j < arr.length; j++) {
                        arr[j] = "'" + arr[j] + "'";
                    }
                    v = String.join(",", arr);
                    sql = "n." + key + "=[" + val + "]";
                } else if (val instanceof List) {
                    //如果为true则强转成String数组
                    List<String> arr = (ArrayList<String>) val;
                    List<String> aa = new ArrayList<String>();
                    String v = "";
                    for (String s : arr) {
                        s = "'" + s + "'";
                        aa.add(s);
                    }
                    v = String.join(",", aa);
                    sql = "n." + key + "=[" + v + "]";
                } else {
                    // 得到此属性的值
                    map.put(key, val);// 设置键值
                    if (type.getName().equals("int")) {
                        sql = "n." + key + "=" + val + "";
                    } else {
                        sql = "n." + key + "='" + val + "'";
                    }
                }

                sqlList.add(sql);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                log.error(e.getMessage());
            }
        }
        return String.join(",", sqlList);
    }



    public static HashMap<String, Object> getOneNode(String cypherSql) {
        HashMap<String, Object> ret = new HashMap<String, Object>();
        try (Session session = neo4jDriver.session()) {
            log.debug(cypherSql);
            Result result = session.run(cypherSql);
            if (result.hasNext()) {
                Record record = result.list().get(0);
                Pair<String, Value> f = record.fields().get(0);
                String typeName = f.value().type().name();
                if ("NODE".equals(typeName)) {
                    Node noe4jNode = f.value().asNode();
                    String uuid = String.valueOf(noe4jNode.id());
                    Map<String, Object> map = noe4jNode.asMap();
                    for (Entry<String, Object> entry : map.entrySet()) {
                        String key = entry.getKey();
                        ret.put(key, entry.getValue());
                    }
                    ret.put("uuid", uuid);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return ret;
    }

    public static boolean batchRunCypherWithTx(List<String> cyphers) {
        Session session = neo4jDriver.session();
        try (Transaction tx = session.beginTransaction()) {
            for (String cypher : cyphers) {
                tx.run(cypher);
            }
        } catch (Exception e) {
            log.info(e.getMessage());
            return false;
        }
        return true;
    }


    public static List<HashMap<String, Object>> toDistinctList(List<HashMap<String, Object>> list) {
        Set<String> keysSet = new HashSet<String>();
        Iterator<HashMap<String, Object>> it = list.iterator();
        while (it.hasNext()) {
            HashMap<String, Object> map = it.next();
            String uuid = (String) map.get("uuid");
            int beforeSize = keysSet.size();
            keysSet.add(uuid);
            int afterSize = keysSet.size();
            if (afterSize != (beforeSize + 1)) {
                it.remove();
            }
        }
        return list;
    }


    @Override
    public void close() throws Exception {
        neo4jDriver.close();
    }
}
