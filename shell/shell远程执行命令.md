# shell远程执行命令
> 来源:http://www.cnblogs.com/sparkdev/p/6842805.html

## 远程执行普通命令
```bash
ssh root@hostname "ls /root;pwd"
```

## 远程执行交互命令
```bash
ssh -t root@hostname "sudo chmod 777 a.txt"
```

## 本地脚本在远程机器上执行
```bash
ssh root@hostname 'bash -s' < test.sh parameter1 
```

## 执行远程机器上的脚本
```bash
ssh root@hostname /home/dashu/test.sh parameter1
```
