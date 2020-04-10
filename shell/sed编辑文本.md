# sed 编辑文本
> 来源：https://www.cnblogs.com/ggjucheng/archive/2013/01/13/2856901.html  
> http://www.blogjava.net/zhyiwww/archive/2008/11/24/242281.html

**直接替换文本中字符**
```bash
sed -i 's/需要被替换字符/替换字符/g' 文件名
```

**删除指定字符所在行**
```bash
sed -i /keyword/d filepath
```

**指定字符后插入文件**
```bash
sed -i "/keywords/r insert_filepath" target_filepath
```

