## Flink 基石

- Checkpoint：基于 Chandy-Lamport 算法，实现了分布式一致性快照，提供了一致性语义；
- State：丰富的 state API，valueState, ListState, MapState, BroadcastState;
- Time: 实现了 Watermark 机制，乱序数据处理，迟到数据容忍；
- Window：开箱即用的滚动、滑动、回话窗口。以及灵活的自定义窗口。

## Flink 架构

<div align="center">
    <img src="../../zzzimg/flink/flink架构.jpg" width="70%">
</div>

`Flink 的核心是分布式流式数据引擎，意味着数据以一次一个事件的形式被处理。`


## Flink 运行流程

<div align="center">
    <img src="../../zzzimg/flink/flink运行流程.jpg" width="90%">
</div>

1. Program Code：我们编写的 Flink 应用程序代码；

2. Optimizer: 优化器采用用户指定的程序计划，并创建一个优化计划，其中包含有关物理执行将如何进行的精确描述。它首先将用户程序转换为内部优化器表示，然后在不同的运输策略和本地策略之间进行选择。优化器还将内存分配给各个任务。目前这是以一种非常简单的方式来完成的：所有需要内存的子任务（例如reduce或join）都会得到相等的内存份额。**实现方法请看flink系列文章-optimizer。**

2、Job Client：Job Client 不是 Flink 程序执行的内部部分，但它是任务执行的起点。 Job Client 负责接受用户的程序代码，然后创建数据流，将数据流提交给 Job Manager 以便进一步执行。 执行完成后，Job Client 将结果返回给用户；

3、Job Manager：主进程（也称为作业管理器）协调和管理程序的执行。 它的主要职责包括安排任务，管理checkpoint ，故障恢复等。机器集群中至少要有一个 master，master 负责调度 task，协调 checkpoints 和容灾，高可用设置的话可以有多个 master，但要保证一个是 leader, 其他是 standby; Job Manager 包含 Actor system、Scheduler、Check pointing 三个重要的组件；

4、Task Manager：从 Job Manager 处接收需要部署的 Task。Task Manager 是在 JVM 中的一个或多个线程中执行任务的工作节点。 任务执行的并行性由每个 Task Manager 上可用的任务槽决定。 每个任务代表分配给任务槽的一组资源。 例如，如果 Task Manager 有四个插槽，那么它将为每个插槽分配 25％ 的内存。 可以在任务槽中运行一个或多个线程。 同一插槽中的线程共享相同的 JVM。 同一 JVM 中的任务共享 TCP 连接和心跳消息。Task Manager 的一个 Slot 代表一个可用线程，该线程具有固定的内存，注意 Slot 只对内存隔离，没有对 CPU 隔离。默认情况下，Flink 允许子任务共享 Slot，即使它们是不同 task 的 subtask，只要它们来自相同的 job。这种共享可以有更好的资源利用率。


引用：  
> http://www.54tianzhisheng.cn/2018/10/13/flink-introduction/