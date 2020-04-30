## Array/List/Set/Map

**Array**

数组是大小固定的，并且同一个数组只能存放类型一样的数据（基本类型/引用类型）

**Set**

Set是最简单的一种集合。集合中的对象不按特定的方式排序，并且没有重复对象。 　　
Set接口主要实现了两个实现类： 　
- HashSet : HashSet类按照哈希算法来存取集合中的对象，存取速度比较快 　　
- TreeSet : TreeSet类实现了SortedSet接口，能够对集合中的对象进行排序。

**List**

List的特征是其元素以线性方式存储，集合中可以存放重复对象。List接口主要实现类包括： 　　
- ArrayList() : 代表长度可以改变的数组。可以对元素进行随机的访问，向ArrayList()中插入与与删除元素的速度慢
- LinkedList(): 在实现中采用链表数据结构。插入和删除速度快，访问速度慢。

**Map**

Map 是一种把键对象和值对象映射的集合，它的每一个元素都包含一对键对象和值对象。Map没有继承于Collection接口,从Map集合中检索元素时，只要给出键对象，就会返回对应的值对象。

## HashTable/HashMap/ConcurrentHashMap

**Hashtable**

- 底层数组+链表实现，无论key还是value都不能为null，线程安全，实现线程安全的方式是在修改数据时锁住整个HashTable，效率低。
- 初始size为11，扩容：newsize = oldsize\*2+1
- 负载因子为 0.75f
- 计算index的方法：index = (hash & 0x7FFFFFFF) % tab.length

```java
// 锁住整个table
public synchronized V get(Object key) {    
    Entry<?,?> tab[] = table;    
    int hash = key.hashCode();    
    int index = (hash & 0x7FFFFFFF) % tab.length;    
    for (Entry<?,?> e = tab[index] ; e != null ; e = e.next) {
        if ((e.hash == hash) && e.key.equals(key)) { 
            return (V)e.value;        
        }    
    }    
    return null;
}

// 锁住整个table
public synchronized V put(K key, V value) {    
    // Make sure the value is not null    
    if (value == null) {        
        throw new NullPointerException();    
    }    
    // Makes sure the key is not already in the hashtable.    
    Entry<?,?> tab[] = table;    
    int hash = key.hashCode();    
    int index = (hash & 0x7FFFFFFF) % tab.length;   
    Entry<K,V> entry = (Entry<K,V>)tab[index];    
    for(; entry != null ; entry = entry.next) {        
        if ((entry.hash == hash) && entry.key.equals(key)) {
            V old = entry.value;            
            entry.value = value;            
            return old;        
         }    
     }    
     addEntry(hash, key, value, index);    
     return null;}
```

**HashMap**

> https://www.cnblogs.com/jingpeng77/p/12449308.html

***Brief introduction***

`HashMap` is roughly equivalent to `Hashtable`， except that `HashMap` is `unsynchronized` and ` permits nulls`. This class makes no guarantees as to the order of the map; in particular, it does not guarantee that the order will remain constant over time.

Iteration over collection views requires time proportional to the "capacity" of the HashMap instance (the number of buckets) plus its size (the number of key-value mappings).  Thus, it's very important not to set the initial capacity too high (or the load factor too low) if iteration performance is important.

If many mappings are to be stored in a `HashMap` instance, creating it with a sufficiently large capacity will allow the mappings to be stored more efficiently than letting it perform automatic rehashing as needed to grow the table.  Note that using many keys with the same` {@code hashCode()}` is a sure way to slow down performance of any hash table. To ameliorate impact, when keys are `{@link Comparable}`, this class may use comparison order among keys to help break ties.

The iterators returned by all of this class's "collection view methods" are `fail-fast`: if the map is structurally modified at any time after the iterator is created, in any way except through the iterator's own `remove` method, the iterator will throw a `{@link ConcurrentModificationException}`.  Thus, in the face of concurrent modification, the iterator fails quickly and cleanly, rather than risking arbitrary, non-deterministic behavior at an undetermined time in the future.

***implementation notes***

This map usually acts as a binned (bucketed) hash table, but when bins get too large, they are transformed into bins of TreeNodes, each structured similarly to those in `java.util.TreeMap`. Most methods try to use normal bins, but relay to TreeNode methods when applicable (simply by checking instanceof a node).  Bins of TreeNodes may be traversed and used like any others, but additionally support faster lookup when overpopulated. However, since the vast majority of bins in normal use are not overpopulated, checking for existence of tree bins may be delayed in the course of table methods.

```java
// put 
public V put(K key, V value) {    return putVal(hash(key), key, value, false, true);}

final V putVal(int hash, K key, V value, boolean onlyIfAbsent, boolean evict) {    
    Node<K,V>[] tab; 
    Node<K,V> p;
    int n, i;   
    if ((tab = table) == null || (n = tab.length) == 0)       
        n = (tab = resize()).length;   
    if ((p = tab[i = (n - 1) & hash]) == null)       
        tab[i] = newNode(hash, key, value, null);    
    else {        
        Node<K,V> e; K k;        
        if (p.hash == hash && ((k = p.key) == key || (key != null && key.equals(k))))            
            e = p;        
        else if (p instanceof TreeNode)           
            e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);        
        else {            
            for (int binCount = 0; ; ++binCount) {               
                if ((e = p.next) == null) {                   
                    p.next = newNode(hash, key, value, null);                    
                    if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st                        
                        treeifyBin(tab, hash);                    
                        break;               
                     }                
                     if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k))))  
                        break;                
                     p = e;            
                  }        
              }        
              if (e != null) { // existing mapping for key            
                  V oldValue = e.value;            
                  if (!onlyIfAbsent || oldValue == null)                
                        e.value = value;            
                  afterNodeAccess(e);            
                  return oldValue;       
               }    
        }    
        ++modCount;    
        if (++size > threshold)        
            resize();    
        afterNodeInsertion(evict);    
        return null;
}
```

java1.8中，如果链表长度超过阈值（TREEIFY THRESHOLD==8），就把链表转成红黑树，链表长度低于6，就把红黑树转回链表。如果桶满了（容量16 * 加载因子0.75），就需要 resize（扩容2倍后重排）。

```java
public V get(Object key) {    
    Node<K,V> e;    
    return (e = getNode(hash(key), key)) == null ? null : e.value;
}

final Node<K,V> getNode(int hash, Object key) {    
    Node<K,V>[] tab; 
    Node<K,V> first, e; 
    int n; K k;    
    if ((tab = table) != null && (n = tab.length) > 0 &&        (first = tab[(n - 1) & hash]) != null) {        
        if (first.hash == hash && // always check first node            
            ((k = first.key) == key || (key != null && key.equals(k))))            
                return first;        
        if ((e = first.next) != null) {           
            if (first instanceof TreeNode)                
                return ((TreeNode<K,V>)first).getTreeNode(hash, key);            
            do {                
                if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k))))
                    return e;            
                } while ((e = e.next) != null);        
             }    
        }    
        return null;
}
```

我们调用 get() 方法，HashMap 会使用键对象的 hashcode 找到 bucket 位置，找到 bucket 位置之后，会调用 keys.equals() 方法去找到链表中正确的节点，最终找到要找的值对象。从这里我们可以想象得到，如果每个位置上的链表只有一个元素，那么hashmap的get效率将是最高的，但是理想总是美好的，现实总是有困难需要我们去克服。















