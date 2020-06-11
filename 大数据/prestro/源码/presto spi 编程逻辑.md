java spi 是一种 接口 + 策略模式 + 配置文件 的设计模式，通过定制相同的接口，由不同的实现来提供不同的服务。关键是通过配置文件找到接口对应的实现类，并通过类加载器加载到内存中。[深入理解SPI机制](https://www.jianshu.com/p/3a3edbcd8f24)


presto 插件管理模块：`io.prestosql.server.Server#doStart`

```java
injector.getInstance(PluginManager.class).loadPlugins();
```

加载插件的核心代码: `io.prestosql.server.PluginManager#loadPlugin(io.prestosql.server.PluginClassLoader)`
```java
    private void loadPlugin(PluginClassLoader pluginClassLoader)
    {
        // 加载插件实现类
        ServiceLoader<Plugin> serviceLoader = ServiceLoader.load(Plugin.class, pluginClassLoader);
        List<Plugin> plugins = ImmutableList.copyOf(serviceLoader);
        checkState(!plugins.isEmpty(), "No service providers of type %s", Plugin.class.getName());
        for (Plugin plugin : plugins) {
            log.info("Installing %s", plugin.getClass().getName());
            installPlugin(plugin, pluginClassLoader::duplicate);
        }
    }
```

安装插件核心代码: `io.prestosql.server.PluginManager#installPluginInternal`
```java
    private void installPluginInternal(Plugin plugin, Supplier<ClassLoader> duplicatePluginClassLoaderFactory)
    {
        for (BlockEncoding blockEncoding : plugin.getBlockEncodings()) {
            log.info("Registering block encoding %s", blockEncoding.getName());
            metadataManager.addBlockEncoding(blockEncoding);
        }

        for (Type type : plugin.getTypes()) {
            log.info("Registering type %s", type.getTypeSignature());
            metadataManager.addType(type);
        }

        for (ParametricType parametricType : plugin.getParametricTypes()) {
            log.info("Registering parametric type %s", parametricType.getName());
            metadataManager.addParametricType(parametricType);
        }

        for (ConnectorFactory connectorFactory : plugin.getConnectorFactories()) {
            log.info("Registering connector %s", connectorFactory.getName());
            connectorManager.addConnectorFactory(connectorFactory, duplicatePluginClassLoaderFactory);
        }

        for (Class<?> functionClass : plugin.getFunctions()) {
            log.info("Registering functions from %s", functionClass.getName());
            metadataManager.addFunctions(extractFunctions(functionClass));
        }

        for (SessionPropertyConfigurationManagerFactory sessionConfigFactory : plugin.getSessionPropertyConfigurationManagerFactories()) {
            log.info("Registering session property configuration manager %s", sessionConfigFactory.getName());
            sessionPropertyDefaults.addConfigurationManagerFactory(sessionConfigFactory);
        }

        for (ResourceGroupConfigurationManagerFactory configurationManagerFactory : plugin.getResourceGroupConfigurationManagerFactories()) {
            log.info("Registering resource group configuration manager %s", configurationManagerFactory.getName());
            resourceGroupManager.addConfigurationManagerFactory(configurationManagerFactory);
        }

        for (SystemAccessControlFactory accessControlFactory : plugin.getSystemAccessControlFactories()) {
            log.info("Registering system access control %s", accessControlFactory.getName());
            accessControlManager.addSystemAccessControlFactory(accessControlFactory);
        }

        for (PasswordAuthenticatorFactory authenticatorFactory : plugin.getPasswordAuthenticatorFactories()) {
            log.info("Registering password authenticator %s", authenticatorFactory.getName());
            passwordAuthenticatorManager.addPasswordAuthenticatorFactory(authenticatorFactory);
        }

        for (CertificateAuthenticatorFactory authenticatorFactory : plugin.getCertificateAuthenticatorFactories()) {
            log.info("Registering certificate authenticator %s", authenticatorFactory.getName());
            certificateAuthenticatorManager.addCertificateAuthenticatorFactory(authenticatorFactory);
        }

        for (EventListenerFactory eventListenerFactory : plugin.getEventListenerFactories()) {
            log.info("Registering event listener %s", eventListenerFactory.getName());
            eventListenerManager.addEventListenerFactory(eventListenerFactory);
        }

        for (GroupProviderFactory groupProviderFactory : plugin.getGroupProviderFactories()) {
            log.info("Registering group provider %s", groupProviderFactory.getName());
            groupProviderManager.addGroupProviderFactory(groupProviderFactory);
        }
    }
```

presto 主要使用以下类来对 plugin 进行管理：

- `io.prestosql.connector.ConnectorManager`
- `io.prestosql.metadata.MetadataManager`
- `io.prestosql.execution.resourcegroups.ResourceGroupManager`
- `io.prestosql.security.AccessControlManager`
- `io.prestosql.server.security.PasswordAuthenticatorManager`
- `io.prestosql.server.security.CertificateAuthenticatorManager`
- `io.prestosql.eventlistener.EventListenerManager`
- `io.prestosql.security.GroupProviderManager`

根据 SPI 模式来自定义插件开发，需要遵守官方给出的几个规则，具体规则参考官方文档，[presto SPI 开发](https://github.com/prestosql/presto/blob/master/presto-docs/src/main/sphinx/develop/spi-overview.rst)，需要注意的是打包方式使用官方指定的 `presto-plugin`,主要是用于创建 classpath 下的 META-INF/services/io.prestosql.spi.Plugin 文件，即在maven 中使用 `<packaging>presto-plugin</packaging>`。




