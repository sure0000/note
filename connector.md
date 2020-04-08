```json
{
	"name": "kafka-cdc-hbase",
	"config": {
		"connector.class": "io.svectors.hbase.sink.HBaseSinkConnector",
		"tasks.max": 1,
		"topics": "xyctest26",
		"zookeeper.quorum": "10.10.77.153:2181",
		"event.parser.class": "io.svectors.hbase.parser.AvroEventParser",
		"hbase.table.name": "xyctest26",
		"hbase.xyctest26.rowkey.columns": "id,name",
		"hbase.xyctest26.rowkey.delimiter": "|",
		"hbase.xyctest26.family": "cf"
	}

}

{
  "name": "hdfs-sink",
  "config": {
    "connector.class": "io.confluent.connect.hdfs.HdfsSinkConnector",
    "tasks.max": "1",
    "topics": "xyctest20",
    "hdfs.url": "hdfs://10.10.77.108:8020",
    "flush.size": "3",
    "format.class": "io.confluent.connect.hdfs.json.JsonFormat"
  }
}

{
  "name": "hive-sink",
  "config": {
    "connector.class": "io.confluent.connect.hdfs.HdfsSinkConnector",
    "tasks.max": "1",
    "topics": "xyctest22",
    "hdfs.url": "hdfs://10.10.77.108:8020",
    "flush.size": "3",
    "hive.integration": true,
    "hive.metastore.uris": "thrift://10.10.77.108:9083",
    "schema.compatibility": "FULL"
  }
}


{
	"name": "neo4jsink",
	"config": {
		"topics": "xyctest29",
		"connector.class": "streams.kafka.connect.sink.Neo4jSinkConnector",
		"errors.retry.delay.max.ms": 1000,
		"errors.tolerance": "all",
		"errors.log.enable": true,
		"errors.deadletterqueue.topic.name": "test-error-topic",
		"errors.log.include.messages": true,
		"neo4j.server.uri": "bolt://10.10.77.152:7687",
		"neo4j.authentication.basic.username": "neo4j",
		"neo4j.authentication.basic.password": "supconit",
		"neo4j.encryption.enabled": false,
		"neo4j.topic.cypher.xyctest29": "create(student:Student{id: event.id,name: event.name,age: event.age})"
	}
	
}


{
	"name": "source_http",
	"config": {
		"connector.class": "com.supconit.HttpSourceConnector",
		"topic.postfix": "xyc",
		"connection.url": "http://data152:18083/",
		"schedule": "*/5 * * * * ?"
	}
}


{
	"name":"sink_172_16_100_33_Id",
	"config":{
		"connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
		"type.name": "doc",
		"connection.password": "elastic",
		"topics": "ruleengine.t_rule_flow_instance_parameter,ruleengine.t_rule_flow_element_instance,ruleengine.t_rule_flow_instance",
		"transforms.unwrap.field": "Id",
		"tasks.max": "4",
		"connection.username": "elastic",
		"transforms": "unwrap",
		"key.ignore": "false",
		"topic.index.map": "ruleengine.t_rule_flow_instance_parameter:ruleengine.t_rule_flow_instance_parameter_hot,ruleengine.t_rule_flow_element_instance:ruleengine.t_rule_flow_element_instance_hot,ruleengine.t_rule_flow_instance:ruleengine.t_rule_flow_instance_hot",
		"transforms.unwrap.type": "org.apache.kafka.connect.transforms.ExtractField$Key",
		"connection.url": "http://172.17.100.21:9200,http://172.17.100.22:9200,http://172.17.100.23:9200"
	}
}
```