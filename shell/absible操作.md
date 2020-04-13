## ansible-playbook

**执行playbook命令**
```bash
ansible-playbook -i inventory_file_path playbook.yml -e "var=real_data"
```
**基础playbook**
```yml
---
- hosts: all
  remote_user: root
  tasks:
  - name: copy template to hosts
    copy:
      src: /data0/xyc/playbook/tem.yml
      dest: /root/
      owner: root
      group: root

  - name: add monitor to filebeat
    shell: sed -i "/max_message_bytes/r /root/tem.yml" /etc/filebeat/filebeat.yml

  - name: restart filebeat
    service:
      name: filebeat
      state: restart
```
**引入外部文件与变量**
```yml
---
## 引用task或play
- include: "{{ main }}.yml"

## 引用yml文件内的变量
task:
- include_vars: vars.yml
  name: get vars
  shell: echo "{{ text }}""
```
vars.yml
```yml
text: this is from vars
```
**模版文件**
```yml
---
- hosts: all
  remote_user: root
  vars:
    dynamic_word: "{{ ansible_facts['nodename'] }}"

  tasks:
  - name: add monitor to filebeat
    template:
      src: hello.txt.j2
      dest: /root/hello_xyc.txt
      owner: root
      group: root
      mode: 777
      backup: yes
```
hello.txt.j2
```
hello "{{ dynamic_word }}"
```

**循环**
```yml
---
- hosts: all
  remote_user: root
  
  tasks:
  - include_vars: list.yml
  
  - name: loop
    shell: echo "{{ item }}"
    loop: {{ list }}
```
list.yml
```yml
list:
  - this is 1
  - this is 2
  - this is 3
```

## Handlers

```yml
---

- name: setup the nginx
 hosts: all
 become: true
 vars:
   username: "ironman"
   mail: "chusiang (at) drx.tw"
   blog: "http://note.drx.tw"

 tasks:
   # 执行 'apt-get update' 指令。
   - name: update apt repo cache
     apt: name=nginx update_cache=yes

   # 执行 'apt-get install nginx' 指令。
   - name: install nginx with apt
     apt: name=nginx state=present

   # 于网页根目录 (DocumentRoot) 编辑 index.html。
   - name: modify index.html
     template: >
       src=templates/index.html.j2
       dest=/usr/share/nginx/html/index.html
       owner=www-data
       group=www-data
       mode="644"
       backup=yes
     notify: restart nginx

   # (security) 关闭 server_tokens：移除 server_tokens 前的 '#' 字元。
   - name: turn server_tokens off
     lineinfile: >
       dest=/etc/nginx/nginx.conf
       regexp="server_tokens off;"
       insertafter="# server_tokens off;"
       line="server_tokens off;"
       state=present
     notify: restart nginx

 # handlers 
 #
 # * 当确认事件有被触发才会动作。
 # * 一个 handler 可被多个 task 通知 (notify)，并于 tasks 跑完才会执行。
 handlers:
   # 执行 'sudo service nginx restart' 指令。
   - name: restart nginx
     service: name=nginx enabled=yes state=restarted

 # post_tasks:
 #
 # 在 tasks 之后执行的 tasks。
 post_tasks:
   # 检查网页内容。
   - name: review http state
     command: "curl -s http://localhost"
     register: web_context

   # 印出检查结果。
   - name: print http state
     debug: msg=

# vim:ft=ansible :
```
1. 在第 47 行里，我们建立了一个 restart nginx handler。
2. 在修改到 Nginx 设定档的 tasks (modify index.html, turn server_tokens off) 里，使用 notify 通知 handlers (restart nginx) 说这些 Tasks 要进行关连。
3. 最后在 post_tasks 里建了 2 个 tasks，让它们可以在一般的 tasks 结束后才执行。