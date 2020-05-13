# Order, Sort, Cluster, 与 Distribute By 的比较

## order by

HSQL 中的 order by 与 SQL 中的 order by 类似：
```sql
colOrder: ( ASC | DESC )
colNullOrder: (NULLS FIRST | NULLS LAST)           -- (Note: Available in Hive 2.1.0 and later)
orderBy: ORDER BY colName colOrder? colNullOrder? (',' colName colOrder? colNullOrder?)*
query: SELECT expression (',' expression)* FROM src orderBy
```

order by 存在着一些限制，在严格模式下(i.e., hive.mapred.mode=strict)，order by 后面必须跟着 “limit”，非严格模式下则不用。原因是为了强制所有结果的总顺序，必须有一个reducer来对最终输出进行排序。如果输出中的行数太大，则单个reducer可能需要很长时间才能完成。


## Sort By

hsql 中的 sort by 与 SQL 中的 order by 类似：
```sql
colOrder: ( ASC | DESC )
sortBy: SORT BY colName colOrder? (',' colName colOrder?)*
query: SELECT expression (',' expression)* FROM src sortBy
```

hive 使用 `sort by` 对进入 reducer 之前的 row 进行排序，针对每个 reducer。


## sort by 与 order by 和 cluster by 的不同

sort by 为每个 reducer 进行排序，因此仅保证每个 reducer 内有序，order by 保证最终有序，因此会有一个最终 reducer来做强制排序。

cluster by 是根据字段来分区，如果有多个 reducer 分区， sort by 则是随机分区，以便在 reducer 上均匀地分布数据和负载。


## 设置 Sort By 类型

转换后，变量类型通常被视为字符串，这意味着数字数据将按字典顺序排序。为了克服这个问题，可以在使用SORT BY之前使用第二个带强制转换的SELECT语句。

```sql
FROM (FROM (FROM src
            SELECT TRANSFORM(value)
            USING 'mapper'
            AS value, count) mapped
      SELECT cast(value as double) AS value, cast(count as int) AS count
      SORT BY value, count) sorted
SELECT TRANSFORM(value, count)
USING 'reducer'
AS whatever
```

## Cluster By and Distribute By

hive 使用  Distribute By 分发相同的 column 到相同的 reducer 中，但是不保证顺序。Distribute By + sort by 可以实现与 cluster by 相同的效果。

```sql

SELECT col1, col2 FROM t1 CLUSTER BY col1

SELECT col1, col2 FROM t1 DISTRIBUTE BY col1 SORT BY col1 ASC, col2 DESC
```


