## 几个重要目录

/var/lib/ambari-server/resources/common-services: ambari server 端用于保存stacks中的公共服务，被 stacks 中的service 引用，自定义服务一般放在此处，如图：
<div align="center">
    <img src="../zzzimg/hadoop/server-common-service.png" />
</div>

/var/lib/ambari-server/resources/stacks/HDP/2.6/services/: ambari server 端stacks 服务的文件，ambari UI 中实际看到的服务，存在多个 stacks ,每个 stacks 可以拥有独立的服务或者引用公共的服务，如图：
<div align="center">
    <img src="../zzzimg/hadoop/server-stacks-service.png" />
</div>

/var/lib/ambari-agent/cache/: ambari agent 端缓存的由 server 端重启后分发的各种配置，cluster_configuration 用于存储配置文件的参数，如图：
<div align="center">
    <img src="../zzzimg/hadoop/agent-cache.png" />
</div>

/usr/hdp/current/ ：ambari agent 端自定义的服务安装位置：
<div align="center">
    <img src="../zzzimg/hadoop/agent-install.png" />
</div>