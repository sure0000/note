JobManager 协调每个 Flink 作业的部署，负责调度与资源管理。默认情况下，每个 Flink 集群都有一个 JobManager 实例。这会产生`单点故障（SPOF）`：如果 JobManager 崩溃，则无法提交新作业且运行中的作业也会失败。JobManager 高可用模式，就是为了解决这个问题。您可以为 standalone 集群和 YARN 集群配置高可用模式。

## standalone 集群高可用性

standalone 集群的 JobManager 高可用性的概念是，任何时候都有一个主 JobManager 和 多个备 JobManagers，以便在主节点失败时有新的 JobNamager 接管集群。这样就保证了没有单点故障，一旦备 JobManager 接管集群，作业就可以依旧正常运行。主备 JobManager 实例之间没有明确的区别。每个 JobManager 都可以充当主备节点。如图所示：

<div align="center">
    <img src="../zzzimg/flink/JM&#32;HA.jpg" width="50%" />
</div>


要启用 JobManager 高可用性功能，必须将高可用性模式设置为 zookeeper，配置 ZooKeeper quorum，将所有 JobManagers 主机及其 Web UI 端口写入配置文件。Flink 利用 ZooKeeper 在所有正在运行的 JobManager 实例之间进行分布式协调。ZooKeeper 是独立于 Flink 的服务，通过 leader 选举和轻量级一致性状态存储提供高可靠的分布式协调服务。

要启用 HA 集群，需要修改 conf/masters 与 conf/flink-conf.yaml 文件：

masters

```bash
# masters 文件包含启动 JobManagers 的所有主机以及 Web 用户界面绑定的端口，上面一行写一个。
# 默认情况下，job manager 选一个随机端口作为进程通信端口。您可以通过 high-availability.jobmanager.port 更改此设置。此配置接受单个端口（例如 50010），范围（50000-50025）或两者的组合（50010,50011,50020-50025,50050-50075）。

localhost:8081
xxx.xxx.xxx.xxx:8081
```

flink-conf.yaml

```bash
high-availability: zookeeper
high-availability.zookeeper.quorum: ip1:2181 [,...],ip2:2181
high-availability.storageDir: hdfs:///flink/ha/
```


## YARN 集群高可用性

**当运行高可用的 YARN 集群时，我们不会运行多个 JobManager 实例，而只会运行一个，该 JobManager 实例失败时，YARN 会将其重新启动。Yarn 的具体行为取决于您使用的 YARN 版本。**

Application Master 最大重试次数 ( yarn-site.xml 或者 flink-conf.yaml )

yarn-site.xml

```xml
<property>
  <name>yarn.resourcemanager.am.max-attempts</name>
  <value>4</value>
  <description>
    The maximum number of application master execution attempts.
  </description>
</property>
```

flink-conf.yaml

```yaml
yarn.application-attempts: 10
```

这意味着在如果程序启动失败，YARN 会再重试 9 次（9 次重试 + 1 次启动），如果启动 10 次作业还失败，yarn 才会将该任务的状态置为失败。如果因为节点硬件故障或重启，NodeManager 重新同步等操作，需要 YARN 继续尝试启动应用。这些重启尝试不计入 yarn.application-attempts 个数中。

yarn 容器关闭行为

- YARN 2.3.0 < 版本 < 2.4.0. 如果 application master 进程失败，则所有的 container 都会重启。
- YARN 2.4.0 < 版本 < 2.6.0. TaskManager container 在 application master 故障期间，会继续工作。这具有以下优点：作业恢复时间更快，且缩短所有 task manager 启动时申请资源的时间。	
- YARN 2.6.0 < version: 将尝试失败有效性间隔设置为 Flink 的 Akka 超时值。尝试失败有效性间隔表示只有在系统在一个间隔期间看到最大应用程序尝试次数后才会终止应用程序。这避免了持久的工作会耗尽它的应用程序尝试。



示例：

```yaml
# flink-conf.yaml
high-availability: zookeeper
high-availability.zookeeper.quorum: localhost:2181
yarn.application-attempts: 10

# zoo.cfg
server.0=localhost:2888:3888
```

引用
> http://www.54tianzhisheng.cn/2019/01/13/Flink-JobManager-High-availability/