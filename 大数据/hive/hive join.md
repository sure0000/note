## left join / right join / inner join 等关系一览

<div align="center">
    <img src="../../zzzimg/hadoop/hive%20sql.png" width="50%">
</div>

hive 仅支持等值 join,即不支持大于等于等操作：
`SELECT a.* FROM a LEFT OUTER JOIN b ON (a.id <> b.id)` 是无效的。

## HIVE join 优化

如果每张表的相同列都用于 join 中，hive 会将多张表的 join 转化为单个 mapreduce 任务。  
```sql
SELECT a.val, b.val, c.val FROM a JOIN b ON (a.key = b.key1) JOIN c ON (c.key = b.key1)
```

而如下的例子则将 join 转化为两个 mapreduce 任务：  
```sql
SELECT a.val, b.val, c.val FROM a JOIN b ON (a.key = b.key1) JOIN c ON (c.key = b.key2)
```

在每个 join 的 map/reduce 阶段，语句的最后一张表通过 reducer 被流化，其他的表都被 buffer，因此将最大的表放在语句的最后有利于减少 reducer 的内存需求。

通过 `STREAMTABLE` 可以指定被流化的表  
```sql
SELECT /*+ STREAMTABLE(a) */ a.val, b.val, c.val FROM a JOIN b ON (a.key = b.key1) JOIN c ON (c.key = b.key1)
```

## map join

它通常会用在如下的一些情景：在二个要连接的表中，有一个很大，有一个很小，这个小表可以存放在内存中而不影响性能。这样我们就把小表文件复制到每一个Map任务的本地，再让Map把文件读到内存中待用。

```sql
SELECT /*+ MAPJOIN(b) */ a.key, a.value
FROM a JOIN b ON a.key = b.key
```
map join 的限制为，在 `a FULL/RIGHT OUTER JOIN b` 的情况下不能使用。

针对 bucket 进行 mapjoin 需要设置：`set hive.optimize.bucketmapjoin = true`

针对 sort and bucket 进行 mapjoin 需要设置：
```shell
set hive.input.format=org.apache.hadoop.hive.ql.io.BucketizedHiveInputFormat;
set hive.optimize.bucketmapjoin = true;
set hive.optimize.bucketmapjoin.sortedmerge = true;
```

The following is not supported
- Union Followed by a MapJoin
- Lateral View Followed by a MapJoin
- Reduce Sink (Group By/Join/Sort By/Cluster By/Distribute By) Followed by MapJoin
- MapJoin Followed by Union
- MapJoin Followed by Join
- MapJoin Followed by MapJoin


参考：  
> https://cwiki.apache.org/confluence/display/Hive/LanguageManual+Joins