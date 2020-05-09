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
3. 当用户登录后，redis中有值则从redis中对应的key中取，否则先从redis中获取所有的关注的人，然后从数据库中遍历搜索。 至此，获得了用户关注的人的最近动态
4. 当用户每查看一个帖子时，就会将这个帖子的标签存到redis对应的tagKey中。
5. 最终，用户的个性化页面展示顺序即为：
        5.1 用户关注的人发送的帖子
        5.2 用户关注的人点赞的帖子
        5.3 用户关注的人评论的帖子
        5.4 用户最近查看的标签对应的帖子且发布时间是近期(否则用户看了两个"多线程"的帖子，然后向此用户推荐了网站所有的多线程，显然不合适，所以只推荐最近发布的"多线程"的帖子)
        5.5 再按照分数递减的顺序
注意点
1. 我们为timelineKey设置了生存时间，如小葛关注了小芳，则小芳发布的帖子，进入了小葛的timelineKey，此时，如果小葛5天没登录了，则从此小芳的点赞评论都不会在告诉小葛，
   此时我们就可以把小葛的timelineKey删除了，当小葛再次登录时，小芳发布的帖子，评论点赞才会进入小葛的timelineKey，以此解决redis内存。
2. 如果我们最近查看了"多线程"，那么分页查询第一页一定是0-10，这时候我们看的帖子的标签就会被redis记录，此时如果我们点击第二页，则从redis中的tagKey取值时，第二页的查询条件就会改变，
   这时候可能第一页查看过的数据，第二页又会出现，或者是有些数据，永远看不到，即我们需要保证分页查询时，每次查询条件不变，所以我们设置了两个key,持久化key（persistence）和最近的key(latest),
   这时候我们每次查询都从持久化key中取，而每次查询第一页时，将最新的key置换为持久化key，这样即可以保证每次查询条件相同，且是伪最新的标签。

#### 主页热门标签显示模块
1. 用户发布帖子的时候，都会附带一个tag标签
2. 使用quartz 每隔3个小时统计一次，遍历所有的帖子，统计属于每个标签的帖子个数以及总分数（如"多线程"标签有两个帖子，则"多线程"的分即为这两个帖子的分数相加
3. 使用treeset对这些标签进行排序，然后选出N个帖子存储在tagCache中，每当用户访问主页面时，就从TagCache中去取
4. 当帖子数量增多时，统计时间也可以设置为每天晚上3点，夜深人静偷偷统计。（统计帖子的个数，有点误差可以接受，所以一天统计一次似乎也可行）

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


