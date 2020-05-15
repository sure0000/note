## 行存储与列存储的优缺点是什么？
> https://blog.csdn.net/vagabond6/article/details/79555282
## HBase是怎么实现的？

**Hbase特性与架构**
> https://www.ibm.com/developerworks/cn/analytics/library/ba-cn-bigdata-hbase/index.html

<div align="center">
    <img src="https://www.ibm.com/developerworks/cn/analytics/library/ba-cn-bigdata-hbase/image003.png" width= 50%>
</div>

HBase 的集群是通过 Zookeeper 来进行机器之前的协调，也就是说 HBase Master 与 Region Server 之间的关系是依赖 Zookeeper 来维护。当一个 Client 需要访问 HBase 集群时，Client 需要先和 Zookeeper 来通信，然后才会找到对应的 Region Server。

HBase Master 用于协调多个 Region Server，侦测各个 Region Server 之间的状态，并平衡 Region Server 之间的负载。HBase Master 还有一个职责就是负责分配 Region 给 Region Server。HBase 允许多个 Master 节点共存，但是这需要 Zookeeper 的帮助。不过当多个 Master 节点共存时，只有一个 Master 是提供服务的，其他的 Master 节点处于待命的状态。当正在工作的 Master 节点宕机时，其他的 Master 则会接管 HBase 的集群。

每一个 Region Server 管理着很多个 Region。对于 HBase 来说，Region 是 HBase 并行化的基本单元。因此，数据也都存储在 Region 中。这里我们需要特别注意，每一个 Region 都只存储一个 Column Family 的数据，并且是该 CF 中的一段（按 Row 的区间分成多个 Region）。Region 所能存储的数据大小是有上限的，当达到该上限时（Threshold），Region 会进行分裂，数据也会分裂到多个 Region 中，这样便可以提高数据的并行化，以及提高数据的容量。

每个 Region 包含着多个 Store 对象。每个 Store 包含一个 MemStore，和一个或多个 HFile。MemStore 便是数据在内存中的实体，并且一般都是有序的。当数据向 Region 写入的时候，会先写入 MemStore。当 MemStore 中的数据需要向底层文件系统倾倒（Dump）时（例如 MemStore 中的数据体积到达 MemStore 配置的最大值），Store 便会创建 StoreFile，而 StoreFile 就是对 HFile 一层封装。所以 MemStore 中的数据会最终写入到 HFile 中，也就是磁盘 IO。由于 HBase 底层依靠 HDFS，因此 HFile 都存储在 HDFS 之中。

**Hbase数据的可靠性**

HBase 中的 HLog 机制是 WAL 的一种实现，而 WAL（预写日志）是事务机制中常见的一致性的实现方式。每个 Region Server 中都会有一个 HLog 的实例，Region Server 会将更新操作（如 Put，Delete）先记录到 WAL（也就是 HLog）中，然后将其写入到 Store 的 MemStore，最终 MemStore 会将数据写入到持久化的 HFile 中（MemStore 到达配置的内存阀值）。这样就保证了 HBase 的写的可靠性。如果没有 WAL，当 Region Server 宕掉的时候，MemStore 还没有写入到 HFile，或者 StoreFile 还没有保存，数据就会丢失。

**HFile**

<div align="center">
    <img src="https://www.ibm.com/developerworks/cn/analytics/library/ba-cn-bigdata-hbase/image004.png" width= 50%>
</div>

HFile 由很多个数据块（Block）组成，并且有一个固定的结尾块。其中的数据块是由一个 Header 和多个 Key-Value 的键值对组成。在结尾的数据块中包含了数据相关的索引信息，系统也是通过结尾的索引信息找到 HFile 中的数据。HFile 中的数据块大小默认为 64KB。如果访问 HBase 数据库的场景多为有序的访问，那么建议将该值设置的大一些。如果场景多为随机访问，那么建议将该值设置的小一些。一般情况下，通过调整该值可以提高 HBase 的性能。

**HBase 的数据映射关系**

<div align="center">
    <img src="https://www.ibm.com/developerworks/cn/analytics/library/ba-cn-bigdata-hbase/image005.png" width= 50%>
</div>


**Hbase使用建议**
有上亿或上千亿行数据，HBase 才会是一个很好的备选。其次，需要确信业务上可以不依赖 RDBMS 的额外特性，例如，列数据类型, 二级索引，SQL 查询语言等。再而，需要确保有足够硬件；

1. 当客户端需要频繁的写一张表，随机的 RowKey 会获得更好的性能。
2. 当客户端需要频繁的读一张表，有序的 RowKey 则会获得更好的性能。
3. 对于时间连续的数据（例如 log），有序的 RowKey 会很方便查询一段时间的数据（Scan 操作）

一个 Region 对应于一个 CF。那么设想，如果在一个表中定义了多个 CF 时，就必然会有多个 Region。当 Client 查询数据时，就不得不查询多个 Region。这样性能自然会有所下降，尤其当 Region 夸机器的时候。因此在大多数的情况下，一个表格不会超过 2 到 3 个 CF，而且很多情况下都是 1 个 CF 就足够了。

## Hbase 中逻辑上数据的排布与物理上排布的关联

<div align="center">
    <img src="https://www.ibm.com/developerworks/cn/analytics/library/ba-cn-bigdata-hbase/image001.png" width="50%">
</div>

从上图我们看到 Row1 到 Row5 的数据分布在两个 CF 中，并且每个 CF 对应一个 HFile。并且逻辑上每一行中的一个单元格数据，对应于 HFile 中的一行，然后当用户按照 Row-key 查询数据的时候，HBase 会遍历两个 HFile，通过相同的 Row-Key 标识，将相关的单元格组织成行返回，这样便有了逻辑上的行数据。讲解到这，我们就大致了解 HBase 中的数据排布格式，以及与 RDBMS 的一些区别。