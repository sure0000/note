## Non-blocking IO（非阻塞IO）
Java NIO可以让你非阻塞的使用IO，例如：当线程从通道读取数据到缓冲区时，线程还是可以进行其他事情。当数据被写入到缓冲区时，线程可以继续处理它。从缓冲区写入通道也类似。

## Channels and Buffers（通道和缓冲区）
标准的IO基于字节流和字符流进行操作的，而NIO是基于通道（Channel）和缓冲区（Buffer）进行操作，数据总是从通道读取到缓冲区中，或者从缓冲区写入到通道中。

字节流和字符流: InputStream/OutputStream、Reader/Writer

**I/O获取的演变**
> https://blog.csdn.net/qq_22771739/article/details/86370771

传统的数据流：CPU处理IO，性能损耗太大

改为：内存和IO接口之间加了 DMA(直接存储器)，DMA向CPU申请权限，IO的操作全部由DMA管理。CPU不要干预。
若有大量的IO请求，会造成DMA的走线过多，则也会影响性能。

改DMA为Channel，Channel为完全独立的单元，不需要向CPU申请权限，专门用于IO。

**演变架构图**

<div align="center"> 
    <img src="https://images2017.cnblogs.com/blog/307536/201707/307536-20170731144838583-1592690474.png" width="50%">
    <img src="https://images2017.cnblogs.com/blog/307536/201707/307536-20170731144858802-1222092512.png" width="50%">
    <img src="https://images2017.cnblogs.com/blog/307536/201707/307536-20170731144908771-376135818.png" width="50%">
</div>

**Buffer**

缓冲区本质上是一块可以写入数据，然后可以从中读取数据的内存。这块内存被包装成NIO Buffer对象，并提供了一组方法，用来方便的访问该块内存。

**Seletor**
> http://ifeve.com/selectors/

Selector（选择器）是Java NIO中能够检测一到多个NIO通道，并能够知晓通道是否为诸如读写事件做好准备的组件。这样，一个单独的线程可以管理多个channel，从而管理多个网络连接。仅用单个线程来处理多个Channels的好处是，只需要更少的线程来处理通道。事实上，可以只用一个线程处理所有的通道。对于操作系统来说，线程之间上下文切换的开销很大，而且每个线程都要占用系统的一些资源（如内存）。因此，使用的线程越少越好。

但是，现代的操作系统和CPU在多任务方面表现的越来越好，所以多线程的开销随着时间的推移，变得越来越小了。实际上，如果一个CPU有多个内核，不使用多任务可能是在浪费CPU能力。

注册 Selector

为了将Channel和Selector配合使用，必须将channel注册到selector上:
```java
channel.configureBlocking(false);
SelectionKey key = channel.register(selector,
	Selectionkey.OP_READ);
```
与Selector一起使用时，Channel必须处于非阻塞模式下。这意味着不能将FileChannel与Selector一起使用。

register()方法的第二个参数，这是一个“interest集合”，意思是在通过Selector监听Channel时对什么事件感兴趣，这四种事件用SelectionKey的四个常量来表示：

- SelectionKey.OP_CONNECT
- SelectionKey.OP_ACCEPT
- SelectionKey.OP_READ
- SelectionKey.OP_WRITE

通过Selector选择通道
```java
int select()
int select(long timeout)
int selectNow()

select()阻塞到至少有一个通道在你注册的事件上就绪了。
select(long timeout)和select()一样，除了最长会阻塞timeout毫秒(参数)。
selectNow()不会阻塞，不管什么通道就绪都立刻返回（译者注：此方法执行非阻塞的选择操作。如果自从前一次选择操作后，没有通道变成可选择的，则此方法直接返回零。）
```

**完整示例**

```java
Selector selector = Selector.open();
channel.configureBlocking(false);
SelectionKey key = channel.register(selector, SelectionKey.OP_READ);
while(true) {
  int readyChannels = selector.select();
  if(readyChannels == 0) continue;
  Set selectedKeys = selector.selectedKeys();
  Iterator keyIterator = selectedKeys.iterator();
  while(keyIterator.hasNext()) {
    SelectionKey key = keyIterator.next();
    if(key.isAcceptable()) {
        // a connection was accepted by a ServerSocketChannel.
    } else if (key.isConnectable()) {
        // a connection was established with a remote server.
    } else if (key.isReadable()) {
        // a channel is ready for reading
    } else if (key.isWritable()) {
        // a channel is ready for writing
    }
    keyIterator.remove();
  }
}
```