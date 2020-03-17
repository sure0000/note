## Components

Spark applications run as independent sets of processes on a cluster, `coordinated by the SparkContext` object in your main program (called the driver program).

<div align="center">
    <img src="../zzzimg/spark/cluster&#32;mode.png" width="50%" />
</div>

1. the SparkContext can connect to several types of cluster managers (either Spark’s own standalone cluster manager, Mesos or YARN), which allocate resources across applications.
2. Once connected, Spark acquires executors on nodes in the cluster, which are processes that run computations and store data for your application.
3. Next, it sends your application code (defined by JAR or Python files passed to SparkContext) to the executors.
4. Finally, SparkContext sends tasks to the executors to run.

**There are several useful things to note about this architecture:**

1. Each application gets its own executor processes, which stay up for the duration of the whole application and run tasks in multiple threads. This has the benefit of isolating applications from each other, on both the scheduling side (each driver schedules its own tasks) and executor side (tasks from different applications run in different JVMs). However, `it also means that data cannot be shared across different Spark applications (instances of SparkContext) without writing it to an external storage system`.
2. Spark is agnostic to the underlying cluster manager. As long as it can acquire executor processes, and these communicate with each other, it is relatively easy to run it even on a cluster manager that also supports other applications (e.g. Mesos/YARN).
3. The driver program must listen for and accept incoming connections from its executors throughout its lifetime (e.g., see spark.driver.port in the network config section). As such, the driver program must be network addressable from the worker nodes.
4. Because the driver schedules tasks on the cluster, it should be run close to the worker nodes, preferably on the same local area network. If you’d like to send requests to the cluster remotely, it’s better to open an RPC to the driver and have it submit operations from nearby than to run a driver far away from the worker nodes.