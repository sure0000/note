## 什么是 Bean？

在 Spring 中，构成应用程序的主干并由 Spring IOC 容器管理的对象成为 Bean。 Bean 以及 Bean 之间的依赖关系反映在容器的配置元数据中。

## Bean Definition（Bean 的定义）

bean 的元数据包含一下内容：
1. 包限定类名：通常为被定义的 bean 的具体实现类；
2. Bean 的行为配置元素：定义了 Bean 在 container 中如何行动。（scope,lifecycle,callbacks 等）；
3. Bean 的依赖项，bean 执行其工作所需的其他 bean 的引用；
4. 在新创建的对象中设置的其他配置的设置，如连接池中的连接数。

## Bean 的生成（BeanFactory）

org.springframework.factory.BeanFactory

获取 Spring bean 容器的根接口，这是一个 bean container 的基础视图，进一步的接口可在特殊的目的中获取。如 ListableBeanFactory, ConfigurableBeanFactory。

这个接口由拥有一系列 bean 定义的对象实现，并且每个对象被`唯一的字符名称`标识。根据 bean 的定义，这个 factory 会返回一个`独立的 contianer object`，采取的是`原型设计模式`，
或者一个`单独的共享实例`，采取的是单例模式。具体返回哪种类型的实例由`bean factory configuration`决定。这种方法的重点是 BeanFactory 是一个应用组件的集中化注册中心与配置中心。

通常 BeanFactory 会从配置源加载 bean 的定义(`Bean Definition`)，如 XML 文件，并且使用 beans 包去配置不同的 bean。

与 ListableBeanFactory 中的方法相比，本接口的所有操作也会检查父工厂是否为 `HierarchicalBeanFactory`， 如果在此工厂中找不到某个 bean，会直接询问父工厂。本工厂实例中的 Bean 
支持充血父工厂中相同名称的 bean。

***BeanFactory*** 提供了配置框架与基础功能。