ksql 可扩展、弹性、容错、实时，支持数据过滤、转换、聚合、join、窗口、sessionization。

ksql 架构图 https://docs.confluent.io/current/_images/ksql-architecture-and-components1.png

## ksql server
> The KSQL server runs the engine that executes KSQL queries. This includes processing, reading, and writing data to and from the target Kafka cluster.<br>
KSQL servers form KSQL clusters and can run in containers, virtual machines, and bare-metal machines. You can add and remove servers to/from the same KSQL cluster during live operations to elastically scale KSQL's processing capacity as desired. You can deploy different KSQL clusters to achieve workload isolation.

- ksql server 是和 ksql cli client 与 kafka broker 分别分开运行的，在部署时可以分开远程部署。见上述架构图。
- 在实时操作时，可以添加或者移除**同一资源池**中的的 ksql server，由此来弹性控制查询处理的规模。
- 可以使用不同的资源池来实现负载隔离。例如，一个用来生产，一个用来测试。
- 一次只能连接一个 ksql server，ksql cli 不支持自动故障转移到另一个 ksql server;

**启动命令**
```
启动 ksql-server
$ <path-to-confluent>/bin/ksql-server-start <path-to-confluent>/etc/ksql/ksql-server.properties

启动 ksql cli
LOG_DIR=./ksql_logs <path-to-confluent>/bin/ksql http://localhost:8088
```

**ksql 配置**
- 安全配置，HTTPS，用户密码认证
- ksql client/stream 设置
- ksql 查询设置
- ksql server 设置
- RBAC 权限控制配置

## ksql 语法
**DDL**
```
CREATE STREAM
CREATE TABLE
DROP STREAM
DROP TABLE
CREATE STREAM AS SELECT (CSAS)
CREATE TABLE AS SELECT (CTAS)
```
**DML**
```
SELECT
INSERT INTO
CREATE STREAM AS SELECT (CSAS)
CREATE TABLE AS SELECT (CTAS)
```



