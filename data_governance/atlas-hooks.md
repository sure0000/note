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