# filebeat+logstash动态加载额外配置文件

> 来源： 
> filebeat https://www.elastic.co/guide/en/beats/filebeat/6.3/filebeat-configuration-reloading.html  
> logstash https://www.elastic.co/guide/en/logstash/current/multiple-pipelines.html

## 动态配置背景
对于不同服务组件的日志需要配置不同的接收路径与分别处理，写在同一个文件中不但使得文件冗长并且使得后期针对某个服务的调整会影响其他服务的日志接收与处理，因此在日志的接收与处理两端的配置需要能够动态分离配置。

## filebeat
通过配置filebeat.yml文件指定input配置文件的路径，以可配置的频率动态加载更改或者新增的input配置，例子如下：
```yml
filebeat.config.inputs:
  enabled: true
  path: configs/*.yml    #配置文件的路径
  scan_frequency: 10s    #扫描配置文件的时间间隔

output.logstash:
  # The Logstash hosts
  hosts: ["10.1.3.125:5043","10.1.3.123:5043"]
  loadbalance: true

```
input配置文件格式
```yml
 - type: log
   enabled: true
   paths:
     - /var/log/cloudera-scm-server/cloudera-scm-server.log
   fields:
     "tag": "cloudera-scm-server.log"

 - type: log
   enabled: true
   paths:
     - /var/log/cloudera-scm-server/cmf-server-perf.log
   fields:
     "tag": "cmf-server-perf.log"

 - type: log
   enabled: true
   paths:
     - /var/log/cloudera-scm-agent/cloudera-scm-agent.log
   fields:
     "tag": "cloudera-scm-agent.log"

```

## logstash
logstash可以通过配置多个pipeline，动态加载额外的配置,示例如下：
```yml
 - pipeline.id: cloudera  #自己取名对配置文件进行区别
   pipeline.workers: 1
   pipeline.batch.size: 1
   path.config: "/root/logstash-6.3.2/config/cloudera_pipeline.conf"

```
> 注意：一个pipeline对应一个filebeat端口，不同端口对应同一个filebeat将产生端口冲突。

cloudera_pipeline.conf 
```
input {
    beats {
        port => "5043"
    }
}
# The filter part of this file is commented out to indicate that it is
# optional.
# filter {
#
# }

output{
        if [fields][tag]=="cloudera-scm-server.log"{
                elasticsearch{
            hosts => ["10.1.3.123:9200","10.1.3.124:9200","10.1.3.125:9200"]
                manage_template => false
                index => "cloudera-scm-server "
            user => elastic
            password => elastic
        }
    }

    if [fields][tag]=="cmf-server-perf.log"{
                elasticsearch{
            hosts => ["10.1.3.123:9200","10.1.3.124:9200","10.1.3.125:9200"]
                manage_template => false
                index => "cmf-server-perf "
            user => elastic
            password => elastic
        }
    }

    if [fields][tag]=="cloudera-scm-agent.log"{
                elasticsearch{
            hosts => ["10.1.3.123:9200","10.1.3.124:9200","10.1.3.125:9200"]
                manage_template => false
                index => "cloudera-scm-agent "
            user => elastic
            password => elastic
        }
    }

}
```