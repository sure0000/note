## awt Exception

**solution**

modify the shell of flink in file `bin/flink`, add `-Djava.awt.headless=true`

original command:
```bash
exec $JAVA_RUN $JVM_ARGS "${log_setting[@]}" -classpath "`manglePathList "$CC_CLASSPATH:$INTERNAL_HADOOP_CLASSPATHS"`" org.apache.flink.client.cli.CliFrontend "$@"
```

after modification:
```bash
exec $JAVA_RUN -Djava.awt.headless=true $JVM_ARGS "${log_setting[@]}" -classpath "`manglePathList "$CC_CLASSPATH:$INTERNAL_HADOOP_CLASSPATHS"`" org.apache.flink.client.cli.CliFrontend "$@"
```

## java-jsch can't find environment variable

java-jsch execute command by non login and non interactive, so can't get the environment variable in `/etc/profile`, we should set environment variable in `~/.bashrc`.

## java-jsch can not execute shell 

use command: `nohup  command >file 2>&1 &`

[java Test](../flink/Test.java)