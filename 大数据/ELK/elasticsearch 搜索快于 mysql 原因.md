> 参考资料：<br>
> https://www.cnblogs.com/bypp/p/7755307.html<br>
> https://infoq.cn/article/database-timestamp-02?utm_source=infoq&utm_medium=related_content_link&utm_campaign=<br>
> http://www.cnblogs.com/bonelee/p/6226185.html

## 前提
mysql 基于 b+ 树结构的索引，b-tree 是为写入优化的索引结构。elasticsearch 基于倒排索引，应用于不需要支持快速更新的场景，采用预先排序等方式换取了更快的检索效率，代价使得更新更慢。

## 索引原理
通过不断地缩小想要获取数据的范围来筛选出最终想要的结果，同时把随机的事件变成顺序的事件，也就是说，有了这种索引机制，我们可以总是用同一种查找方式来锁定数据。

## 磁盘IO与预读
考虑到磁盘IO是非常高昂的操作，计算机操作系统做了一些优化，当一次IO时，不光把当前磁盘地址的数据，而是把相邻的数据也都读取到内存缓冲区内，因为局部预读性原理告诉我们，当计算机访问一个地址的数据的时候，与其相邻的数据也会很快被访问到。每一次IO读取的数据我们称之为一页(page)。具体一页有多大数据跟操作系统有关，一般为4k或8k，也就是我们读取一页内的数据时候，实际上才发生了一次IO，这个理论对于索引的数据结构设计非常有帮助。

## b+ 树
![](https://user-images.githubusercontent.com/18415138/54344817-adf64e00-467c-11e9-876e-41e271559d6b.png)

非叶子节点不存储真实的数据，只存储指引搜索方向的数据项，真实的数据存在于叶子节点即3、5、9、10、13、15、28、29、36、60、75、79、90、99，如17、35并不真实存在于数据表中。

**b+树性质**
- 索引字段要尽量的小：通过上面的分析，我们知道IO次数取决于b+数的高度h，假设当前数据表的数据为N，每个磁盘块的数据项的数量是m，则有h=㏒(m+1)N，当数据量N一定的情况下，m越大，h越小；而m = 磁盘块的大小 / 数据项的大小，磁盘块的大小也就是一个数据页的大小，是固定的，如果数据项占的空间越小，数据项的数量越多，树的高度越低。这就是为什么每个数据项，即索引字段要尽量的小，比如int占4字节，要比bigint8字节少一半。这也是为什么b+树要求把真实的数据放到叶子节点而不是内层节点，一旦放到内层节点，磁盘块的数据项会大幅度下降，导致树增高。当数据项等于1时将会退化成线性表。
- 索引的最左匹配特性（即从左往右匹配）：当b+树的数据项是复合的数据结构，比如(name,age,sex)的时候，b+数是按照从左到右的顺序来建立搜索树的，比如当(张三,20,F)这样的数据来检索的时候，b+树会优先比较name来确定下一步的所搜方向，如果name相同再依次比较age和sex，最后得到检索的数据；但当(20,F)这样的没有name的数据来的时候，b+树就不知道下一步该查哪个节点，因为建立搜索树的时候name就是第一个比较因子，必须要先根据name来搜索才能知道下一步去哪里查询。比如当(张三,F)这样的数据来检索时，b+树可以用name来指定搜索方向，但下一个字段age的缺失，所以只能把名字等于张三的数据都找到，然后再匹配性别是F的数据了， 这个是非常重要的性质，即索引的最左匹配特性。

## es/lucence 倒排索引
一个文档对应多个关键词，反过来通过关键词对应多个文档ID作为索引，则称为倒排索引,如下图：
![倒排索引](https://user-images.githubusercontent.com/18415138/54345230-99ff1c00-467d-11e9-881d-19367bd7473e.png)

在 es 中为了加快索引添加了一层 term index,索引结构则如下图所示：

![](https://static001.infoq.cn/resource/image/37/6a/378bc62acf1a493c402291a8f8e99e6a.jpg)

term index 基于 FST（Finite State Transducer） 数据结构，能够极大的减少空间的占有率，当使用FST插入cat、deep、do、dog、dogs等词时过程如下：

![](https://user-images.githubusercontent.com/18415138/54346105-7210b800-467f-11e9-9057-8fd6e0501617.png)

![](https://user-images.githubusercontent.com/18415138/54346142-88b70f00-467f-11e9-8aef-b5d7de9835e9.png)

![](https://user-images.githubusercontent.com/18415138/54346545-3b876d00-4680-11e9-9875-ee9ec39c1d91.png)

![](https://user-images.githubusercontent.com/18415138/54346597-53f78780-4680-11e9-83fb-c1de78cfb770.png)

![](https://user-images.githubusercontent.com/18415138/54346654-6a054800-4680-11e9-95b8-d1425eb12eaa.png)
由于term index占用空间少，使得将整个term index缓存在内存中成为了可能，于是es 的索引结构如下图所示：
![](https://static001.infoq.cn/resource/image/e4/26/e4599b618e270df9b64a75eb77bfb326.jpg)

Mysql 只有 term dictionary 这一层，是以 b-tree 排序的方式存储在磁盘上的。检索一个 term 需要若干次的 random access 的磁盘操作。而 Lucene 在 term dictionary 的基础上添加了 term index 来加速检索，term index 以树的形式缓存在内存中。从 term index 查到对应的 term dictionary 的 block 位置之后，再去磁盘上找 term，大大减少了磁盘的 random access 次数。