---
typora-root-url: docs\images
---



# 1 插件开发过程

spring boot项目，尤其是spring cloud搭建的微服务，每个服务都需要发布至少一个节点。如果发布多个节点，且使用docker，需要敲一堆的命令。

整个发布的流程看起来复杂，但是，拆分开来，也没多少东西。

所以，我想，功能还是可以实现的：

**按照默认的约定，只需很少的配置，将gradle打包好的jar自动发布成多个docker container。**

分步骤实现自定义的docker plugin



![使用插件后，project的构建过程](/1532399005841.png)



## 1.1 v0.1

| 功能点              | 描述                                              |
| ------------------- | ------------------------------------------------- |
| build dockerfile    | dockerfile的内容，使用kotlin的模板字符串          |
| connect docker host | 使用jsch连接linux                                 |
| push dockerfile     | 上传dockerfile到`/opt/${project.name}/Dockerfile` |





# 2 使用插件

