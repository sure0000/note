# Classification Propagation

分类传播使与实体关联的分类能够自动与实体的其他相关实体关联,这在处理数据集从其他数据集（如文件中加载数据的表、从表/视图生成的报表等）派生数据时非常有用.

## 用例

考虑下面的血缘，其中来自“hdfs_path”实体的数据被加载到表中，表进一步通过视图提供。

将分类“PII”添加到“hdfs_path”实体时，该分类将传播到血缘路径中的所有受影响实体，包括“employees”表、视图“us_employees”和“uk_employees”，而之后对分类“PII”的任何操作，都将传播到血缘路径中的所有受影响实体。