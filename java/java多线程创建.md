# java 多线程

- java 创建线程的方式
- 采用线程池的方式创建多线程

## java 创建线程的方式

runnable：线程执行后没有返回值

```java
public class Thread1 implements Runnable{ 
    @Overvide 
    public void run(){ // code } 
}
```

callable：线程执行后有返回值

```java
public class Thread2 implements Callable{ 
    @Override 
    public Object call() throws Exception { return null; } 
}
```

Thread 构造器创建线程： 单纯的使用 runnable 或 callable 的 run() 只是一个普通的类方法，只有使用 Thread.start() 方法才能创建一个线程。

```java
Thread thread1 = new Thread(new Thread1()); 
thread.start(); 

FutureTask futureTask = new FutureTask(new Thread2()); 
Thread thread2 = new Thread(futureTask); 
thread2.start(); 
if (futureTask.isDone) { 
    System.out.println(futureTask.get()); 
}
```

直接继承 Thread 类

```java
package com.thread;  
  
public class FirstThreadTest extends Thread{  
    int i = 0;  
    //重写run方法，run方法的方法体就是现场执行体  
    public void run()  
    {  
        for(;i<100;i++){  
        System.out.println(getName()+"  "+i);  
        }  
    }  
    public static void main(String[] args)  
    {  
        for(int i = 0;i< 100;i++)  
        {  
            System.out.println(Thread.currentThread().getName()+"  : "+i);  
            if(i==20)  
            {  
                new FirstThreadTest().start();  
                new FirstThreadTest().start();  
            }  
        }  
    }  
}
```
### 三种创建方式的对比

**采用实现Runnable、Callable接口的方式创建多线程**

优势：线程类只是实现了Runnable接口或Callable接口，还可以继承其他类。在这种方式下，多个线程可以共享同一个target对象，所以非常适合多个相同线程来处理同一份资源的情况，从而可以将CPU、代码和数据分开，形成清晰的模型，较好地体现了面向对象的思想。

劣势：编程稍微复杂，如果要访问当前线程，则必须使用Thread.currentThread()方法。

**使用继承Thread类的方式创建多线程**

优势：编写简单，如果需要访问当前线程，则无需使用Thread.currentThread()方法，直接使用this即可获得当前线程。

劣势：线程类已经继承了Thread类，所以不能再继承其他父类。

***建议使用实现 Runnable/Callable 接口的方式***

### future 与 futureTask 的区别 

future 包装 callable 的执行结果需要去判断线程任务是否执行结束，再去执行之后的逻辑代码

```java
boolean flag =true; 
while(flag) {            
     for(Iterator<Future<String>> iter  = results.iterator();iter.hasNext();){
           Future<String> f =iter.next();
                if(f.isDone()){
                  System.out.println(f.get());
                     iter.remove();                     
                 }
     }
     
     if(results.size()==0){
           flag =false;
     }
            
}
```

futureTask 可以指定 callable 任务结束之后，自动去执行下一任务
```java
class MyFutureTask extends FutureTask<String> {
 
    public MyFutureTask(Callable<String> callable) {
        super(callable);
    }
 
    @Override
    protected void done() {
        try {
            System.out.println(get() + " 线程执行完毕！~");
        } catch (InterruptedException | ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }    
}
```

## 线程池的方式创建线程

### 线程池的介绍

**corePoolSize**

默认情况下，在创建了线程池后，线程池中的线程数为0，当有任务来之后，就会创建一个线程去执行任务，当线程池中的线程数目达到corePoolSize后，就会把到达的任务放到缓存队列当中。核心线程在allowCoreThreadTimeout被设置为true时会超时退出，默认情况下不会退出。

**maxPoolSize**

当线程数大于或等于核心线程，且任务队列已满时，线程池会创建新的线程，直到线程数量达到maxPoolSize。如果线程数已等于maxPoolSize，且任务队列已满，则已超出线程池的处理能力，线程池会拒绝处理任务而抛出异常。

**keepAliveTime**

当线程空闲时间达到keepAliveTime，该线程会退出，直到线程数量等于corePoolSize。如果allowCoreThreadTimeout设置为true，则所有线程均会退出直到线程数量为0。

**allowCoreThreadTimeout**

是否允许核心线程空闲退出，默认值为false

**queueCapacity**

任务队列容量。从maxPoolSize的描述上可以看出，任务队列的容量会影响到线程的变化，因此任务队列的长度也需要恰当的设置。


### java 默认方式创建线程池
```java
// 创建固定大小的线程池
ExecutorService pool = Executors.newFixedThreadPool(taskSize); 

// 创建只有一个线程的线程池
ExecutorService pool = Executors.newSingleThreadPool(taskSize); 

// 创建一个不限线程数上限的线程池，任何提交的任务都将立即执行
ExecutorService pool = Executors.newCachedThreadPool(taskSize); 
```
> 默认方法的问题，`Executors.newFixedThreadPool` 底层使用了`LinkedBlockingQueue` 来构造队列，是一个无边界的阻塞队列，最大长度为` Integer.MAX_VALUE` ，因此可能造成 OOM。


**推荐采用** `ThreadPoolExecutor(coresize, maxsize, keepaliveTime, unit, ArrayBlockingQueue)` 

ArrayBlockingQueue是一个用数组实现的有界阻塞队列，必须设置容量。

ThreadPoolExecutor 创建线程池
```java
private static ExecutorService pool = new ThreadPoolExecutor(10,30,5,TimeUnit.SECONDS,new ArrayBlockingQueue<>(10)); 
pool.execute(new Thread1()); 
Feture result = pool.submit(new Thread2()); 
if (result.isDone()) { 
    System.out.println(resulr.get()) 
} 
pool.shutdown()
```


## Thread 常用方法

Thread#yield()：
执行此方法会向系统线程调度器（Schelduler）发出一个暗示，告诉其当前JAVA线程打算放弃对CPU的使用，但该暗示，有可能被调度器忽略。使用该方法，可以防止线程对CPU的过度使用，提高系统性能。

Thread#sleep(time)或Thread.sleep(time, nanos)：
使当前线程进入休眠阶段，状态变为：TIME_WAITING，暂时放弃CPU使用时间，不是放对象锁

Thread.interrupt()：
中断当前线程的执行，允许当前线程对自身进行中断，否则将会校验调用方线程是否有对该线程的权限。
如果当前线程因被调用Object#wait(),Object#wait(long, int), 或者线程本身的join(), join(long),sleep()处于阻塞状态中，此时调用interrupt方法会使抛出InterruptedException，而且线程的阻塞状态将会被清除。

Thread#interrupted()，返回true或者false：
查看当前线程是否处于中断状态，这个方法比较特殊之处在于，如果调用成功，会将当前线程的interrupt status清除。所以如果连续2次调用该方法，第二次将返回false。

Thread.isInterrupted()，返回true或者false：
与上面方法相同的地方在于，该方法返回当前线程的中断状态。不同的地方在于，它不会清除当前线程的interrupt status状态。

Thread#join()，Thread#join(time)：
A线程调用B线程的join()方法，将会使A等待B执行，直到B线程终止。如果传入time参数，将会使A等待B执行time的时间，如果time时间到达，将会切换进A线程，继续执行A线程。

