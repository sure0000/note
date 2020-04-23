## kafka hooks bridges

从 import-kafka.sh 中可以知道需要环境变量：

```shell
export KAFKA_HOME=/home/xyc/kafka_service/kafka
export HADOOP_HOME=/home/xyc/hadoop-2.7.7
export HBASE_CONF_DIR=/home/xyc/apache-atlas-1.2.0/conf/hbase
export ATLASCPPATH=/home/xyc/apache-atlas-1.2.0
```

从 atlas 1.2 版本的代码中可以看出，kafka bridge 默认读取 `ATLASCPPATH` 根目录下` atlas-application.properties` 文件, 否则回报以下错误：

```
2020-04-22 14:53:01,631 INFO  - [main:] ~ Looking for atlas-application.properties in classpath (ApplicationProperties:97)
2020-04-22 14:53:01,633 INFO  - [main:] ~ Looking for /atlas-application.properties in classpath (ApplicationProperties:102)
2020-04-22 14:53:01,634 INFO  - [main:] ~ Loading atlas-application.properties from null (ApplicationProperties:110)
2020-04-22 14:53:01,652 ERROR - [main:] ~ ImportKafkaEntities failed (KafkaBridge:150)
org.apache.atlas.AtlasException: Failed to load application properties
        at org.apache.atlas.ApplicationProperties.get(ApplicationProperties.java:121)
        at org.apache.atlas.ApplicationProperties.get(ApplicationProperties.java:73)
        at org.apache.atlas.kafka.bridge.KafkaBridge.main(KafkaBridge.java:102)
Caused by: org.apache.commons.configuration.ConfigurationException: Cannot locate configuration source null
        at org.apache.commons.configuration.AbstractFileConfiguration.load(AbstractFileConfiguration.java:259)
        at org.apache.commons.configuration.AbstractFileConfiguration.load(AbstractFileConfiguration.java:238)
        at org.apache.commons.configuration.AbstractFileConfiguration.<init>(AbstractFileConfiguration.java:197)
        at org.apache.commons.configuration.PropertiesConfiguration.<init>(PropertiesConfiguration.java:284)
        at org.apache.atlas.ApplicationProperties.<init>(ApplicationProperties.java:56)
        at org.apache.atlas.ApplicationProperties.get(ApplicationProperties.java:112)
        ... 2 more

```

## hive hooks

```xml
    <property>
      <name>hive.exec.post.hooks</name>
      <value>org.apache.atlas.hive.hook.HiveHook</value>
    </property>
```

- 将 hive hook 相关包拷贝到 hive 安装节点的 pathdir/hook/hive 目录
- 在 hive 配置文件 hive-env.sh 中添加 'export HIVE_AUX_JARS_PATH=<atlas package>/hook/hive'，让 hive 找到相关依赖包
- 将 atlas-application.properties 拷贝到 hive 安装的根目录下

在 atlas-application.properties 原有基础上添加 hive 相关配置：

```properties
atlas.hook.hive.synchronous=false # whether to run the hook synchronously. false recommended to avoid delays in Hive query completion. Default: false
atlas.hook.hive.numRetries=3      # number of retries for notification failure. Default: 3
atlas.hook.hive.queueSize=10000   # queue size for the threadpool. Default: 10000

atlas.cluster.name=primary # clusterName to use in qualifiedName of entities. Default: primary

atlas.kafka.zookeeper.connect=                    # Zookeeper connect URL for Kafka. Example: localhost:2181
atlas.kafka.zookeeper.connection.timeout.ms=30000 # Zookeeper connection timeout. Default: 30000
atlas.kafka.zookeeper.session.timeout.ms=60000    # Zookeeper session timeout. Default: 60000
atlas.kafka.zookeeper.sync.time.ms=20             # Zookeeper sync time. Default: 20
```