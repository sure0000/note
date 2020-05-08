> streams/src/main/java/org/apache/kafka/streams/KafkaStreams.java

## 源码注释

一个 kafka client 允许执行对一个或多个 input topics 来源的 input 数据进行持续不断的计算，
并且将输出数据发送到 0个，1个或多个 topic中。

这个计算逻辑既可以使用 `Topology.java` 定义一个 DAG 拓扑图来指定，也可以使用 `StreamBuilder.java`
提供的 high-level DSL 来定义转换。

在配置文件中为正在进行的处理工作指定，一个 `kafkaStreams` 实例可以包含一个或者多个线程。

一个 `KafkaStreams` 实例可以与其他拥有相同 `StreamsConfig#APPLICATION_ID_CONFIG application ID` 作为一个（可能是分布式）相互协作的 Stream 处理应用。（不论是在相同的进程、机器，还是不同的进程、机器）

这个实例会根据 `input topic partitions` 的指派对作业进行划分，所以所有的分区会被消费。

如果一个实例被新增或者失败，所有剩余的实例将会`rebalance`分区的指派，来平衡处理负载并且保证所有的 input topic 的分区都被处理。

一个 kafkaStreams 实例内部包含一个普通的 KafkaProducer 和 KafkaConsumer 实例，用于读取 input 数据
和写出 output 数据。

## 案例代码

```java
Properties props = new Properties();
props.put(StreamsConfig.APPLICATION_ID_CONFIG, "my-stream-processing-application");
props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

StreamsBuilder builder = new StreamsBuilder();
builder.<String, String>stream("my-input-topic").mapValues(value -> String.valueOf(value.length())).to("my-output-topic");

KafkaStreams streams = new KafkaStreams(builder.build(), props);
streams.start();
 ```

 ## 源码说明

 ```java
  /**
     * Kafka Streams 状态是一个 Kafka Streams instance 可能成为的状态.
     * 一个实例在同一时间只能拥有一种状态。
     * 状态的转换如下：
     * <pre>
     *                 +--------------+
     *         +&lt;----- | Created (0)  |
     *         |       +-----+--------+
     *         |             |
     *         |             v
     *         |       +----+--+------+
     *         |       | Re-          |
     *         +&lt;----- | Balancing (1)| --------&gt;+
     *         |       +-----+-+------+          |
     *         |             | ^                 |
     *         |             v |                 |
     *         |       +--------------+          v
     *         |       | Running (2)  | --------&gt;+
     *         |       +------+-------+          |
     *         |              |                  |
     *         |              v                  |
     *         |       +------+-------+     +----+-------+
     *         +-----&gt; | Pending      |&lt;--- | Error (5)  |
     *                 | Shutdown (3) |     +------------+
     *                 +------+-------+
     *                        |
     *                        v
     *                 +------+-------+
     *                 | Not          |
     *                 | Running (4)  |
     *                 +--------------+
     *
     *
     * </pre>
     * Note the following:
     * - RUNNING state 将会转换到 REBALANCING 只有它的任一线程处于 PARTITION_REVOKED or PARTITIONS_ASSIGNED 状态
     * - REBALANCING state 将会转换到 RUNNING 如果它的任一线程处于 RUNNING state
     * - 除了 NOT_RUNNING 其它任何状态都可转换到 PENDING_SHUTDOWN (whenever close is called)
     * - 尤其重要的: If the global stream thread dies, or all stream threads die (or both) then
     *   the instance will be in the ERROR state. The user will need to close it.
     */
```