# kafka connector 设计架构源码分析

kafka connector 常常被用来连接外部数据源进行数据的导入导出，为了更加深入的理解 connector 的工作模式，本文对源码中实现 connector
的主逻辑进行了简单的分析。

## 初始化加载不同 connector 插件

在执行 `bin/connect-distributed.sh config/connect-distributed.conf` 之后将会执行 ConnectDistributed.java 中的 main 函数，
主要功能是加载 connector 插件目录下所有的 jar 包，注册到插件管理中，之后作为 rest 服务对应的 resource，并同时启动 jetty server
服务用于构建 rest 服务，启动 KafkaOffsetBackingStore、StatusBackingStore、ConfigBackingStore 来管理存储 offset/status/config
等信息。

```java
/**
 * <p>
 * Command line utility that runs Kafka Connect in distributed mode. In this mode, the process joints a group of other workers
 * and work is distributed among them. This is useful for running Connect as a service, where connectors can be
 * submitted to the cluster to be automatically executed in a scalable, distributed fashion. This also allows you to
 * easily scale out horizontally, elastically adding or removing capacity simply by starting or stopping worker
 * instances.
 * </p>
 */
public class ConnectDistributed {
    private static final Logger log = LoggerFactory.getLogger(ConnectDistributed.class);

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            log.info("Usage: ConnectDistributed worker.properties");
            Exit.exit(1);
        }

        String workerPropsFile = args[0];
        // 配置文件转换为map
        Map<String, String> workerProps = !workerPropsFile.isEmpty() ?
                Utils.propsToStringMap(Utils.loadProps(workerPropsFile)) : Collections.<String, String>emptyMap();

        Time time = Time.SYSTEM;
        // 插件注册
        Plugins plugins = new Plugins(workerProps);
        plugins.compareAndSwapWithDelegatingLoader();
        // 分布式配置加载
        DistributedConfig config = new DistributedConfig(workerProps);

        // 启动 jetty server，构建 rest 服务用于管理kafka connector， 
        RestServer rest = new RestServer(config);
        URI advertisedUrl = rest.advertisedUrl();
        String workerId = advertisedUrl.getHost() + ":" + advertisedUrl.getPort();

        // 使用一个 topic 来存储 connector 涉及的 offset 信息
        KafkaOffsetBackingStore offsetBackingStore = new KafkaOffsetBackingStore();
        offsetBackingStore.configure(config);

        // 真正干活的
        Worker worker = new Worker(workerId, time, plugins, config, offsetBackingStore);

        // 使用一个 topic 来存储 connector 与 task 的状态
        StatusBackingStore statusBackingStore = new KafkaStatusBackingStore(time, worker.getInternalValueConverter());
        statusBackingStore.configure(config);

        // 使用一个 topic 来存储 connector config 信息
        ConfigBackingStore configBackingStore = new KafkaConfigBackingStore(worker.getInternalValueConverter(), config);

        // 分布式 connector
        DistributedHerder herder = new DistributedHerder(config, time, worker, statusBackingStore, configBackingStore,
                advertisedUrl.toString());
        final Connect connect = new Connect(herder, rest);
        try {
            // 启动 DistributedHerder 与 jetty server,
            // DistributedHerder 将会启动 connector task
            connect.start();
        } catch (Exception e) {
            log.error("Failed to start Connect", e);
            connect.stop();
        }

        // Shutdown will be triggered by Ctrl-C or via HTTP shutdown request
        connect.awaitStop();
    }
}

```

## 启动一个 connector

ConnectDistributed 类中调用 Connector 类的 start() 函数， 从而执行了 DistributedHerder 的 start() 函数，从而提交了异步的 trick() 函数，代码如下：
```java
// DistributedHerder.java

 @Override
    public void start() {
        this.herderExecutor.submit(this);
    }

    @Override
    public void run() {
        try {
            log.info("Herder starting");

            startServices();

            log.info("Herder started");

            while (!stopping.get()) {
                // 启动任务
                tick();
            }

            halt();

            log.info("Herder stopped");
        } catch (Throwable t) {
            log.error("Uncaught exception in herder work thread, exiting: ", t);
            Exit.exit(1);
        }
    }
```

trick() 在主循环主要做两件事：1）驱动组成员协议，在事件发生时响应重新平衡事件；2）处理针对 leader 的外部请求。herder的所有“实际”工作都是在这个线程中执行的，它保持了同步的直接性，但代价是一些可能阻塞线程的操作（尤其是那些由于重新平衡事件而导致的回调）。主要执行的函数 handleRebalanceCompleted、processConnectorConfigUpdates 最终执行 Worker.java 中的 startConnector 函数。代码如下：


```java
    /**
     * Start a connector managed by this worker.
     *
     * @param connName the connector name.
     * @param connProps the properties of the connector.
     * @param ctx the connector runtime context.
     * @param statusListener a listener for the runtime status transitions of the connector.
     * @param initialState the initial state of the connector.
     * @return true if the connector started successfully.
     */
    public boolean startConnector(
            String connName,
            Map<String, String> connProps,
            ConnectorContext ctx,
            ConnectorStatus.Listener statusListener,
            TargetState initialState
    ) {
        if (connectors.containsKey(connName))
            throw new ConnectException("Connector with name " + connName + " already exists");

        final WorkerConnector workerConnector;
        ClassLoader savedLoader = plugins.currentThreadLoader();
        try {
            // 获取 connector 配置
            final ConnectorConfig connConfig = new ConnectorConfig(plugins, connProps);
            final String connClass = connConfig.getString(ConnectorConfig.CONNECTOR_CLASS_CONFIG);
            log.info("Creating connector {} of type {}", connName, connClass);
            // 实例化 connector
            final Connector connector = plugins.newConnector(connClass);
            workerConnector = new WorkerConnector(connName, connector, ctx, statusListener);
            log.info("Instantiated connector {} with version {} of type {}", connName, connector.version(), connector.getClass());
            savedLoader = plugins.compareAndSwapLoaders(connector);
            // 初始化 connector
            workerConnector.initialize(connConfig);
            workerConnector.transitionTo(initialState);
            Plugins.compareAndSwapLoaders(savedLoader);
        } catch (Throwable t) {
            log.error("Failed to start connector {}", connName, t);
            // Can't be put in a finally block because it needs to be swapped before the call on
            // statusListener
            Plugins.compareAndSwapLoaders(savedLoader);
            statusListener.onFailure(connName, t);
            return false;
        }

        WorkerConnector existing = connectors.putIfAbsent(connName, workerConnector);
        if (existing != null)
            throw new ConnectException("Connector with name " + connName + " already exists");

        log.info("Finished creating connector {}", connName);
        return true;
    }
```

## 启动 connector task

只有在执行 handleRebalanceCompleted() 的时候才会真正的执行 connector task, 代码如下：

```java
/**
     * Start a task managed by this worker.
     *
     * @param id the task ID.
     * @param connProps the connector properties.
     * @param taskProps the tasks properties.
     * @param statusListener a listener for the runtime status transitions of the task.
     * @param initialState the initial state of the connector.
     * @return true if the task started successfully.
     */
    public boolean startTask(
            ConnectorTaskId id,
            Map<String, String> connProps,
            Map<String, String> taskProps,
            TaskStatus.Listener statusListener,
            TargetState initialState
    ) {
        log.info("Creating task {}", id);

        if (tasks.containsKey(id))
            throw new ConnectException("Task already exists in this worker: " + id);

        final WorkerTask workerTask;
        ClassLoader savedLoader = plugins.currentThreadLoader();
        try {
            final ConnectorConfig connConfig = new ConnectorConfig(plugins, connProps);
            String connType = connConfig.getString(ConnectorConfig.CONNECTOR_CLASS_CONFIG);
            ClassLoader connectorLoader = plugins.delegatingLoader().connectorLoader(connType);
            savedLoader = Plugins.compareAndSwapLoaders(connectorLoader);
            final TaskConfig taskConfig = new TaskConfig(taskProps);
            final Class<? extends Task> taskClass = taskConfig.getClass(TaskConfig.TASK_CLASS_CONFIG).asSubclass(Task.class);
            final Task task = plugins.newTask(taskClass);
            log.info("Instantiated task {} with version {} of type {}", id, task.version(), taskClass.getName());

            // By maintaining connector's specific class loader for this thread here, we first
            // search for converters within the connector dependencies, and if not found the
            // plugin class loader delegates loading to the delegating classloader.
            Converter keyConverter = connConfig.getConfiguredInstance(WorkerConfig.KEY_CONVERTER_CLASS_CONFIG, Converter.class);
            if (keyConverter != null)
                keyConverter.configure(connConfig.originalsWithPrefix("key.converter."), true);
            else
                keyConverter = defaultKeyConverter;
            Converter valueConverter = connConfig.getConfiguredInstance(WorkerConfig.VALUE_CONVERTER_CLASS_CONFIG, Converter.class);
            if (valueConverter != null)
                valueConverter.configure(connConfig.originalsWithPrefix("value.converter."), false);
            else
                valueConverter = defaultValueConverter;
            // 构建 connector workerTask
            workerTask = buildWorkerTask(connConfig, id, task, statusListener, initialState, keyConverter, valueConverter, connectorLoader);
            workerTask.initialize(taskConfig);
            Plugins.compareAndSwapLoaders(savedLoader);
        } catch (Throwable t) {
            log.error("Failed to start task {}", id, t);
            // Can't be put in a finally block because it needs to be swapped before the call on
            // statusListener
            Plugins.compareAndSwapLoaders(savedLoader);
            statusListener.onFailure(id, t);
            return false;
        }

        WorkerTask existing = tasks.putIfAbsent(id, workerTask);
        if (existing != null)
            throw new ConnectException("Task already exists in this worker: " + id);
        // 执行 connector task
        executor.submit(workerTask);
        if (workerTask instanceof WorkerSourceTask) {
            sourceTaskOffsetCommitter.schedule(id, (WorkerSourceTask) workerTask);
        }
        return true;
    }
```

WorkerTask 是一个异步方法，方法执行的主逻辑封装在 run() 方法中，代码如下：

```java

@Override
    public void run() {
        ClassLoader savedLoader = Plugins.compareAndSwapLoaders(loader);
        try {
            doRun();
            onShutdown();
        } catch (Throwable t) {
            onFailure(t);

            if (t instanceof Error)
                throw (Error) t;
        } finally {
            Plugins.compareAndSwapLoaders(savedLoader);
            shutdownLatch.countDown();
        }
    }

    public boolean shouldPause() {
        return this.targetState == TargetState.PAUSED;
    }

private void doRun() throws InterruptedException {
        try {
            synchronized (this) {
                if (stopping)
                    return;

                if (targetState == TargetState.PAUSED) {
                    onPause();
                    if (!awaitUnpause()) return;
                }

                statusListener.onStartup(id);
            }
            // 执行 WorkerSinkTask 与 WorkerSourceTask 中的 execute(),
            // 即执行所有 SourceConnector 和 SinkConnector 的运行生命周期
            execute();
        } catch (Throwable t) {
            log.error("Task {} threw an uncaught and unrecoverable exception", id, t);
            log.error("Task is being killed and will not recover until manually restarted");
            throw t;
        } finally {
            doClose();
        }
    }

```

execute() 则是被 WorkerSinkTask 与 WorkerSourceTask 继承重写，用于实现 sink connector 与 source connector 的通用主逻辑，
在 WorkerSinkTask 中的 execute() 逻辑如下：

```java
 @Override
    public void execute() {
        // 初始化 sink connector
        initializeAndStart();
        try {
            while (!isStopping())
                // 迭代不断从 kafka topic 中获取数据，调用 sink connector task 的 
                // put(Collection<SinkRecord> sinkRecords) 方法为 sink connector 
                // 自动提供数据，函数的调用链为： iteration() -> poll(timeoutMs) ->
                // convertMessages(msgs)/deliverMessages() { task.put(new ArrayList<>(messageBatch)); } 
                iteration();
        } finally {
            // Make sure any uncommitted data has been committed and the task has
            // a chance to clean up its state
            closePartitions();
        }
    }
```

WorkerSourceTask 中 execute() 逻辑如下：

```java
 @Override
    public void execute() {
        try {
            task.initialize(new WorkerSourceTaskContext(offsetReader));
            task.start(taskConfig);
            log.info("Source task {} finished initialization and start", this);
            synchronized (this) {
                if (startedShutdownBeforeStartCompleted) {
                    tryStop();
                    return;
                }
                finishedStart = true;
            }

            while (!isStopping()) {
                if (shouldPause()) {
                    onPause();
                    if (awaitUnpause()) {
                        onResume();
                    }
                    continue;
                }

                if (toSend == null) {
                    log.trace("Nothing to send to Kafka. Polling source for additional records");
                    toSend = task.poll();
                }
                if (toSend == null)
                    continue;
                log.debug("About to send " + toSend.size() + " records to Kafka");
                // 将 source connector task 方法 List<SourceRecord> poll() 返回的数据
                // 发送至 kafka topic
                if (!sendRecords())
                    stopRequestedLatch.await(SEND_FAILED_BACKOFF_MS, TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException e) {
            // Ignore and allow to exit.
        } finally {
            // It should still be safe to commit offsets since any exception would have
            // simply resulted in not getting more records but all the existing records should be ok to flush
            // and commit offsets. Worst case, task.flush() will also throw an exception causing the offset commit
            // to fail.
            commitOffsets();
        }
    }
```

所有序列化与反序列化的过程都在，execute() 的主逻辑中被统一处理，不需要用户自己实现。
