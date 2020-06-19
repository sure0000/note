```yaml
env {
  execution.parallelism = 1
}

source {
    FakeSourceStream {
      result_table_name = "fake"
      field_name = "name,age"
    }
}

transform {
    sql {
      sql = "select name,age from fake"
    }
}

sink {
    elasticsearch {
        hosts = ["10.10.77.153:9200"]
        index = "waterdrop2"
    }
}
```