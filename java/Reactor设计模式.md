> 来源：https://www.jianshu.com/p/af202026ffc5

## 应用场景
解决多请求问题，但是这些请求不需要一直占有整个线程资源（比如IO操作时不必一直等待），所以不适合使用一个请求分配一个线程的多线程方案；类似于消息队列模型，但是是事件驱动，没有Queue来做缓冲；

优点：解耦、高效、提高复用；<br/>
缺点：需要操作系统底层支持、内部回调复杂。

## IO

**IO 操作步骤**
1. 数据准备，将数据从磁盘加载到内核缓存；
2. 将数据从内核缓存加载到用户缓存

**IO操作模型**
- 阻塞（等待数据全部读取成功再返回）
- 非阻塞（读取为空马上返回然后下次再读）
- 同步（用户缓存主动去读取内核缓存）
- 异步（内核缓存读取磁盘成功后通知用户缓存）

NIO是同步非阻塞模型，也是IO多路复用基础；Reactor模式基于同步I/O，Proactor模式基于异步I/O。

**IO多路复用**
区别于传统的多进程并发模型 (每有新的IO流就分配一个新的进程管理)，IO多路复用仅使用单个线程，通过记录跟踪每个I/O流的状态来同时管理多个I/O流（哪个IO流ready线程就处理哪个）。

select, poll, epoll 都是I/O多路复用的具体的实现：
- select：仅返回有无事件不返回具体事件Id，只能监控1024个连接，线程不安全
- poll：连接数无限制
- epoll：返回具体事件Id，线程安全

## Reactor 模式
处理一个或多个客户端并发请求服务的事件设计模式。当请求抵达后，服务处理程序使用I/O多路复用策略，然后同步地派发这些请求至相关的请求处理程序。

<img src="https://upload-images.jianshu.io/upload_images/10987585-cdf22ef1fc3cdb46.png?imageMogr2/auto-orient/strip|imageView2/2/w/802/format/webp" width="50%" alt="reactor 架构">

- Handle：事件（网络编程中就是一个Socket，数据库操作中就是一个DBConnection，Java NIO中的Channel）
- EventHandler：事件处理器，用于处理不同状态的事件
- Concrete Event Handler：事件处理器的具体实现，实现了事件处理器所提供的各种回调方法，从而实现特定于业务的逻辑
- Synchronous Event Demultiplexer：用于等待事件的发生，调用方在调用它的时候会被阻塞，一直阻塞到同步事件分离器上有事件产生为止（NIO中对应Selector，当Selector.select()返回时说明有事件发生，然后调用Selector的selectedKeys()方法获取Set<SelectionKey>，一个SelectionKey表示一个有事件发生的Channel以及该Channel上的事件类型）
- Initiation Dispatcher：用于管理EventHandler、分发event。通过Synchronous Event Demultiplexer来等待事件的发生，一旦事件发生，Initiation Dispatcher首先会分离出每一个事件，然后调用事件处理器，最后调用相关的回调方法来处理这些事件

**运行流程**
1. 初始化dispatcher，注册具体事件处理器到分发器（即指定什么事件触发什么事件处理器）
2. 注册完毕后，分发器调用handle_events方法启动事件循环，并启动Synchronous Event Demultiplexer等待事件发生（阻塞等待）
3. 当有事件发生，即某个Handle变为ready状态(如TCP socket变为等待读状态)，Synchronous Event Demultiplexer就会通知Initiation Dispatcher
4. Initiation Dispatcher根据发生的事件，将被事件源激活的Handle作为『key』来寻找并分发恰当的事件处理器回调方法


**具体模型分类**

单线程模型（I/O、非I/O业务操作都在一个线程上处理，可能会大大延迟I/O请求的响应）

<img src="https://upload-images.jianshu.io/upload_images/10987585-8dccfd237d9848a1.png?imageMogr2/auto-orient/strip|imageView2/2/w/700/format/webp" width="50%">

工作站线程池模型（非I/O操作从Reactor线程中移出转交给工作者线程池执行）

<img src="https://upload-images.jianshu.io/upload_images/10987585-d468fefc36440c59.png?imageMogr2/auto-orient/strip|imageView2/2/w/700/format/webp" width="50%">

多线程模型（mainReactor线程主要负责接收客户端的连接请求，然后将接收到的SocketChannel传递给subReactor，由subReactor来完成和客户端的通信），但是注意subReactor线程只负责完成I/O的read()或者write()操作，在读取到数据后业务逻辑的处理仍然放入到工作者线程池中完成，可避免因为read()数据量太大而导致后面的客户端连接请求得不到即时处理的情况

<img src="https://upload-images.jianshu.io/upload_images/10987585-b7749ce42331702f.png?imageMogr2/auto-orient/strip|imageView2/2/w/700/format/webp" width="50%">