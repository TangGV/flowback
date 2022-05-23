#!/bin/bash

#安装所需要的软件包
sudo yum install -y yum-utils \
  device-mapper-persistent-data \
  lvm2
#设置稳定的仓库------阿里云源
sudo yum-config-manager \
    --add-repo \
    http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo
#安装 Docker Engine-Community
sudo yum install -y docker-ce docker-ce-cli containerd.io
#启动docker
sudo systemctl start docker
#设置docker开机启动
systemctl  enable docker
#安装Docker Compose
#下载二进制文件
curl -L https://get.daocloud.io/docker/compose/releases/download/v2.4.1/docker-compose-`uname -s`-`uname -m` > /usr/local/bin/docker-compose
#添加执行权限
sudo chmod +x /usr/local/bin/docker-compose
#创建软链
sudo ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose
#测试是否安装成功
docker-compose --version

#wget http://tools.lishaoyu.xyz/tang/docker-compose-standalone.yaml
#wget http://tools.lishaoyu.xyz/tang/prometheus.yml

docker-compose -f docker-compose-standalone.yaml up -d

echo "启动成功"
echo "执行测试请求:http://ip:16666/logTestWork?count=10000000"

