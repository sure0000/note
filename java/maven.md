maven 依赖打包 本地安装



```xml
<plugin>
  <artifactId>maven-assembly-plugin</artifactId>
  <version>3.1.1</version>
  <configuration>
    <descriptorRefs>
      <descriptorRef>jar-with-dependencies</descriptorRef>
    </descriptorRefs>
  </configuration>
</plugin>
```


`mvn package assembly:single`

安装本地包

`mvn install:install-file -Dfile=path -DgroupId=groupId -DartifactId=artifactId -Dversion=1.11 -Dpackaging=jar`
