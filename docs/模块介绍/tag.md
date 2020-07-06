改进：
1. 记录每个tag对应的帖子id
discusspost:tag:多线程  list(1,2,3,4,5)
discusspost:tag:mysql  list(7,8)

这时候显示所有tag怎么办？  keys discusspost:tag:*  然后遍历所有key找length。
每次用户访问首页都来一次，感觉也比较耗费资源

2. 增加hashmap存储帖子数量
discusspost:tag-whole {多线程:5, mysql 2}

3. 用户发帖   tags 增加
   用户改帖   旧的tags：-1  新的tags：+1
   
4. 缓存帖子内容 {id:帖子}

5. 全量更新  ->  增量更新 -> 同步 
    发布帖子： tags -> kafka
    修改帖子： 旧的，新的tags -> kafka


redis or caffine:


1. 本地缓存 ->  发布订阅   ->  两个caffine（1. 多线程：list 2. 多线程:5 ）
    1. 启动的时候，就得遍历所有的帖子，增量更新
    2. 新的应用加入了，也得遍历
    
2. redis集中式缓存+caffine缓存：   
    1. redis计算
    2. caffeine 维护1.2级缓存
    3. 专门设置一个应用程序 ，全量统计（加tagCache功能，运行一次）
    4. 应用程序负责增量（消息发布订阅）   
    启动的时候全量（在本地计算，计算完成后直接给redis，set，第二次再统计数量），以后每次增量
    
    1. 专门设置一个应用程序 ，全量统计，操作redis
    2. 增量：旧的，新的tags 更新redis，并且消费发布订阅
    3. 新的应用加入了，去redis取

