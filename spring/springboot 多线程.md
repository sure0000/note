Spring中实现多线程，其实非常简单，只需要在配置类中添加`@EnableAsync`就可以使用多线程。在希望执行的并发方法中使用`@Async`就可以定义一个线程任务。通过spring给我们提供的`ThreadPoolTaskExecutor`就可以使用线程池。


第一步，先在Spring Boot主类中定义一个线程池

```java
@Configuration
@EnableAsync  // 启用异步任务
public class AsyncConfiguration {
    // 声明一个线程池(并指定线程池的名字)
    @Bean("taskExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //核心线程数5：线程池创建时候初始化的线程数
        executor.setCorePoolSize(5);
        //最大线程数5：线程池最大的线程数，只有在缓冲队列满了之后才会申请超过核心线程数的线程
        executor.setMaxPoolSize(5);
        //缓冲队列500：用来缓冲执行任务的队列
        executor.setQueueCapacity(500);
        //允许线程的空闲时间60秒：当超过了核心线程出之外的线程在空闲时间到达之后会被销毁
        executor.setKeepAliveSeconds(60);
        //线程池名的前缀：设置好了之后可以方便我们定位处理任务所在的线程池
        executor.setThreadNamePrefix("DailyAsync-");
        executor.initialize();
        return executor;
    }
}
```

第二步，使用线程池

```java
@Service
public class GitHubLookupService {

    private static final Logger logger = LoggerFactory.getLogger(GitHubLookupService.class);

    @Autowired
    private RestTemplate restTemplate;

    // 这里进行标注为异步任务，在执行此方法的时候，会单独开启线程来执行(并指定线程池的名字)
    // 该方法的返回类型是 CompleetableFuture 而不是 String，这是任何异步服务的要求。
    @Async("taskExecutor")
    public CompletableFuture<String> findUser(String user) throws InterruptedException {
        logger.info("Looking up " + user);
        String url = String.format("https://api.github.com/users/%s", user);
        String results = restTemplate.getForObject(url, String.class);
        // Artificial delay of 3s for demonstration purposes
        Thread.sleep(3000L);
        return CompletableFuture.completedFuture(results);
    }
}
```


**异步方法和调用方法一定要写在不同的类中 ,如果写在一个类中,是没有效果的！**