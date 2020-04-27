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