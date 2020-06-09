guice 是轻量级的依赖注入框架，依赖注入是类将他们的依赖声明为参数，而不是直接创建依赖的一种设计模式。

## guice 的核心概念

### @Inject construtor

java 类的构造器被`@Inject`注解，可以被 Guice 通过一种被称作 `constructor injection` 的进程调用，在此过程中 Guice 会创建并提供构造器参数。例如：
```java
class Greeter {
  private final String message;
  private final int count;

  // Greeter declares that it needs a string message and an integer
  // representing the number of time the message to be printed.
  // The @Inject annotation marks this constructor as eligible to be used by
  // Guice.
  @Inject
  Greeter(@Message String message, @Count int count) {
    this.message = message;
    this.count = count;
  }

  void sayHello() {
    for (int i=0; i < count; i++) {
      System.out.println(message);
    }
  }
}
```

### Guice modules

应用包含在其他对象上声明依赖关系的对象，并且这些依赖关系形成图。`Guice modules` 允许应用指定如何满足这些依赖关系。

```java
/**
 * Guice module that provides bindings for message and count used in
 * {@link Greeter}.
 */
import com.google.inject.Provides;

class DemoModule extends AbstractModule {
  @Provides
  @Count
  static Integer provideCount() {
    return 3;
  }

  @Provides
  @Message
  static String provideMessage() {
    return "hello world";
  }
}
```

### Guice injectors

injector 将所有 modules 作为构造器参数准备好，对需要获取实例的类进行注入。

```java
public final class MyWebServer {
  public void start() {
    ...
  }

  public static void main(String[] args) {
    // Creates an injector that has all the necessary dependencies needed to
    // build a functional server.
    Injector injector = Guice.createInjector(
        new RequestLoggingModule(),
        new RequestHandlerModule(),
        new AuthenticationModule(),
        new DatabaseModule(),
        ...);
    // Bootstrap the application by creating an instance of the server then
    // start the server to handle incoming requests.
    injector.getInstance(MyWebServer.class)
        .start();
  }
}
```

### bindings

injectors 的工作是装配对象的依赖，要创建绑定，请继承`AbstractModule`并重写其`configure`方法。在方法体中，调用`bind()`指定每个绑定。这些方法是类型检查的，因此如果使用错误的类型，编译器可以报告错误。创建模块后，将它们作为参数传递给`Guice.createInjector()`以构建注入器。binding 的类型有： linked bindings, instance bindings, @Provides methods, provider bindings, constructor bindings and untargetted bindings.
