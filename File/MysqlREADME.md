# mysql 主从复制教程
use mysql
 
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '123';
 
FLUSH PRIVILEGES;

# 切换到------------主库

# 可以给另一个ip访问的权限，以及使用对应的localhost ip 能够登录

create user 'testyun'@'192.168.1.125' identified by 'ctj123';

grant all on *.* to testyun@192.168.1.125;

flush privileges;# 保存刷新

# grant 权限列表 on 数据库 to '用户名'@'访问主机'; (修改权限时在后面加 with grant option)

show master status;//查看master的状态


# 切换到------------从库

# 关联主库上的testyun ctj123 ip:192.168.1.141的用户

change master to master_host='192.168.1.141',master_port=3306,master_user='testyun',master_password='ctj123',master_log_file='mysql-bin.000007',master_log_pos=0;

# 然后执行命令

# 关闭主从复制
stop slave;

# 开启主从复制
start slave;

# 查看slave的状态
show slave status \G;



