## 安装

通常 NameNode 与 ResourceManager 在集群中被分别部署在单独的机器上。其他的服务如 `Web App Proxy Server` and `MapReduce Job History server` 通常会根据负载部署在独立硬件或共享设施上。集群中剩余的机器既是 dataNode 又是 NodeManager，都属于 worker。

## 配置非安全模式的 Hadoop

hadoop 的 java 配置，由两种类型的配置文件驱动：
- Read-only default 配置：core-default.xml, hdfs-default.xml, yarn-default.xml and mapred-default.xml.
- Site-specific 配置文件：etc/hadoop/core-site.xml, etc/hadoop/hdfs-site.xml, etc/hadoop/yarn-site.xml and etc/hadoop/mapred-site.xml.

HDFS 后台进程有 NameNode, SecondaryNameNode, and DataNode. YARN 的后台进程有 ResourceManager, NodeManager, and WebAppProxy. 如果使用了 MapReduce 就还有 MapReduce Job History Server 进程。

### 配置 hadoop 后台程序环境

管理员应该使用 etc/hadoop/hadoop-env.sh、etc/hadoop/mapred-env.sh、etc/hadoop/yarn-env.sh 来进行 hadoop 后台进程配置。