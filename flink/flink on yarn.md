## Config 

1. You can reference a Hadoop configuration by setting the `environment variable` `HADOOP_CONF_DIR`.

```bash
HADOOP_CONF_DIR=/path/to/etc/hadoop
```
Referencing the HDFS configuration in the Flink configuration is deprecated.

2. Adding the Hadoop classpath to Flink
```bash
export HADOOP_CLASSPATH=`hadoop classpath`
```
3. Putting the required jar files into /lib directory of the Flink distribution
   - [Flink-shaded](https://flink.apache.org/downloads.html#flink-shaded)
   - sli4j

## Submit job with Yarn

**Flink YARN Session**

> Please note that the Client requires the YARN_CONF_DIR or HADOOP_CONF_DIR environment variable to be set to read the YARN and HDFS configuration.

```bash
# start a Yarn session cluster where each task manager is started with 8 GB of memory and 32 processing slots
./bin/yarn-session.sh -tm 8192 -s 32

Usage:
   Optional
     -D <arg>                        Dynamic properties
     -d,--detached                   Start detached
     -jm,--jobManagerMemory <arg>    Memory for JobManager Container with optional unit (default: MB)
     -nm,--name                      Set a custom name for the application on YARN
     -at,--applicationType           Set a custom application type on YARN
     -q,--query                      Display available YARN resources (memory, cores)
     -qu,--queue <arg>               Specify YARN queue.
     -s,--slots <arg>                Number of slots per TaskManager
     -tm,--taskManagerMemory <arg>   Memory per TaskManager Container with optional unit (default: MB)
     -z,--zookeeperNamespace <arg>   Namespace to create the Zookeeper sub-paths for HA mode
```

**Run a single Flink job on YARN**

```bash
./bin/flink run ./examples/batch/WordCount.jar \
       --input hdfs:///..../LICENSE-2.0.txt --output hdfs:///.../wordcount-result.txt
```