## kafka-server-start.sh
1. 设置 log4j
2. 设置 kafka head
3. 判断是否进入后台启动模式

```bash
# "USAGE: $0 [-daemon] [-name servicename] [-loggc] classname [opts]"
exec $base_dir/kafka-run-class.sh -name kafkaServer -loggc kafka.Kafka
```

## kafka-server-start.sh
1. 参数检查，Java版本检查，jvm等检查
2. 判断命令执行模式

```bash
# Launch mode
if [ "x$DAEMON_MODE" = "xtrue" ]; then
  nohup $JAVA $KAFKA_HEAP_OPTS $KAFKA_JVM_PERFORMANCE_OPTS $KAFKA_GC_LOG_OPTS $KAFKA_JMX_OPTS $KAFKA_LOG4J_OPTS -cp $CLASSPATH $KAFKA_OPTS "$@" > "$CONSOLE_OUTPUT_FILE" 2>&1 < /dev/null &
else
  exec $JAVA $KAFKA_HEAP_OPTS $KAFKA_JVM_PERFORMANCE_OPTS $KAFKA_GC_LOG_OPTS $KAFKA_JMX_OPTS $KAFKA_LOG4J_OPTS -cp $CLASSPATH $KAFKA_OPTS "$@"
fi
```