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

options

```
Options for yarn-cluster mode:
     -d,--detached                        If present, runs the job in detached
                                          mode
     -m,--jobmanager <arg>                Address of the JobManager (master) to
                                          which to connect. Use this flag to
                                          connect to a different JobManager than
                                          the one specified in the
                                          configuration.
     -sae,--shutdownOnAttachedExit        If the job is submitted in attached
                                          mode, perform a best-effort cluster
                                          shutdown when the CLI is terminated
                                          abruptly, e.g., in response to a user
                                          interrupt, such as typing Ctrl + C.
     -yat,--yarnapplicationType <arg>     Set a custom application type for the
                                          application on YARN
     -yD <property=value>                 use value for given property
     -yd,--yarndetached                   If present, runs the job in detached
                                          mode (deprecated; use non-YARN
                                          specific option instead)
     -yh,--yarnhelp                       Help for the Yarn session CLI.
     -yid,--yarnapplicationId <arg>       Attach to running YARN session
     -yj,--yarnjar <arg>                  Path to Flink jar file
     -yjm,--yarnjobManagerMemory <arg>    Memory for JobManager Container with
                                          optional unit (default: MB)
     -yn,--yarncontainer <arg>            Number of YARN container to allocate
                                          (=Number of Task Managers)
     -ynl,--yarnnodeLabel <arg>           Specify YARN node label for the YARN
                                          application
     -ynm,--yarnname <arg>                Set a custom name for the application
                                          on YARN
     -yq,--yarnquery                      Display available YARN resources
                                          (memory, cores)
     -yqu,--yarnqueue <arg>               Specify YARN queue.
     -ys,--yarnslots <arg>                Number of slots per TaskManager
     -yst,--yarnstreaming                 Start Flink in streaming mode
     -yt,--yarnship <arg>                 Ship files in the specified directory
                                          (t for transfer)
     -ytm,--yarntaskManagerMemory <arg>   Memory per TaskManager Container with
                                          optional unit (default: MB)
     -yz,--yarnzookeeperNamespace <arg>   Namespace to create the Zookeeper
                                          sub-paths for high availability mode
     -z,--zookeeperNamespace <arg>        Namespace to create the Zookeeper
                                          sub-paths for high availability mode
```