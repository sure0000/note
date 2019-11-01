## 架构图

<div align="center">
    <img src="https://netty.io/3.8/guide/images/architecture.png" width="70%">
</div>

##  ChannelBuffer 

特性：
- 如果有必要，你可以自己定义专属 buffer 类型；
- 透明零拷贝是通过内置的复合缓冲区类型实现的;
- 动态 buffer 类型是开箱即用的，其容量按需扩展，就像StringBuffer一样。
- 再也不用去调用 flip()
- 通常比 ByteBuffer 更快

**合并与切分 ChannelBuffers**

数据在通信层进行传输时，数据通常需要合并或切片。传统方式下，多个包的数据通过拷贝到一个新的 byte buffer 完成合并，Netty 通过 ChannelBuffer “指向”需要的 buffers 支持了一种零拷贝方法，消除了实施拷贝操作的需求。

<div align="center">
    <img src="https://netty.io/3.8/guide/images/combine-slice-buffer.png" width="50%">
</div>

## 通用异步I/O API

netty有一个名为channel的通用异步i/o接口，它抽象出点对点通信所需的所有操作。也就是说，一旦您在一个netty传输上编写了应用程序，您的应用程序就可以在其他netty传输上运行。支持的类型：

- NIO-based TCP/IP transport (See org.jboss.netty.channel.socket.nio),
- OIO-based TCP/IP transport (See org.jboss.netty.channel.socket.oio),
- OIO-based UDP/IP transport, and
- Local transport (See org.jboss.netty.channel.local).

## 基于拦截连模式的事件模型

对于事件驱动的应用程序，必须有定义良好且可扩展的事件模型。NETY有一个定义在I/O上的定义良好的事件模型，它还允许您实现自己的事件类型而不破坏现有代码，因为每个事件类型都是通过严格的类型层次结构与其他事件类型区分开来的。

一个 ChannelEvent 在一个 ChannelPipeline 中被一系列 ChannelHandler 处理，管道实现了拦截过滤器模式的高级形式，使用户可以完全控制如何处理事件以及管道中的处理程序如何相互交互。

```java
// you can define what to do when data is read from a socket:

public class MyReadHandler implements SimpleChannelHandler {
     public void messageReceived(ChannelHandlerContext ctx, MessageEvent evt) {
            Object message = evt.getMessage();
         // Do something with the received message.
            ...
 
            // And forward the event to the next handler.
            ctx.sendUpstream(evt);
        }
 }
// You can also define what to do when a handler receives a write request:

  public class MyWriteHandler implements SimpleChannelHandler {
      public void writeRequested(ChannelHandlerContext ctx, MessageEvent evt) {
            Object message = evt.getMessage();
       // Do something with the message to be written.
            ...

            // And forward the event to the next handler.
        ctx.sendDownstream(evt);
        }
}
 ```