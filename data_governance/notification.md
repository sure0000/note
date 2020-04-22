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