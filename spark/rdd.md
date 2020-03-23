## overview

The `main abstraction` Spark provides is a `resilient distributed dataset (RDD)`, which is a collection of elements partitioned across the nodes of the cluster that can be operated on in parallel. RDDs are created by starting with a file in the Hadoop file system (or any other Hadoop-supported file system), or an existing Scala collection in the driver program, and transforming it. Users may also ask Spark to persist an RDD in memory, allowing it to be reused efficiently across parallel operations. Finally, RDDs automatically recover from node failures.

A `second abstraction` in Spark is `shared variables` that can be used in parallel operations. By default, when Spark runs a function in parallel as a set of tasks on different nodes, it ships a copy of each variable used in the function to each task. Sometimes, a variable needs to be shared across tasks, or between tasks and the driver program. Spark supports two types of shared variables: `broadcast variables`, which can be used to cache a value in memory on all nodes, and `accumulators`, which are variables that are only “added�? to, such as counters and sums.

## details

RDD全称叫做弹性分布式数据集(Resilient Distributed Datasets)，它是一种分布式的内存抽象，表示一个只读的记录分区的集合，它只能通过其他RDD转换而创建，为此，RDD支持丰富的转换操作(如map, join, filter, groupBy等)，通过这种转换操作，新的RDD则包含了如何从其他RDDs衍生所必需的信息，所以说RDDs之间是有依赖关系的。基于RDDs之间的依赖，RDDs会形成一个有向无环图DAG，该DAG描述了整个流式计算的流程，实际执行的时候，RDD是通过血缘关系(Lineage)一气呵成的，即使出现数据分区丢失，也可以通过血缘关系重建分区，总结起来，`基于RDD的流式计算任务可描述为：从稳定的物理存储(如分布式文件系统)中加载记录，记录被传入由一组确定性操作构成的DAG，然后写回稳定存储。`

## RDD的依赖关系

如果父RDD的每个分区最多只能被子RDD的一个分区使用，我们称之为（narrow dependency）窄依赖；若一个父RDD的每个分区可以被子RDD的多个分区使用，我们称之为（wide dependency）宽依赖。`简单来讲窄依赖就是父子RDD分区间”一对一“的关系，宽依赖就是”一对多“关系`，具体理解可参考下图：


<div align="center">
    <img src="../zzzimg/spark/dependcy.png" width="50%" />
</div>

**为什么Spark要将依赖分成这两种呢?**

首先，从计算过程来看，窄依赖是数据以管道方式经一系列计算操作可以运行在了一个集群节点上，如（map、filter等），宽依赖则可能需要将数据通过跨节点传递后运行（如groupByKey），有点类似于MR的shuffle过程。

其次，从失败恢复来看，窄依赖的失败恢复起来更高效，因为它只需找到父RDD的一个对应分区即可，而且可以在不同节点上并行计算做恢复；宽依赖则牵涉到父RDD的多个分区，恢复起来相对复杂些。

综上， 这里引入了一个新的概念Stage。`Stage可以简单理解为是由一组RDD组成的可进行优化的执行计划`。如果RDD的衍生关系都是窄依赖，则可放在同一个Stage中运行，若RDD的依赖关系为宽依赖，则要划分到不同的Stage。这样Spark在执行作业时，会按照Stage的划分, 生成一个完整的最优的执行计划。下面引用一张比较流行的图片辅助大家理解Stage，如图RDD¬-A到RDD-B和RDD-F到RDD-G均属于宽依赖，所以与前面的父RDD划分到了不同的Stage中。


<div align="center">
    <img src="../zzzimg/spark/stage.png" width="50%" />
</div>

## RDD 数据结构

RDD 的属性主要包括（rddname、sparkcontext、sparkconf、parent、dependency、partitioner、parttions、checkpoint、storageLevel）

<div align="center">
    <img src="../zzzimg/spark/rdd&#32;parttions.png" width="50%" />
    <img src="../zzzimg/spark/rdd.png" width="50%" />
</div>

**rddname**

即rdd的名称

**sparkcontext**

SparkContext为Spark job的入口，由Spark driver创建在client端，包括集群连接，RddID，创建抽样，累加器，广播变量等信息。

**sparkconf**

- Spark api：控制大部分的应用程序参数，可以用SparkConf对象或者Java系统属性设置
- 环境变量：可以通过每个节点的conf/spark-env.sh脚本设置。例如IP地址、端口等信息
- 日志配置：可以通过log4j.properties配置


**parent**

指向依赖父RDD的partition id，利用dependencies方法可以查找该RDD所依赖的partiton id的List集合，即上图中的parents。

**iterator**

迭代器，用来查找当前RDD Partition与父RDD中Partition的血缘关系。并通过StorageLevel确定迭代位置，直到确定真实数据的位置。迭代方式分为checkpoint迭代和RDD迭代， 如果StorageLevel为NONE则执行computeOrReadCheckpoint计算并获取数据，此方法也是一个迭代器，迭代checkpoint数据存放位置，迭代出口为找到真实数据或内存。如果Storagelevel不为空，根据存储级别进入RDD迭代器，继续迭代父RDD的结构，迭代出口为真实数据或内存。迭代器内部有数据本地化判断，先从本地获取数据，如果没有则远程查找。

**prisist**

rdd存储的level，即通过storagelevel和是否可覆盖判断，
storagelevel分为 5中状态 ，useDisk, useMemory, useOffHeap, deserialized, replication 可组合使用。

**parttions**

Spark RDD是被分区的，`在生成RDD时候`，一般可以指定分区的数量，如果不指定分区数量，当RDD从集合创建时候，则默认为该程序所分配到的资源的CPU核数，如果是从HDFS文件创建，默认为文件的Block数。

**partitioner 分区方式**

`shuffle 阶段RDD的分区方式`。RDD的分区方式主要包含两种（Hash和Range），这两种分区类型都是针对K-V类型的数据。如是非K-V类型，则分区为None。 Hash是以key作为分区条件的散列分布，分区数据不连续，极端情况也可能散列到少数几个分区上，导致数据不均等；Range按Key的排序平衡分布，分区内数据连续，大小也相对均等。

**checkpoint**

Spark提供的一种缓存机制，当需要计算的RDD过多时，为了避免重新计算之前的RDD，可以对RDD做checkpoint处理，检查RDD是否被物化或计算，并将结果持久化到磁盘或HDFS。与spark提供的另一种缓存机制cache相比， cache缓存数据由executor管理，当executor消失了，被cache的数据将被清除，RDD重新计算，而checkpoint将数据保存到磁盘或HDFS，job可以从checkpoint点继续计算。

**storageLevel**

一个枚举类型，用来记录RDD的存储级别。存储介质主要包括内存、磁盘和堆外内存，另外还包含是否序列化操作以及副本数量。如：MEMORY_AND_DISK_SER代表数据可以存储在内存和磁盘，并且以序列化的方式存储。是判断数据是否保存磁盘或者内存的条件。



## 参考文献
> 
> https://www.jianshu.com/p/dd7c7243e7f9?from=singlemessage
> 
> http://sharkdtu.com/posts/spark-rdd.html
> 
> https://blog.csdn.net/qq_31598113/article/details/70832701