# dead letter queue
> https://www.elastic.co/guide/en/logstash/current/dead-letter-queues.html

**作用**

用于处理logstash解析失败的数据，使解析失败的数据不被丢弃，可使用插件再次被使用。

**配置**
```yml
# logstash.yml
dead_letter_queue.enable: true
path.dead_letter_queue: "path/to/data/dead_letter_queue"
```

**处理图**

![](https://www.elastic.co/guide/en/logstash/current/static/images/dead_letter_queue.png)

**处理失败队列数据**

```yml
input {
  dead_letter_queue {
    path => "/path/to/data/dead_letter_queue" 
    commit_offsets => true 
    pipeline_id => "main" 
  }
}

output {
  stdout {
    codec => rubydebug { metadata => true }
  }
}
```
