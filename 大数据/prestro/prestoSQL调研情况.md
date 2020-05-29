## 针对 ES 中的数组类型

需要在 index 的 mapping 的 `_meta.presto` 中标记出来：

```
curl --request PUT \
    --url localhost:9200/doc/_mapping \
    --header 'content-type: application/json' \
    --data '
{
    "_meta": {
        "presto":{
            "array_string_field":{
                "isArray":true
            },
            "object_field":{
                "array_int_field":{
                    "isArray":true
                }
            },
        }
    }
}'
```

## 针对 Hbase phoenix 

仅支持 Phoenix 4.14.1 以上的版本