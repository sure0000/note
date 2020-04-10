# es Long类型无法查询问题分析与解决方案

## 场景还原：

在查询上图两条记录时，kibana显示数据都在，但通过BatchId去查询时，使用第二条记录的BatchId:15316703685571028进行过滤查询可以成功，使用第一条记录的BatchId:15316703694181456进行过滤查询却失败，通过Id过滤却都可以查询成功，因此可以确定面临的问题是：数据存在，却无法通过所有的BatchId进行有效的过滤查询。

初步分析： 
观察两个BatchId的特点，长度一样，因此没有越界，猜想可能es对Long类型的支持不好(毕竟主要是用来做文本查询)，尝试通过重建索引过程将BatchId的类型改为text类型观察是否有BatchId查询不成功，尝试结果失败。

再次分析： 
观察失败结果，发现即使BatchId的类型被设置为text，es对纯数字依然默认转为Long类型。于是提出第二方案，在BatchId第一位插入字母a，让es将Batchid转为text类型，使用方经过一层解析理论可以正常使用，尝试结果查询成功。但使用方因实际需求不接受此方案。

深度挖掘： 
考虑到BatchId在没有越界的情况下，有的可以查询成功有的却失败，猜想可能是数字类型的精度问题，查询官网支持的类型中包含double类型，再次尝试，重建索引，将BatchId设为double类型，插入10000条17位数字后，每条都可查询成功，以此解决es Long型数据某些值无法过滤问题。

## 创建索引
```
curl -X PUT -u elastic:elastic "localhost:9200/instance_para_test06" -H 'Content-Type: application/json' -d'
{
	"mappings": {
		"kafka-connect": {
			"properties": {
				"BatchId": {
					"type": "double"
				},
				"CreatedDatetime": {
					"type": "date",
					"format": "yyyy-MM-dd'T'HH:mm:ssZZ||yyyy-MM-dd||epoch_millis"
				},
				"ElementArtfiactId": {
					"type": "long"
				},
				"ElementArtifactName": {
					"type": "text",
					"fields": {
						"keyword": {
							"type": "keyword",
							"ignore_above": 256
						}
					}
				},
				"ElementExecutedDurationInMillis": {
					"type": "long"
				},
				"ElementExecutedEndDatetime": {
					"type": "date"
				},
				"ElementExecutedStartDatetime": {
					"type": "date"
				},
				"ElementExecutedStatus": {
					"type": "text",
					"fields": {
						"keyword": {
							"type": "keyword",
							"ignore_above": 256
						}
					}
				},
				"ExecutedEndDatetime": {
					"type": "date",
					"format": "yyyy-MM-dd'T'HH:mm:ssZZ||yyyy-MM-dd||epoch_millis"
				},
				"ExecutedStartDatetime": {
					"type": "date",
					"format": "yyyy-MM-dd'T'HH:mm:ssZZ||yyyy-MM-dd||epoch_millis"
				},
				"Id": {
					"type": "long"
				},
				"LastUpdatedDatetime": {
					"type": "date",
					"format": "yyyy-MM-dd'T'HH:mm:ssZZ||yyyy-MM-dd||epoch_millis"
				}
			}
		}
	}
}'
```
## 重建索引
```
curl -X POST -u elastic:elastic "localhost:9200/_reindex/" -H 'Content-Type: application/json' -d'
{
    "source": {
        "index": "rules_engine.t_rule_flow_instance_reindex"
    }, 
    "dest": {
        "index": "rules_engine.t_rule_flow_instance"
    }
}'
```

## shell 脚本插入数据
```
#/bin/bash
base=152781106299900000
for i in `seq 1 1000`
do
    tmp=RANDOM
    batchid=$[base+tmp]
   curl -X POST -u elastic:elastic "es1:9200/instance_para_test06/180601to08" -H 'Content-Type: application/json' -d '{"batchid": '"$batchid"'}'
    echo $batchid
done
```