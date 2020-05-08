> 来源：https://mp.weixin.qq.com/s/GwQKSPrnjZvCuV6_ixnAHA

不建议用户使用 Kafka Streams，因为其**缺少检查点机制**，也**不具备随机排序**等功能，而 KSQL 以 Kafka Streams 为基础，因此其同样继承了后者所固有的不少问题。

## 检查点机制

检查点，是将当前整体态定入至持久存储（S3/HDFS）中。因此，一旦发生大规模故障，恢复程序将直接读取前一个检查点，重播该检查点之后的所有消息（通常在 1000 秒以内），以便快速恢复后续处理能力。总体而言，检查点支持下的恢复流程一般仅耗时几秒钟到几分钟不等。

kafka streams 缺少检查点机制，一旦某个节点无法正常运行，则必须从主题中重播所有消息并将其插入数据库内。只有执行完成整个流程，处理才能恢复至原有状态并继续进行。

Flink 中的检查点机制被称作快照。

## 随机排序

随机排序是分布式处理流程的重要组成部分，其本质是将数据与同一个键整合起来的实现方法。如果需要对数据进行分析，则很可能会接触到随机排序。

Kafka Streams 的随机排序与 Flink 或者 Spark Streaming 中的随机排序存在巨大差异。下面来看看 JavaDoc 中关于其工作原理的描述：

> 如果某个键变更运算符在实际使用之前发生了变化（例如 selectKey(KeyValueMapper)、map(KeyValueMapper), flatMap(KeyValueMapper) 或者 transform(TransformerSupplier, String…)），且此后没有发生数据重新分发（例如通过 through(String)），那么在 Kafka 当中创建一个内部重新分区主题。该主题将被命名为“${applicationId}-XXX-repartition”的形式，其中，“applicationId”由用户在 StreamsConfig 中通过 APPLICATION_ID_CONFIG 参数进行指定，“XXX”为内部生成的名称，而“-repartition”则为固定后缀。开发者可以通过 KafkaStreams.toString() 检索所有已生成的内部主题名称。

**这意味着只要变更键（一般用于分析），Kafka Streams 就会新建一个主题来实现随机排序。这会快速增加代理上的负载与数据总量，并最终导致系统崩溃。**

