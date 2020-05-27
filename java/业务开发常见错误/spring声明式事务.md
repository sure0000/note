

@Transactional 生效原则:

1. 除非特殊配置（比如使用 AspectJ 静态织入实现 AOP），否则只有定义在 public 方法上的 @Transactional 才能生效。原因是，Spring 默认通过动态代理的方式实现 AOP，对目标方法进行增强，private 方法无法代理到，Spring 自然也无法动态增强事务处理逻辑。

2. 必须通过代理过的类从外部调用目标方法才能生效。

3. 事务即便生效也不一定能回滚


> https://time.geekbang.org/column/article/213295