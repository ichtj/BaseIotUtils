# mysql 主从复制教程
use mysql
 
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '123';
 
FLUSH PRIVILEGES;
# ----------------------------------------
# 登录
mysql -u root -p

# 使用端口号登录
mysql -h localhost -u root -p -P 3307
# ----------------------------------------

# 切换到------------主库

# 创建一个用户，可以给另一个从库访问的权限

create user 'testmaster'@'192.168.1.211' identified by 'ctj123';

grant all on *.* to testmaster@192.168.1.211;

flush privileges;# 保存刷新

# grant 权限列表 on 数据库 to '用户名'@'访问主机'; (修改权限时在后面加 with grant option)

show master status;//查看主服务器的二进制信息


# 切换到------------从库

# 关联主库上的testmaster ctj123 ip:192.168.1.211的用户

change master to master_host='192.168.1.159',master_port=3307,master_user='testmaster',master_password='ctj123',master_log_file='mysql-bin.000053',master_log_pos=12927;

# 然后执行命令

# 关闭主从复制
stop slave;

# 开启主从复制
start slave;

# 查看slave的状态
show slave status \G


# 以下为配置文件内容




