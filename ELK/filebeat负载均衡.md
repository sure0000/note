# filebeat负载均衡

> 来源：https://www.elastic.co/guide/en/beats/filebeat/current/load-balancing.html  
> 通过打开filebeat的负载均衡机制，定义filebeat输出主机与worker数可以实现接收主机的负载均衡。

```yml
filebeat.inputs:
- type: log
  paths:
    - /var/log/*.log
output.logstash:
  hosts: ["localhost:5044", "localhost:5045"]
  loadbalance: true
  worker: 2
```

 在6.x版本以后filebeat可以有多个input，但只能有一个output
 >来源：https://www.elastic.co/guide/en/beats/filebeat/current/configuration-filebeat-options.html 多个input
 https://www.elastic.co/guide/en/beats/filebeat/current/configuring-output.html 只能一个output

