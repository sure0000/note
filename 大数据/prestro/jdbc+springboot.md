## 简单描述

presto 的 jdbc driver 与 mysql, pg 的 jdbc driver 在使用上并没有太大的差别，唯一需要注意的是查询表的定位问题，普通的数据查询是通过`database.table`来定位需要查询的数据表，而 presto 采用的是`catalog.schema.table`来定位需要查询的数据表。由于 presto 可以对异构数据源进行查询，所以通常 catalog 、schema、table 都需要动态来传入。本文采用了 `JdbcTemplate + springboot` 来测试 presto jdbc driver 的功能。（注：presto 有两个发行版，prestodb 与 prestosql，本文针对的是 prestosql）

## 环境配置

1. jdk 1.8+
   
2. driver `io.prestosql.jdbc.PrestoDriver`
```xml
<dependency>
    <groupId>io.prestosql</groupId>
    <artifactId>presto-jdbc</artifactId>
    <version>334</version>
</dependency>
```

3. springboot 配置

```yml
spring:
  datasource:
    username: root
    driver-class-name: io.prestosql.jdbc.PrestoDriver
    url: jdbc:presto://localhost:8900
```

## java 代码

```java
@Autowired
private JdbcTemplate jdbcTemplate;

private List<Map<String, Object>> getResult(String sql) {
        logger.info("execute sql {}", sql);

        RowMapper<Map<String, Object>> rowMapper = new RowMapper<Map<String, Object>>() {
            @Override
            public Map<String, Object> mapRow(ResultSet resultSet, int i) throws SQLException {
                Map<String, Object> resultMap = new HashMap<>();
                ResultSetMetaData meta =  resultSet.getMetaData();
                for (int j = 1; j <= meta.getColumnCount(); j++) {
                    resultMap.put(meta.getColumnName(j), resultSet.getObject(j));
                }
                return resultMap;
            }
        };

        try {
            return jdbcTemplate.query(sql, rowMapper);
        } catch (Exception e) {
            throw new APIException(ConstantsUtil.RESPONSE_ERROR_CODE, e.getMessage());
        }
}
```