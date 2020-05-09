## 什么是 IOC、DI？

IOC（Inversion of Control）即“控制反转”，不是什么技术，而是一种设计思想。DI（Dependency Injection）即“依赖注入”，它们是同一个概念的不同角度描述。

IOC 是对象定义其依赖的过程，定义的方式仅能通过构造参数、工厂方法参数，或者是对象被构造成从工厂方法返回之后设定在对象实例上的属性。然后由 IOC 容器在创建 bean 时将这些依赖注入。

## IOC/DI 的好处

使用 DI 原则代码会跟家干净，当对象具有依赖关系时，解耦更加有效，对象不查找其依赖项，也不知道其依赖项的位置或类。因此类变得更加容易测试，特别是当依赖项是接口或抽象基类时。这些接口或抽象类允许在单元测试中使用 sub 或 mock implemenation。

## IOC 容器

org.springframework.context.ApplicationContext

ApplicationContext 接口代表 Spring IOC Container, 并且负责实例化、配置、装配 bean。容器通过读取配置文件元数据获取指令，决定哪个对象被实例化、配置、与装配。

接口说明：给一个应用提供配置的中心化接口，当应用在运行时为只读，但是如果实现类支持也可能被重新加载。

一个 ApplicationContext 提供了：
1. 获取应用组件的 bean 工厂方法(通过继承 ListableBeanFactory)
2. 以通用的方式加载文件的能力（通过继承 ResourceLoader）
3. 发布事件到注册监听器的能力（通过继承 ApplicationEventPublisher）
4. 解析消息、支持国际化的能力（通过继承 MessageSource）
5. 从父 context 继承；子 context 的定义总是优先考虑。也就是说，例如一个单独的父 context 可以被整个 web application 使用，每个servlet 拥有其子 context， 并且子 context 与其他 servlet 相互独立。

## IOC Container 架构

![](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/images/container-magic.png)