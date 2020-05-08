## LogManager.scala
log manager 负责 log 文件的创建、检索、清理。所有读写操作都委派给各个 log 实例。
log manager 维护一个或多个文件夹中的 log，新 log 将在包含最少 logs 的文件夹中创建。
后台线程通过定期截断多余的日志段来处理日志保留。

**创建并验证数据文件夹**
```scala
// 创建并检查不在给定下线目录中的给定目录的有效性，特别是：
// 1. 确保目录列表中没有重复项
// 2. 如果不存在，则创建每个目录
// 3. 检查每个路径是否为可读目录
createAndValidateLogDirs(dirs: Seq[File], initialOfflineDirs: Seq[File])
```


**启动后台线程 flush logs and do log cleanup**
```scala
  def startup(): Unit = {
    /* Schedule the cleanup task to delete old logs */
    if (scheduler != null) {
      info("Starting log cleanup with a period of %d ms.".format(retentionCheckMs))
      scheduler.schedule("kafka-log-retention",
                         cleanupLogs _,
                         delay = InitialTaskDelayMs,
                         period = retentionCheckMs,
                         TimeUnit.MILLISECONDS)
      info("Starting log flusher with a default period of %d ms.".format(flushCheckMs))
      scheduler.schedule("kafka-log-flusher",
                         flushDirtyLogs _,
                         delay = InitialTaskDelayMs,
                         period = flushCheckMs,
                         TimeUnit.MILLISECONDS)
      scheduler.schedule("kafka-recovery-point-checkpoint",
                         checkpointLogRecoveryOffsets _,
                         delay = InitialTaskDelayMs,
                         period = flushRecoveryOffsetCheckpointMs,
                         TimeUnit.MILLISECONDS)
      scheduler.schedule("kafka-log-start-offset-checkpoint",
                         checkpointLogStartOffsets _,
                         delay = InitialTaskDelayMs,
                         period = flushStartOffsetCheckpointMs,
                         TimeUnit.MILLISECONDS)
      scheduler.schedule("kafka-delete-logs", // will be rescheduled after each delete logs with a dynamic period
                         deleteLogs _,
                         delay = InitialTaskDelayMs,
                         unit = TimeUnit.MILLISECONDS)
    }
    if (cleanerConfig.enableCleaner)
      cleaner.startup()
  }
```

**获取或者创建log**
```scala
/**
   * If the log already exists, just return a copy of the existing log
   * Otherwise if isNew=true or if there is no offline log directory, create a log for the given topic and the given partition
   * Otherwise throw KafkaStorageException
   *
   * @param topicPartition The partition whose log needs to be returned or created
   * @param config The configuration of the log that should be applied for log creation
   * @param isNew Whether the replica should have existed on the broker or not
   * @param isFuture True iff the future log of the specified partition should be returned or created
   * @throws KafkaStorageException if isNew=false, log is not found in the cache and there is offline log directory on the broker
   */
def getOrCreateLog(topicPartition: TopicPartition, 
                   config: LogConfig,
                   isNew: Boolean = false, 
                   isFuture: Boolean = false)
```

**删除logs**
```scala
// 删除被标记为删除的日志。
// 在`currentDefaultConfig.fileDeleteDelayMs`之后 delete 被调度，删除所有 logs.
// 在此间隔中未被删除的 logs，将在下一次“deleteLogs”迭代中将考虑删除
// 下一个迭代将在第一个未删除日志的剩余时间之后执行。如果不再有'logsToBeDeleted'，
// 'deleteLogs'将在'currentDefaultConfig.fileDeleteDelayMs'之后执行。

private def deleteLogs()
```
