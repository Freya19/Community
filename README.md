# Community

### 技术栈
****




### 功能梳理
****




### 模块梳理

#### 个性化推荐模块
1. 整体上本模块采用"推"和"拉"的方式并存。
2. 当用户点赞，评论，发布博客时，会将这些"动态"封装成feed对象存入数据库，同时将这个动态发给所有的粉丝（从数据库中找到所有的粉丝，筛选最近n天登陆过的人，然后将此feed存入他们对应的redis的timelineKey中，即
每个用户的redis的timelineKey中存放着自己关注的对象的动态）
3. 当用户登录后，redis中有值则从redis中取，否则从数据库中取。
4. 至此，获得了用户关注的人的最近动态


****




### 环境搭建
- 环境搭建，见[docker.md](https://github.com/Freya19/Community/blob/master/docs/docker.md)，本项目所有服务都在docker环境下搭建。
- 数据库初始化：所有的SQL script见[SQL_Script](https://github.com/Freya19/Community/tree/master/docs/SQL_Script)
  <br/>运行顺序为 	init_schema.sql(建表） -> init_quartz.sql（quartz相关表）
****




### 更新日志
- 见[bugs.md](https://github.com/Freya19/Community/blob/master/docs/更新日志.md)
****



### 项目截图

#### 首页
<img src="http://pyyf.oss-cn-hangzhou.aliyuncs.com/post/img/2020/04/09/22/49/57/img/首页.png" alt="img" width="67%;" />

#### 个性化推荐
<img src="http://pyyf.oss-cn-hangzhou.aliyuncs.com/post/img/2020/04/09/22/49/57/img/个性化预览.png" alt="img" width="67%;" />

#### 帖子详情页
- 支持目录查看即目录跳转
<img src="http://pyyf.oss-cn-hangzhou.aliyuncs.com/post/img/2020/04/09/22/49/57/img/帖子详情页.png" alt="img" width="67%;" />

#### 站内搜索
<img src="http://pyyf.oss-cn-hangzhou.aliyuncs.com/post/img/2020/04/09/22/49/57/img/站内搜索.png" alt="img" width="67%;" />

#### 标签检索
<img src="http://pyyf.oss-cn-hangzhou.aliyuncs.com/post/img/2020/04/09/22/49/57/img/标签检索.png" alt="img" width="67%;" />

#### 个人主页
<img src="http://pyyf.oss-cn-hangzhou.aliyuncs.com/post/img/2020/04/09/22/49/57/img/个人主页.png" alt="img" width="67%;" />

#### 最新动态
<img src="http://pyyf.oss-cn-hangzhou.aliyuncs.com/post/img/2020/04/09/22/49/57/img/最新动态.png" alt="img" width="67%;" />

#### 站内私信功能
<img src="http://pyyf.oss-cn-hangzhou.aliyuncs.com/post/img/2020/04/09/22/49/57/img/站内私信功能.png" alt="img" width="67%;" />


