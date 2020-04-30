原理

Java远程调试的原理是两个VM之间通过debug协议进行通信，然后以达到远程调试的目的。两者之间可以通过socket进行通信。

调试方法

首先被debug程序的虚拟机在启动时要开启debug模式，启动debug监听程序。jdwp是Java Debug Wire Protocol的缩写。


```bash
java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=n zhc_application
```

这是jdk1.7版本之前的方法，1.7之后可以这样用：


```bash
java -agentlib:jdwp=transport=dt_socket,address=8000,server=y,suspend=n zhc_application
```

zhc_application是main程序，server=y表示是监听其他debugclient端的请求。address=8000表示端口是8000

然后用一个debug客户端去debug远程的程序了，比如用Eclipse自带的debug客户端，填写运行被debug程序的虚拟机监听的端口号和地址，选择connect方式为attach。