## 特征

**元数据类型与实例（Metadata types & instances）**

- 各种 Hadoop 与非 Hadoop 元数据的预定义类型
- 为需要管理的元数据定义新类型
- 类型可以有原始属性、复杂属性、对象引用；可以从其他类型继承
- 类型的实例被称作实体（entities）,捕获元数据对象详细信息及其关系
- 与类型和实例一起工作的 rest api 允许更容易的集成

**分类**

- 动态创建分类的能力，如，PII, EXPIRES_ON, DATA_QUALITY, SENSITIVE
- 分类可以包含属性，如，expiry_date 属性在 EXPIRE_ON 分类中
- 实体可以与多个分类相关联，从而实现更容易的发现和安全实施
- 通过血缘传递分类-当数据在经历各种处理时自动确保类别跟随数据

**血缘**

- 直观的用户界面，用于查看数据在不同进程中的血缘关系
- REST APIs 获取、更新血缘

**搜索发现**

- 按类型、分类、属性值或纯文本搜索实体的直观用户界面
- 按复杂条件搜索的丰富 rest api
- 用于搜索实体的类SQL查询语言-领域特定语言（DSL）

**安全与数据屏蔽**

- 元数据访问的细粒度安全性，允许对实体实例和操作（如添加/更新/删除分类）的访问进行控制
- 与Apache Ranger的集成支持基于Apache Atlas中与实体关联的分类对数据访问进行授权/数据屏蔽。例如：
  - who can access data classified as PII, SENSITIVE
  - customer-service users can only see last 4 digits of columns classified as NATIONAL_ID


## 架构

<div align="center">
    <img src="../../zzzimg/hadoop/atlas-architecture.png" width="50%" />
</div>

### core

**Type 系统**

atlas 允许用户为他们想要管理的元数据对象定义一个模型，模型由被称作`type`的定义组成。type 的实例被称作 `entities`，代表了管理的实际元数据。`type 系统`是一个允许用户定义和管理 `type` 与 `entities` 的一个组件。

**Graph Engine**

在内部，Atlas使用图形模型保存它管理的元数据对象，这种方式提供了极大的灵活性，并且能够有效的处理不同元数据对象之间丰富的关系。`Graph Engine` 组件负责在Atlas类型系统的类型和实体之间转换，以及底层的图形持久化模型。为了管理图对象，图引擎也为元数据对象创建了合适的索引，以便他们可以方便的被检索。atlas 使用 JanusGraph 存储元数据对象。

**ingest and export **

将源数据导入导出 Atlas 系统。

**Integration**

用户可以通过两种方式来管理元数据：

- `API`: 用户可以使用 REST API 的方式来管理 types 与 entities，这也是 atlas 管理 type 与 entities 的主要方式。
- 除了API之外，用户还可以选择使用基于Kafka的消息接口与Atlas集成。这对于将元数据对象传递到Atlas以及使用Atlas中的元数据更改事件（使用这些事件可以构建应用程序）都很有用。如果希望使用与Atlas更松散耦合的集成，则消息接口特别有用，这样可以实现更好的可伸缩性、可靠性等。Atlas使用Apache Kafka作为通知服务器，用于钩子和元数据通知事件的下游使用者之间的通信。事件由hooks和Atlas为不同的Kafka主题编写。

**元数据源**

atlas 目前支持一下数据源的交互：
- HBase
- Hive
- Sqoop
- Storm
- Kafka

集成意味着两件事：Atlas本机定义了元数据模型来表示这些组件的对象。Atlas提供了一些组件来从这些组件中摄取元数据对象（在某些情况下是实时的或批处理模式）。


