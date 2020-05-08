## kafka stream 特征
- stateful processing features
- fault tolerance
- processing guarantees

# stream
stream 代表了一个无边界的、持续更新的数据集，一个 stream 由多个 stream partition 组成，stream parition 是一个不可变数据记录的，有序、可重放和可容错的序列，其中的数据记录被定义为键值对。

## stream processing application

stream 处理应用并不运行在 broker 中，而是运行在不同的独立 JVM 实例或者单独的集群中。
<div align="center">
    <img src="https://docs.confluent.io/current/_images/streams-apps-not-running-in-brokers.png"
    alt="stream processing application" width="50%">
</div>


## stream 架构

<div align="center">
<img src="https://docs.confluent.io/current/_images/streams-architecture-overview.jpg" width="50%" >
</div>

## 处理器拓扑结构

处理器拓扑图或者简单拓扑图定义了你的应用的流处理计算逻辑，例如，输入数据如何转换成输出数据。拓扑是由流（边）或共享状态存储连接的流处理器（节点）的构成的图. 在拓扑中有两个特殊的处理器:
- source processor
- sink processor

## stream processor

stream processor 是处理器拓扑结构中的一个节点，用于转换数据。标准的操作有
map/filter/join/aggregations等。kafka Stream 提供了两套 API 来定义 stream processtors:
1. The declarative, functional DSL 
2. The imperative, lower-level Processor API

## stateful stream processing

- 无状态(stateless): 处理的消息之间相互独立；
- 有状态(stateful): join,aggregate,window 消息之间相互关联，需要由 fault-tolerant manner 进行状态维护。

## stream-table duality

- stream as table: 流可以被视为表的变更日志，通过从头到尾重放变更日志来重建表，可以很容易地将其转换为“真实”表
- table as stream: 在某个时间点，表可以被视为流中每个键的最新值的快照（流的数据记录是键-值对）。因此，表是伪装的流，通过迭代表中的每个键值项，可以很容易地将其转换为“真实”流。


## Out-of-Order Handling (乱序处理)

流任务可能正在处理多个主题分区，如果用户将应用程序配置为不等待所有分区包含一些缓冲数据，并从时间戳最小的分区中选择以处理下一条记录，则稍后当为其他主题分区提取某些记录时，它们的时间戳可能比那些已处理的记录小，从而有效地导致较旧的记录在较新的记录之后被处理。

如果用户想要处理这样的无序数据，通常他们需要允许他们的应用程序等待更长的时间，同时在等待时间内记录他们的状态，即在延迟、成本和正确性之间做出权衡决定。在kafka流中，用户可以为窗口聚合配置窗口操作符，以实现这种权衡。



