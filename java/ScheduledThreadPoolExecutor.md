> https://www.jianshu.com/p/502f9952c09b

ScheduledThreadPoolExecutor可以用来在给定延时后执行异步任务或者周期性执行任务.

<div align="center">
    <img src="https://upload-images.jianshu.io/upload_images/2615789-adf418781bb01bf1.png?imageMogr2/auto-orient/strip|imageView2/2/w/856/format/webp" width="70%">
</div>

1. 从UML图可以看出，ScheduledThreadPoolExecutor继承了ThreadPoolExecutor，也就是说ScheduledThreadPoolExecutor拥有execute()和submit()提交异步任务的基础功能;ScheduledThreadPoolExecutor类同时实现了ScheduledExecutorService，该接口定义了ScheduledThreadPoolExecutor能够延时执行任务和周期执行任务的功能；
2. ScheduledThreadPoolExecutor有两个重要的内部类：DelayedWorkQueue和ScheduledFutureTask。可以看出DelayedWorkQueue实现了BlockingQueue接口，也就是一个阻塞队列，ScheduledFutureTask则是继承了FutureTask类，也表示该类用于返回异步任务的结果。

ScheduledThreadPoolExecutor的核心线程池的线程个数为指定的corePoolSize，当核心线程池的线程个数达到corePoolSize后，就会将任务提交给有界阻塞队列DelayedWorkQueue

ScheduledThreadPoolExecutor实现了ScheduledExecutorService接口，该接口定义了可延时执行异步任务和可周期执行异步任务的特有功能，相应的方法分别为：
```java
//达到给定的延时时间后，执行任务。这里传入的是实现Runnable接口的任务，
//因此通过ScheduledFuture.get()获取结果为null
public ScheduledFuture<?> schedule(Runnable command,
                                       long delay, TimeUnit unit);
//达到给定的延时时间后，执行任务。这里传入的是实现Callable接口的任务，
//因此，返回的是任务的最终计算结果
 public <V> ScheduledFuture<V> schedule(Callable<V> callable,
                                           long delay, TimeUnit unit);

//是以上一个任务开始的时间计时，period时间过去后，
//检测上一个任务是否执行完毕，如果上一个任务执行完毕，
//则当前任务立即执行，如果上一个任务没有执行完毕，则需要等上一个任务执行完毕后立即执行
public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
                                                  long initialDelay,
                                                  long period,
                                                  TimeUnit unit);
//当达到延时时间initialDelay后，任务开始执行。上一个任务执行结束后到下一次
//任务执行，中间延时时间间隔为delay。以这种方式，周期性执行任务。
public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
                                                     long initialDelay,
                                                     long delay,
                                                     TimeUnit unit);
```

ScheduledThreadPoolExecutor最大的特色是能够周期性执行异步任务，当调用schedule,scheduleAtFixedRate和scheduleWithFixedDelay方法时，实际上是将提交的任务转换成的ScheduledFutureTask类
```java
public ScheduledFuture<?> schedule(Runnable command,
                                   long delay,
                                   TimeUnit unit) {
    if (command == null || unit == null)
        throw new NullPointerException();
    RunnableScheduledFuture<?> t = decorateTask(command,
        new ScheduledFutureTask<Void>(command, null,
                                      triggerTime(delay, unit)));
    delayedExecute(t);
    return t;
}
```
为了保证ScheduledThreadPoolExecutor能够延时执行任务以及能够周期性执行任务，ScheduledFutureTask重写了run方法：
```java
public void run() {
    boolean periodic = isPeriodic();
    if (!canRunInCurrentRunState(periodic))
        cancel(false);
    else if (!periodic)
        //如果不是周期性执行任务，则直接调用run方法
        ScheduledFutureTask.super.run();
        //如果是周期性执行任务的话，需要重设下一次执行任务的时间
    else if (ScheduledFutureTask.super.runAndReset()) {
        setNextRunTime();
        reExecutePeriodic(outerTask);
    }
}
```

ScheduledFutureTask最主要的功能是根据当前任务是否具有周期性，对异步任务进行进一步封装。如果不是周期性任务（调用schedule方法）则直接通过run()执行，若是周期性任务，则需要在每一次执行完后，重设下一次执行的时间，然后将下一次任务继续放入到阻塞队列中。

在ScheduledThreadPoolExecutor中还有另外的一个重要的类就是DelayedWorkQueue。为了实现其ScheduledThreadPoolExecutor能够延时执行异步任务以及能够周期执行任务，DelayedWorkQueue进行相应的封装。DelayedWorkQueue是一个基于堆的数据结构，类似于DelayQueue和PriorityQueue。在执行定时任务的时候，每个任务的执行时间都不同，所以DelayedWorkQueue的工作就是按照执行时间的升序来排列，执行时间距离当前时间越近的任务在队列的前面。


