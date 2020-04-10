## 安装
```
pip install elasticsearch-curator
```

## 配置
config.yaml
```
---
client:
  hosts:
    - 127.0.0.1
  port: 9200
  url_prefix:
  use_ssl: False
  certificate:
  client_cert:
  client_key:
  ssl_no_validate: False
  http_auth: elastic:elastic
  timeout: 30
  master_only: False

logging:
  loglevel: INFO
  logfile:
  logformat: default
  blacklist: ['elasticsearch', 'urllib3']
```
action.yaml
```
actions:
  1:
   action: rollover
   description: roll hot index to new index
   options:
     name: ruleengine.t_process_rule_instance_hot
     conditions:
       max_size: 50gb
       max_age: 7d
  2:
   action: rollover
   description: roll hot index to new index
   options:
     name: ruleengine.t_rule_flow_instance_parameter_hot
     conditions:
       max_size: 50gb
       max_age: 7d
  3:
   action: rollover
   description: roll hot index to new index
   options:
     name: ruleengine.t_process_routing_instance_hot
     conditions:
       max_size: 50gb
       max_age: 7d
  4:
   action: rollover
   description: roll hot index to new index
   options:
     name: ruleengine.t_rule_flow_element_instance_hot
     conditions:
       max_size: 50gb
       max_age: 7d
  5:
   action: rollover
   description: roll hot index to new index
   options:
     name: ruleengine.t_rule_flow_instance_hot
     conditions:
       max_size: 50gb
       max_age: 7d
```

## 执行
```
# dry run 模式
curator --config /etc/curator/config.yaml --dry-run /etc/curator/actions/ruleengine_rollover.yaml

# 真实模式
curator --config /etc/curator/config.yaml /etc/curator/actions/ruleengine_rollover.yaml
```

**delete indices**
```yml
---
# Remember, leave a key empty if there is no value.  None will be a string,
# not a Python "NoneType"
#
# Also remember that all examples have 'disable_action' set to True.  If you
# want to use this action as a template, be sure to set this to False after
# copying it.
actions:
  1:
    action: delete_indices
    description: >-
      Delete indices older than 30 days (based on index name), for logstash-
      prefixed indices. Ignore the error if the filter does not result in an
      actionable list of indices (ignore_empty_list) and exit cleanly.
    options:
      ignore_empty_list: True
      disable_action: False         # 修改成 False 生效
    filters:
    - filtertype: pattern
      kind: prefix
      value: log_es-
    - filtertype: age
      source: name
      direction: older
      timestring: '%Y.%m.%d'
      unit: days
      unit_count: 30
  2:
    action: delete_indices
    description: >-
      删除 rollover 产生的index,此例子保留最近30个索引
    options:
      ignore_empty_list: True
      disable_action: False         # 修改成 False 生效
    filters:
    - filtertype: count
      count: 30
      pattern: '(test-.*)-\d{6}$
      reverse: true
```

**rollover**
```yml
---

actions:
  1:
   action: rollover
   description: roll hot index to new index
   options:
     name: ruleengine.t_process_rule_instance_hot
     conditions:
       max_size: 50gb
       max_age: 7d
  2:
   action: rollover
   description: roll hot index to new index
   options:
     name: ruleengine.t_rule_flow_instance_parameter_hot
     conditions:
       max_size: 50gb
       max_age: 1d
```

**run curator**
```
https://www.elastic.co/guide/en/elasticsearch/client/curator/5.6/command-line.html
```