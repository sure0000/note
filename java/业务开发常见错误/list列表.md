第一，想当然认为，Arrays.asList 和 List.subList 得到的 List 是普通的、独立的 ArrayList，在使用时出现各种奇怪的问题。

- Arrays.asList 得到的是 Arrays 的内部类 ArrayList，List.subList 得到的是 ArrayList 的内部类 SubList，不能把这两个内部类转换为 ArrayList 使用。
- Arrays.asList 直接使用了原始数组，可以认为是共享“存储”，而且不支持增删元素；List.subList 直接引用了原始的 List，也可以认为是共享“存储”，而且对原始 List 直接进行结构性修改会导致 SubList 出现异常。
- 对 Arrays.asList 和 List.subList 容易忽略的是，新的 List 持有了原始数据的引用，可能会导致原始数据也无法 GC 的问题，最终导致 OOM。


第二，想当然认为，Arrays.asList 一定可以把所有数组转换为正确的 List。当传入基本类型数组的时候，List 的元素是数组本身，而不是数组中的元素。

第三，想当然认为，内存中任何集合的搜索都是很快的，结果在搜索超大 ArrayList 的时候遇到性能问题。我们考虑利用 HashMap 哈希表随机查找的时间复杂度为 O(1) 这个特性来优化性能，不过也要考虑 HashMap 存储空间上的代价，要平衡时间和空间。

第四，想当然认为，链表适合元素增删的场景，选用 LinkedList 作为数据结构。在真实场景中读写增删一般是平衡的，而且增删不可能只是对头尾对象进行操作，可能在 90% 的情况下都得不到性能增益，建议使用之前通过性能测试评估一下。