﻿# 福大时光机后端接口

## 概要

![interface](http://hlx-blog.oss-cn-beijing.aliyuncs.com/18-10-2/88793019.jpg)

## 说明

### 1.时光

* 发布：包括时光的标题，图片，内容，标签，发布者的经纬度，点赞数，审核状态，发布时间戳以及最后更新时间戳。标签为可选项，可以自定义或者选择系统所提供的标签，最后更新时间戳等于发布时间戳，点赞数默认为0，审核状态默认为待审核(通过int表示状态)

* 修改：可修改时光的标题，图片，内容，标签，并刷新最后更新时间戳

* 收藏：略

* 点赞：增加某时光的点赞数

* 探索：随机出现某一时光(目前)，未来加入实时推荐算法

* 排行：按照某一个算法，获取权重最大的前10个时光，目前采用牛顿冷却定律作为算法的依据

* 删除：略

* 我的：获取自己之前所发布的所有时光

* 分享：未知

* 审核：通过管理员审核时光的合法性，其状态包括待审核，审核失败以及审核成功

### 2.合作商店

* 添加合作商店：

* 添加商品：

* 修改商品：

* 删除商品：

### 3.数据分析

* 时光分布：返回所有时光的经纬度

* 流量统计：统计每天的流量

## 技术关键点

### 1.排名算法

### 2.探索算法

### 3.高并发及负载均衡

## 性能优化

### 1.未来通过**ActiveMQ**消息中间件，进一步优化业务逻辑，异步处理耗时操作

### 2.采用redis作为缓存

### 3.优化tomcat配置

## 基础Api

### 1.GET /user/xLogin 进行模拟登陆,获取S-TOKEN

### 2.后续的请求头部带上S-TOKEN,接口开发过程中,可通过httpSession获取唯一用户id,示例代码:  
```
HttpSession httpSession = request.getSession(false);
Integer userId = (Integer) session.getAttribute("userId");
```