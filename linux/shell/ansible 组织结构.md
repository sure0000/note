怎样组织 playbook 才是最好的方式呢？简单的回答就是：使用 roles ! Roles 基于一个已知的文件结构，去自动的加载某些 vars_files，tasks 以及 handlers。基于 roles 对内容进行分组，使得我们可以容易地与其他用户分享 roles。

```yaml
production # inventory file for production servers 关于生产环境服务器的清单文件
stage # inventory file for stage environment 关于 stage 环境的清单文件


group_vars/
    group1 # here we assign variables to particular groups 这里我们给特定的组赋值
    group2 # ""
host_vars/
    hostname1 # if systems need specific variables, put them here 如果系统需要特定的变量,把它们放置在这里.
    hostname2 # ""


library/ # if any custom modules, put them here (optional) 如果有自定义的模块,放在这里(可选)
filter_plugins/ # if any custom filter plugins, put them here (optional) 如果有自定义的过滤插件,放在这里(可选)


site.yml # master playbook 主 playbook
webservers.yml # playbook for webserver tier Web 服务器的 playbook
dbservers.yml # playbook for dbserver tier 数据库服务器的 playbook


roles/
    common/ # this hierarchy represents a "role" 这里的结构代表了一个 "role"
        tasks/ #
            main.yml # <-- tasks file can include smaller files if warranted
        handlers/ #
            main.yml # <-- handlers file
        templates/ # <-- files for use with the template resource
            ntp.conf.j2 # <------- templates end in .j2
        files/ #
            bar.txt # <-- files for use with the copy resource
            foo.sh # <-- script files for use with the script resource
        vars/ #
            main.yml # <-- variables associated with this role
        defaults/ #
            main.yml # <-- default lower priority variables for this role
        meta/ #
            main.yml # <-- role dependencies


    webtier/ # same kind of structure as "common" was above, done for the webtier role
    monitoring/ # ""
    fooapp/ # ""
```
