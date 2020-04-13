**control node**

Any machine with Ansible installed. You can run commands and playbooks, invoking ***/usr/bin/ansible*** or ***/usr/bin/ansible-playbook***, from any control node. You can use any computer that has Python installed on it as a control node - laptops, shared desktops, and servers can all run Ansible. However, you cannot use a Windows machine as a control node. You can have multiple control nodes.

**managed nodes**

The network devices (and/or servers) you manage with Ansible. Managed nodes are also sometimes called “hosts”. Ansible is not installed on managed nodes.

**inventory**

A list of managed nodes. An inventory file is also sometimes called a “hostfile”. Your inventory can specify information like IP address for each managed node. An inventory can also organize managed nodes, creating and nesting groups for easier scaling. To learn more about inventory, see the Working with Inventory section.

**Modules**

The units of code Ansible executes. Each module has a particular use, from administering users on a specific type of database to managing VLAN interfaces on a specific type of network device. You can invoke a single module with a task, or invoke several different modules in a playbook. For an idea of how many modules Ansible includes, take a look at the [list of all modules](https://docs.ansible.com/ansible/latest/modules/modules_by_category.html#modules-by-category).

**Tasks**

The units of action in Ansible. You can execute a single task once with an ad-hoc command.

**Playbooks**

Ordered lists of tasks, saved so you can run those tasks in that order repeatedly. Playbooks can include variables as well as tasks. Playbooks are written in YAML and are easy to read, write, share and understand. To learn more about playbooks, see [About Playbooks](https://docs.ansible.com/ansible/latest/user_guide/playbooks_intro.html#about-playbooks).

# Inventory

## format
```
## INI 

mail.example.com

[webservers]
foo.example.com
bar.example.com

[dbservers]
one.example.com
two.example.com
three.example.com

## YAML

all:
  hosts:
    mail.example.com:
  children:
    webservers:
      hosts:
        foo.example.com:
        bar.example.com:
    dbservers:
      hosts:
        one.example.com:
        two.example.com:
        three.example.com:
```
**Default groups**

There are two default groups: **all and ungrouped**. The all group contains every host. The ungrouped group contains all hosts that don’t have another group aside from all.

**Nested groups**

You can also use nested groups to simplify **prod** and **test** in this inventory:
```

all:
  hosts:
    mail.example.com:
  children:
    webservers:
      hosts:
        foo.example.com:
        bar.example.com:
    dbservers:
      hosts:
        one.example.com:
        two.example.com:
        three.example.com:
    east:
      hosts:
        foo.example.com:
        one.example.com:
        two.example.com:
    west:
      hosts:
        bar.example.com:
        three.example.com:
    prod:
      children:
        east:
    test:
      children:
        west:
```

To apply a playbook called **site.yml** to all the app servers in the **test environment**, use the following command:
```
## parallelism level of 10
ansible-playbook -i inventory_test site.yml -l appservers -f 10
```
**Adding ranges of hosts**
```
## numeric  ranges
[webservers]
www[01:50].example.com

## alphabetic ranges
[databases]
db-[a:f].example.com
```

**Adding variables to inventory**
```
## one machine

[targets]

localhost                    ansible_connection=local
other1.example.com     ansible_connection=ssh        ansible_user=myuser
other2.example.com     ansible_connection=ssh        ansible_user=myotheruser

## group variables

[atlanta]
host1
host2

[atlanta:vars]
ntp_server=ntp.atlanta.example.com
proxy=proxy.atlanta.example.com

## group variables for groups of groups

[atlanta]
host1
host2

[raleigh]
host2
host3

[southeast:children]
atlanta
raleigh

[southeast:vars]
some_server=foo.southeast.example.com
halon_system_timeout=30
self_destruct_countdown=60
escape_pods=2

[usa:children]
southeast
northeast
southwest
northwest
```

# playbook
```yaml

---
- hosts: webservers
  vars:
    http_port: 80
    max_clients: 200
  remote_user: root
  tasks:
  - name: ensure apache is at the latest version
    yum:
      name: httpd
      state: latest
  - name: write the apache config file
    template:
      src: /srv/httpd.j2
      dest: /etc/httpd.conf
    notify:
    - restart apache
  - name: ensure apache is running
    service:
      name: httpd
      state: started
  handlers:
    - name: restart apache
      service:
        name: httpd
        state: restarted
```

## Includes vs imports

 include and import statements are very similar, however the Ansible executor engine treats them very differently.
 - All import* statements are pre-processed at the time playbooks are parsed.
 - All include* statements are processed as they are encountered during the execution of the playbook.

**Importing Playbooks**
```yaml
- import_playbook: webservers.yml
- import_playbook: databases.yml
```

**Including and Importing tasks**

Breaking tasks up into different files is an excellent way to organize complex sets of tasks or reuse them. A task file simply contains a flat list of tasks:
```yaml
tasks:
- import_tasks: wordpress.yml
  vars:
    wp_user: timmy
- import_tasks: wordpress.yml
  vars:
    wp_user: alice
- import_tasks: wordpress.yml
  vars:
    wp_user: bob
```

## Role

**project structure**

Roles are ways of automatically loading certain vars_files, tasks, and handlers based on a known file structure. Grouping content by roles also allows easy sharing of roles with other users.
```
site.yml
webservers.yml
fooservers.yml
roles/
    common/
        tasks/
        handlers/
        files/
        templates/
        vars/
        defaults/
        meta/
    webservers/
        tasks/
        defaults/
        meta/
```

* tasks - contains the main list of tasks to be executed by the role.
* handlers - contains handlers, which may be used by this role or even anywhere outside this role.
* defaults - default variables for the role .
* vars - other variables for the role.
* files - contains files which can be deployed via this role.
* templates - contains templates which can be deployed via this role.
* meta - defines some meta data for this role. 

**the order of execution**

1. Any *pre_tasks* defined in the play.
2. Any *handlers* triggered so far will be run.
3. Each *role* listed in roles will execute in turn.Any role dependencies defined in the roles *meta/main.yml* will be run first, subject to tag filtering and conditionals.
4. Any *tasks* defined in the play.
5. Any *handlers* triggered so far will be run.
6. Any *post_tasks* defined in the play.
7. Any *handlers* triggered so far will be run.

**Using Role**

The classic (original) way to use roles is via the roles: option for a given play:
```yaml

---
- hosts: webservers
  roles:
    - common
    - webservers
```

## [Variables](https://docs.ansible.com/ansible/latest/user_guide/playbooks_variables.html#playbooks-variables)

Variables discovered from systems: Facts.

Facts are information derived from speaking with your remote systems. You can find a complete set under the ansible_facts variable, most facts are also ‘injected’ as top level variables preserving the ansible_ prefix, but some are dropped due to conflicts. This can be disabled via the [INJECT_FACTS_AS_VARS](https://docs.ansible.com/ansible/latest/reference_appendices/config.html#inject-facts-as-vars) setting.

use facts: `{{ ansible_facts['nodename'] }}`

## program organization


You can also add **group_vars/** and **host_vars/** directories to your playbook directory. The *ansible-playbook* command looks for these directories in the current working directory by default. Other Ansible commands (for example, *ansible, ansible-console*, etc.) will only look for **group_vars/ and host_vars/ in the inventory directory**. If you want other commands to load group and host variables from a playbook directory, you must provide the **--playbook-dir** option on the command line. If you load inventory files from both the playbook directory and the inventory directory, variables in the playbook directory will override variables set in the inventory directory.

