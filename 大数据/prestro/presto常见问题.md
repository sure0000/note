## 连接 oracle 字符问题

> failed: Non supported character set (add orai18n.jar in your classpath): ZHS16GBK

解决办法：把orai18n.jar复制到plugin/oracle目录下就好了



https://blog.csdn.net/qq_27657429/article/details/79928519

hive vs presto https://blog.treasuredata.com/blog/2015/03/20/presto-versus-hive/

Presto内存调优及原理（基础篇）https://blog.csdn.net/gv7lzb0y87u7c/article/details/81049861?utm_medium=distribute.pc_relevant.none-task-blog-baidujs-2


## presto 有哪些操作是在单节点上执行的？

count(distinct x)

考虑使用approx_distinct(x)代替，但是需要注意这个函数有个大约在2.3%的标准误差, 如果需要精确统计的情况, 请绕道.

UNION

UNION有个功能是: 如果两条记录一样, 会只保留一条记录(去重).如果不考虑去重的情况, 请使用UNION ALL

ORDER BY

Presto对数据排序是作用在单节点上的，如果要排序的数据量超过百万行, 要谨慎考虑. 如果非要排序,尽量将排序的字段减少些.

## 如何减少表扫描的范围

- 添加限定条件
- 将大数据量的表水平拆分，查不同的表分区

