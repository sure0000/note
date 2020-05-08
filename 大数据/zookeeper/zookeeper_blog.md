## 其他组件对zookeeper的依赖
**Hbase**
- https://www.jianshu.com/p/67a817a157ee

**kafka**
- https://blog.csdn.net/dly1580854879/article/details/71403867
- https://www.jianshu.com/p/f6261b8f8314

## zookeeper特性
> 参考：https://www.jianshu.com/p/abde992b9060

- 最终一致性：保证各个节点服务器数据能够最终达成一致，zk的招牌功能
- 顺序性：从同一客户端发起的事务请求，都会最终被严格的按照其发送顺序被应用到zk中，这也是zk选举leader的依据之一
- 可靠性：凡是服务器成功的使用一个事务，并完成了客户端的响应，那么这个事务所引起的服务端状态变更会被一直保留下去
- 实时性：zk不能保证多个客户端能同时得到刚更新的数据，所以如果要最新数据，需要在读数据之前强制调用sync接口来保证数据的实时性
- 原子性：数据更新要么成功要么失败
- 单一视图：无论客户端连的是哪个节点，看到的数据模型对外一致

## zookeeper架构
> 参考：https://blog.csdn.net/xuxiuning/article/details/51218941

![](http://www.aboutyun.com/data/attachment/forum/201405/15/103609wrmh43zzbz9h13ur.jpg)

1. 每个server在内存中存储了一份数据
2. zookeeper在启动时，将从实例中选举一个leader(Paxos协议)
3. leader负责处理数据更新等操作(Zab协议)
4. 一个更新操作成功，当且仅当大多数server在内存中成功修改

**zookeeper角色**
- Leader: 更新系统状态，处理事务请求，负责进行投票的发起与决议
- Follower：处理客户端非事务请求并向客户端返回结果，将写事务请求转给Leader，同步Leader状态，选主过程中参与投票。
- Observer：接收客户端读请求，将客户端写请求转发给Leader，不参与投票过程，只同步Leader的状态。目的是为了扩展系统，提高读取速度。
- client：请求发起方。

**zk选主**
> 参考：https://www.jianshu.com/p/75e48405d678
https://blog.csdn.net/liuyuehu/article/details/52136945

一个是服务器的选举状态，分为looking，leading，following和observer
- looking:寻找leader状态，处于该状态需要进入选举流程
- leading:leader状态，表明当前服务角色为leader
- following:跟随者状态，leader已经选举出，表明当前为follower角色
- observer:观察者状态

数据模型
投票信息中包含两个最基本的信息。
sid:即server id，用来标识该机器在集群中的机器序号。
zxid:即zookeeper事务id号。ZooKeeper状态的每一次改变, 都对应着一个递增的Transaction id, 该id称为zxid. 由于zxid的递增性质, 如果zxid1小于zxid2, 那么zxid1肯定先于zxid2发生. 创建任意节点, 或者更新任意节点的数据, 或者删除任意节点, 都会导致Zookeeper状态发生改变, 从而导致zxid的值增加.
以（sid，zxid）的形式来标识一次投票信息。例如，如果当前服务器要推举sid为1，zxid为8的服务器成为leader，那么投票信息可以表示为（1，8）

规则
集群中的每台机器发出自己的投票后，也会接受来自集群中其他机器的投票。每台机器都会根据一定的规则，来处理收到的其他机器的投票，以此来决定是否需要变更自己的投票。
规则如下：
（1）初始阶段，都会给自己投票。
（2）当接收到来自其他服务器的投票时，都需要将别人的投票和自己的投票进行pk，规则如下：
优先检查zxid。zxid比较大的服务器优先作为leader。
如果zxid相同的话，就比较sid，sid比较大的服务器作为leader。

**follower节点处理读写请求**
![image](https://user-images.githubusercontent.com/18415138/46254621-8c394e00-c4c4-11e8-9bd4-21ddd12b7a2f.png)

**Leader节点处理写请求**
![image](https://user-images.githubusercontent.com/18415138/46254634-af63fd80-c4c4-11e8-92fa-600aa1619eeb.png)

## zookeeper数据结构
![image](https://user-images.githubusercontent.com/18415138/46254653-f520c600-c4c4-11e8-8934-1533ea9ab61d.png)

zookeeper采用层次化的目录结构，命名符合常规文件系统规范； 
每个目录在zookeeper中叫做znode,并且其有一个唯一的路径标识； 
Znode可以包含数据和子znode（ephemeral类型的节点不能有子znode）； 
Znode中的数据可以有多个版本，比如某一个znode下存有多个数据版本，那么查询这个路径下的数据需带上版本； 
客户端应用可以在znode上设置监视器（Watcher） 
znode不支持部分读写，而是一次性完整读写 
Znode有两种类型，短暂的（ephemeral）和持久的（persistent）； 
Znode的类型在创建时确定并且之后不能再修改； 
ephemeralzn ode的客户端会话结束时，zookeeper会将该ephemeral znode删除，ephemeralzn ode不可以有子节点； 
persistent znode不依赖于客户端会话，只有当客户端明确要删除该persistent znode时才会被删除； 
Znode有四种形式的目录节点，PERSISTENT、PERSISTENT_SEQUENTIAL、EPHEMERAL、PHEMERAL_SEQUENTIAL。             
