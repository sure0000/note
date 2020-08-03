# spark job

## job 执行流程

<div align="center">
    <img src="../../zzzimg/spark/spark&#32;job.jpeg" />
</div>

在任务执行的过程中，其他组件协同工作，确保整个应用顺利执行。
1. 在集群启动的时候，各个slave节点（也可以说是worker）会向集群的Master注册，告诉Master我随时可以干活了，随叫随到
2. Master会根据一种心跳机制来实时监察集群中各个worker的状态，是否能正常工作
3. Driver Application提交作业的时候也会先向Master注册信息
4. 作业注册完毕之后，Master会向worker发射Executor命令
5. worker产生若干个Executor准备执行
6. 各个worker中的Executor会向Driver Application注册Executor信息，以便Driver Application能够将作业分发到具体的Executor
7. Executor会定期向Driver Application报告当前的状态更新信息
8. Driver Application发射任务到Executor执行


## spark job 在 yarn 中执行流程

<div align="center">
    <img src="../../zzzimg/spark/spark&#32;job&#32;detail.jpeg" width="50%" />
</div>

在 Yarn-Cluster 模式中，当用户向 Yarn 中提交一个应用程序后， Yarn 将分两个阶段运行该应用程序：第一个阶段是把 Spark 的 Driver 作为一个 ApplicationMaster 在 Yarn 集群中先启动；第二个阶段是由 ApplicationMaster 创建应用程序，然后为它向 ResourceManager 申请资源，并启动 Executor 来运行 Task，同时监控它的整个运行过程，直到运行完成。

1. Spark Yarn Client 向 Yarn 中提交应用程序。
2. ResourceManager 收到请求后，在集群中选择一个 NodeManager，并为该应用程序分配一个 Container，在这个 Container 中启动应用程序的 ApplicationMaster， ApplicationMaster 进行 SparkContext 等的初始化。
3. ApplicationMaster 向 ResourceManager 注册，这样用户可以直接通过 ResourceManager 查看应用程序的运行状态，然后它将采用轮询的方式通过RPC协议为各个任务申请资源，并监控它们的运行状态直到运行结束。
4. ApplicationMaster 申请到资源（也就是Container）后，便与对应的 NodeManager 通信，并在获得的 Container 中启动 CoarseGrainedExecutorBackend，启动后会向 ApplicationMaster 中的 SparkContext 注册并申请 Task。
5. ApplicationMaster 中的 SparkContext 分配 Task 给 CoarseGrainedExecutorBackend 执行，CoarseGrainedExecutorBackend 运行 Task 并向ApplicationMaster 汇报运行的状态和进度，以让 ApplicationMaster 随时掌握各个任务的运行状态，从而可以在任务失败时重新启动任务。
6. 应用程序运行完成后，ApplicationMaster 向 ResourceManager申请注销并关闭自己。


## DAG

DAG，有向无环图，Directed Acyclic Graph的缩写，常用于建模。Spark中使用DAG对RDD的关系进行建模，描述了RDD的依赖关系，这种关系也被称之为lineage，RDD的依赖关系使用Dependency维护，DAG在Spark中的对应的实现为DAGScheduler。

<div align="center">
    <img src="../../zzzimg/spark/dagTask.png" width="50%" />
</div>

**DAGScheduler作用**

1. compute DAG，执行DAG，得到stage和对应的task，通过TaskScheduler提交到集群
2. preferred locations，就近执行。 根据 cache 信息和 RDD 的 preferred Locations 获取 preferred location。 
3. fault-tolerant，stage 级别的容错。shuffle结束后，reducer 读取 map 的输出，如果读取失败，会触发DAGScheduler重新提交对应的Stage。

## spark executor

Executor是spark任务（task）的执行单元，运行在worker上，但是不等同于worker，实际上它是一组计算资源(cpu核心、memory)的集合。一个worker上的memory、cpu由多个executor共同分摊。

Application运行在Worker节点上的一个进程，该进程负责运行某些task，并且负责将数据存在内存或者磁盘上。在Spark on Yarn模式下，其进程名称为 CoarseGrainedExecutor Backend，一个CoarseGrainedExecutor Backend进程有且仅有一个executor对象，它负责将Task包装成taskRunner，并从线程池中抽取出一个空闲线程运行Task，这样，`每个CoarseGrainedExecutorBackend能并行运行Task的数据就取决于分配给它的CPU的个数`。