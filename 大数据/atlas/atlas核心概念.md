## Types

Type 在 Atlas 中是定义了一个特定的元数据对象是如何被存取的。类似于 java 中的 Class 或者数据库中的表结构。一个 Hive 表的定义如下：
```yaml
Name:         hive_table
TypeCategory: Entity
SuperTypes:   DataSet
Attributes:
    name:             string
    db:               hive_db
    owner:            string
    createTime:       date
    lastAccessTime:   date
    comment:          string
    retention:        int
    sd:               hive_storagedesc
    partitionKeys:    array<hive_column>
    aliases:          array<string>
    columns:          array<hive_column>
    parameters:       map<string>
    viewOriginalText: string
    viewExpandedText: string
    tableType:        string
    temporary:        boolean
```

- name: atlas 中的唯一标识
- type 拥有原子类型，类似 java 中的原子类型，如下：
  - Primitive metatypes: boolean, byte, short, int, long, float, double, biginteger, bigdecimal, string, date
  - Enum metatypes
  - Collection metatypes: array, map
  - Composite metatypes: Entity, Struct, Classification, Relationship
- Entity & Classification 可以从其他类型继承，称作 “supertype”,本例中 hive 表继承于 DataSet 类型
- ‘Entity’, ‘Struct’, ‘Classification’ or 'Relationship' 可以拥有多个属性。


## Entities

Entities 类似于 java 中类实例化之后的对象，例子如下：
```yaml
guid:     "9ba387dd-fa76-429c-b791-ffc338d3c91f"
typeName: "hive_table"
status:   "ACTIVE"
values:
    name:             “customers”
    db:               { "guid": "b42c6cfc-c1e7-42fd-a9e6-890e0adf33bc",
                        "typeName": "hive_db"
                      }
    owner:            “admin”
    createTime:       1490761686029
    updateTime:       1516298102877
    comment:          null
    retention:        0
    sd:               { "guid": "ff58025f-6854-4195-9f75-3a3058dd8dcf",
                        "typeName":"hive_storagedesc"
                      }
    partitionKeys:    null
    aliases:          null
    columns:          [ { "guid": "65e2204f-6a23-4130-934a-9679af6a211f",
                          "typeName": "hive_column" },
                        { "guid": "d726de70-faca-46fb-9c99-cf04f6b579a6",
                          "typeName": "hive_column" },
                          ...
                      ]
    parameters:       { "transient_lastDdlTime": "1466403208"}
    viewOriginalText: null
    viewExpandedText: null
    tableType:        “MANAGED_TABLE”
    temporary:        false
```

- GUID: 一个 entity 实例的唯一标识，当 entity 类型的对象创建时，由 atlas server 生成；
- 属性值将根据属性的数据类型而定，entity 类型的属性会拥有 type AtlasObjectId 的值。

Entity and Struct 都是有其他类型的属性组成的，它们的区别如下：
- Entity 类型的实例拥有一个唯一标识的GUID, 可以从其他 entities 中引用，如 `hive_db` entity 是从`hive_table` entity 引用的。 


## Attributes

Atlas中的 Attributes 有更多的 properties，这些 properties 定义了更多与类型系统相关的概念。

An attribute has the following properties:
```yaml
name:        string,
typeName:    string,
isOptional:  boolean,
isIndexable: boolean,
isUnique:    boolean,
cardinality: enum
```

- name: attribute 名称
- typeName: attribute 原子类型的名称
- isComposite: 
  - 此标志表示建模的一个方面。如果一个属性被定义为composite，则意味着它不能拥有独立于包含它的实体的生命周期。这个概念的一个很好的例子是构成 hive 表一部分的一组列。由于列在 hive 表之外没有意义，因此它们被定义为复合属性。
  - composite attribute 必须伴随 entity 类型创建
- isIndexable：是否可以被索引
- isUnique：
  - 如果指定为唯一，则意味着在JanusGraph中为此属性创建了一个特殊索引，该索引允许基于等式的查找。
  - 具有此标志的真值的任何属性都被视为主键，以将此实体与其他实体区分开来。因此，应注意确保该属性确实对现实世界中的唯一属性建模。
- multiplicity： 指示此属性是必需的、可选的还是可以是多值的。如果实体对属性值的定义与类型定义中的多重性声明不匹配，则这将违反约束，实体添加将失败。因此，此字段可用于定义元数据信息的某些约束。

示例如下：
```yaml
db:
    "name":        "db",
    "typeName":    "hive_db",
    "isOptional":  false,
    "isIndexable": true,
    "isUnique":    false,
    "cardinality": "SINGLE"

columns:
    "name":        "columns",
    "typeName":    "array<hive_column>",
    "isOptional":  optional,
    "isIndexable": true,
    “isUnique":    false,
    "constraints": [ { "type": "ownedRef" } ]
```

## 系统指定类型

Atlas 有一些预定义的系统类型，如下：
- Referenceable：此类型表示可以使用名为qualifiedName的唯一属性可被搜索的所有 entities
- Asset: 此类型继承Referenceable，并添加了名称、说明和所有者等属性。Name是一个必需的属性（is optional=false），其他属性是可选的。

Referenceable和Asset的目的是为建模者提供在定义和查询自己类型的实体时增强一致性的方法。有了这些固定的属性集，应用程序和用户界面就可以对默认情况下类型的属性进行基于约定的假设。

- Infrastructure: 这种类型继承Asset，通常可以用作基础元数据对象（如集群、主机等）的通用超级类型。
- DataSet: 此类型继承Referenceable。从概念上讲，它可以用来表示存储数据的类型。扩展数据集的类型可以期望有一个Schema，因为它们将有一个定义该数据集属性的属性。
- Process: 这种类型继承Asset,从概念上讲，它可以用来表示任何数据转换操作。例如，将包含原始数据的hive_table转换为存储某些聚合的另一个hive_table的ETL进程可以是继承 Process type 的特定类型。Process type 有两个特定属性：输入和输出。输入和输出都是DataSet实体的数组。因此，Process type 的实例可以使用这些输入和输出来捕获 DataSet 血缘是如何演变。


# Classification Propagation

分类传播使与实体关联的分类能够自动与实体的其他相关实体关联,这在处理数据集从其他数据集（如文件中加载数据的表、从表/视图生成的报表等）派生数据时非常有用.

## 用例

考虑下面的血缘，其中来自“hdfs_path”实体的数据被加载到表中，表进一步通过视图提供。

将分类“PII”添加到“hdfs_path”实体时，该分类将传播到血缘路径中的所有受影响实体，包括“employees”表、视图“us_employees”和“uk_employees”，而之后对分类“PII”的任何操作，都将传播到血缘路径中的所有受影响实体。


# Glossary

词汇表为业务用户提供适当的词汇表，并允许术语（单词）相互关联和分类，以便在不同的上下文中理解它们。然后，这些术语可以映射到诸如数据库、表、列等资产。这有助于抽象与存储库相关的技术术语，并允许用户发现/使用他们更熟悉的词汇表中的数据。

## 用例

- 能够使用自然术语（技术术语和/或业务术语）定义丰富的词汇表词汇。
- 能够在语义上把术语联系起来。
- 能够将资产映射到词汇表术语。
- 能够按类别组织这些术语。这将为术语添加更多上下文。
- 允许类别按层次结构排列-以表示更广泛和更精细的范围。
- 将术语表术语与元数据分开管理。

## What is a Glossary term ?

term 是一个对企业有用的词。为了使 terms 有用和有意义，它们需要围绕其用途和上下文进行分组.Atlas中的 terms 必须具有唯一的限定名，可以有同名的 term，但它们不能属于同一 glossary.

## What is a Glossary category ?

类别是组织 terms 的一种方式，以便可以丰富术语的上下文。类别可能包含层次结构，也可能不包含层次结构，即子类别层次结构。类别的qualifiedName是使用其在词汇表中的分层位置派生的，例如category name.parent category qualifiedName。当发生任何层次结构更改（例如添加父类别、删除父类别或更改父类别）时，将更新此限定名称。


# Notifications

## Notifications from Apache Atlas

Apache Atlas向名为Atlas_ENTITIES的Kafka主题发送有关元数据更改的通知。对元数据更改感兴趣的应用程序可以监视这些通知。例如，Apache Ranger处理这些通知以根据分类授权数据访问。

`ApacheAtlas1.0发送以下元数据操作的通知。`

```yaml
ENTITY_CREATE:         sent when an entity instance is created
      ENTITY_UPDATE:         sent when an entity instance is updated
      ENTITY_DELETE:         sent when an entity instance is deleted
      CLASSIFICATION_ADD:    sent when classifications are added to an entity instance
      CLASSIFICATION_UPDATE: sent when classifications of an entity instance are updated
      CLASSIFICATION_DELETE: sent when classifications are removed from an entity instance
```

`通知包含以下数据`

```yaml
AtlasEntity  entity;
   OperationType operationType;
   List<AtlasClassification>  classifications;
```

## Notifications to Apache Atlas

Apache Atlas可以通过通知名为Atlas_HOOK的Kafka主题来通知元数据更改和沿袭。Apache Hive/Apache HBase/Apache Storm/Apache Sqoop的Atlas钩子使用此机制将感兴趣的事件通知Apache Atlas。

```yaml
ENTITY_CREATE            : create an entity. For more details, refer to Java class HookNotificationV1.EntityCreateRequest
ENTITY_FULL_UPDATE       : update an entity. For more details, refer to Java class HookNotificationV1.EntityUpdateRequest
ENTITY_PARTIAL_UPDATE    : update specific attributes of an entity. For more details, refer to HookNotificationV1.EntityPartialUpdateRequest
ENTITY_DELETE            : delete an entity. For more details, refer to Java class HookNotificationV1.EntityDeleteRequest
ENTITY_CREATE_V2         : create an entity. For more details, refer to Java class HookNotification.EntityCreateRequestV2
ENTITY_FULL_UPDATE_V2    : update an entity. For more details, refer to Java class HookNotification.EntityUpdateRequestV2
ENTITY_PARTIAL_UPDATE_V2 : update specific attributes of an entity. For more details, refer to HookNotification.EntityPartialUpdateRequestV2
ENTITY_DELETE_V2         : delete one or more entities. For more details, refer to Java class HookNotification.EntityDeleteRequestV2
```