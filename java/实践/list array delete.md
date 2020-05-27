java List 对象有三种移除元素的方式：
1. 普通 for 循环移除；
2. foreach 循环移除；
3. iterator 迭代器循环移除；

for 循环移除
```java
// 普通 for 循环每删除一个元素会导致 list 大小发生变化，删除元素的后一位会依次上移到前一位，最终的结果是元素被间隔删除。所以需要在每删除一个元素之后，将 index 后退一步。
for (int i = 0; i < list.size(); i++) {
    list.remove(i);
    i--;    // 关键
}
```

foreach 循环移除
```java
// 这种方式的问题在于，删除元素后继续循环会报错误信息ConcurrentModificationException，因为元素在使用的时候发生了并发的修改，导致异常抛出。但是删除完毕马上使用break跳出，则不会触发报错。原因：checkForComodification方法如果modCount不等于expectedModCount，则抛出ConcurrentModificationException异常。
for (int item : list) {
    if (item == 3)
        list.remove(item);
        break;  // 关键
}

list.foreach((item) -> {
    if (item == 3)
        list.remove(item);
        break;  // 关键
})
```

iterator 循环移除
```java
// 这种方式可以正常的循环及删除。但要注意的是，使用iterator的remove方法，如果用list的remove方法同样会报上面提到的ConcurrentModificationException错误。
Iterator<String> it = list.iterator();
while(it.hasNext()){
    String x = it.next();
    if(x.equals("del")){
        it.remove();
    }
}
```

Array 删除
```java
// Java数组的长度固定，因此无法直接删除数组中的元素。通过创建新的数组，将保留的原数组中的元素赋值到新数组来实现原数组元素的删除。同理，可以实现数组添加元素。
public static void main(String[] args) {
		int[] array1 = new int[] {4, 5, 6, 7};
		int num = 2;
		int[] newArray = new int[array1.length-1];
		
		for(int i=0;i<newArray.length; i++) {
			// 判断元素是否越界
			if (num < 0 || num >= array1.length) {
				throw new RuntimeException("元素越界... "); 
			} 
			// 
			if(i<num) {
				newArray[i] = array1[i];
			}
			else {
				newArray[i] = array1[i+1];
			}
		}
		// 打印输出数组内容
		System.out.println(Arrays.toString(array1));
		array1 = newArray;
		System.out.println(Arrays.toString(array1));
	}
```


