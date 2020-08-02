package site.pyyf.forum.service.impl;


import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import site.pyyf.commons.utils.RedisKeyUtil;
import site.pyyf.forum.entity.DiscussPost;
import site.pyyf.forum.service.IDiscussPostService;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class DiscussPostService extends BaseService implements IDiscussPostService {

    private static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    // Caffeine核心接口: Cache, LoadingCache, AsyncLoadingCache

    // 帖子缓存
    private LoadingCache<String,DiscussPost> postListCache;


    @PostConstruct
    public void init() {
        // 初始化帖子缓存
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, DiscussPost>() {
                    @Nullable
                    @Override
                    public DiscussPost load(@NonNull String key) throws Exception {
                        if (key == null || key.length() == 0) {
                            throw new IllegalArgumentException("参数错误!");
                        }

                        Integer postId = Integer.valueOf(key);
                        logger.debug("从caffeine拿id = "+key+" 的帖子");
                        // 从redis拿
                        return queryById(postId);
                    }
                });
    }


    @Override
    public DiscussPost save(DiscussPost discussPost){
        //传入的是编辑的帖子
        if(discussPost.getId()!=null){
            changeRedisTagList(discussPost);
            clearCache(discussPost.getId());
            update(discussPost);
            return postListCache.get(String.valueOf(discussPost.getId()));
        }else {
            addRedisTagList(discussPost);
            //传入的新发布的帖子
            insert(discussPost);
            redisTemplate.opsForList().leftPush(RedisKeyUtil.getLatestPostsList(),discussPost.getId());
            redisTemplate.opsForZSet().add(RedisKeyUtil.getHotPostsList(),discussPost.getId(),0);
            return postListCache.get(String.valueOf(discussPost.getId()));
        }
    }


    /**
     * @Description 修改数据
     * @author "Gepeng18"
     * @date 2020-03-27 07:57:21
     * @param discussPost 实例对象
     * @return 实例对象
     */
    @Override
    public DiscussPost update(DiscussPost discussPost) {
        this.iDiscussPostMapper.update(discussPost);
        return queryById(discussPost.getId());
    }


    private void  delRedisTagList(DiscussPost discussPost){
        // 先查询以前的帖子tags,将id从里面删了
        String[] tagNames = iDiscussPostMapper.queryById(discussPost.getId()).getTags().split(",");
        for (String tagName:tagNames){
            // 标签对应的帖子数量-1，标签对应的帖子的id，需要删掉一个
            redisTemplate.opsForZSet().remove(RedisKeyUtil.getTagPostsList(tagName),discussPost.getId());
            redisTemplate.opsForZSet().incrementScore(RedisKeyUtil.getTagsCount(),tagName,-1);
            if(redisTemplate.opsForZSet().score(RedisKeyUtil.getTagsCount(),tagName).equals(0))
                redisTemplate.opsForZSet().remove(RedisKeyUtil.getTagsCount(),tagName);

        }
    }

    private void  addRedisTagList(DiscussPost discussPost){
        // 再查询现在的帖子tags,将id加上，同时数量加1
        for (String tagName:discussPost.getTags().split(",")){
            redisTemplate.opsForZSet().add(RedisKeyUtil.getTagPostsList(tagName),discussPost.getId(),discussPost.getCreateTime().getTime());
            redisTemplate.opsForZSet().incrementScore(RedisKeyUtil.getTagsCount(),tagName,1);
        }
    }

    private void changeRedisTagList(DiscussPost discussPost) {
        delRedisTagList(discussPost);
        addRedisTagList(discussPost);
    }

    @Override
    public List<DiscussPost> queryAllByLimit(DiscussPost discussPost, int orderMode, int offset, int limit) {
        // discussPost == null表明是主页查询
        if(discussPost == null){
            //从redis中获得组合id
            Collection<Integer> ids;
            List<DiscussPost> discussPosts = new ArrayList<>();
            if(orderMode==0){
                logger.debug("主页时间查询，从redis中拿到ids，offset = "+offset+" ,limit = "+limit);
                String latestIdsKey = RedisKeyUtil.getLatestPostsList();
                // redis 的 range 是from 到 to
                ids = redisTemplate.opsForList().range(latestIdsKey, offset, offset+limit-1);
            }else if (orderMode == 1){
                logger.debug("主页热度查询，从redis中拿到ids，offset = "+offset+" ,limit = "+limit);
                String hotIdsKey = RedisKeyUtil.getHotPostsList();
                ids = redisTemplate.opsForZSet().reverseRange(hotIdsKey, offset, offset+limit-1); // redis从小到大排的，所以reverse一下
            }else {
                //只是为了代码不报错，没用
                ids = new ArrayList<>();
            }

            // 根据组合id去搜对应的帖子
            for (Integer id: ids){
                discussPosts.add(postListCache.get(String.valueOf(id)));
            }
            return discussPosts;
        }

        // 按标签查询，只是这里没有分按时间和按热度进行子查询，只提供按时间查询
        if(discussPost.getTags()!=null){
            List<DiscussPost> discussPosts = new ArrayList<>();
            Set<Integer> ids = redisTemplate.opsForZSet().reverseRange(RedisKeyUtil.getTagPostsList(discussPost.getTags()), offset, limit);
            logger.debug("标签查询，从redis中拿到ids，tag = "+discussPost.getTags()+",offset = "+offset+" ,limit = "+limit);
            // 根据组合id去搜对应的帖子
            for (Integer id: ids){
                discussPosts.add(postListCache.get(String.valueOf(id)));
            }
            return discussPosts;
        }


        logger.debug("从mysql中进行查询帖子列表，offset = "+offset+",limit= "+limit);
        return iDiscussPostMapper.queryAllByLimit(discussPost, orderMode, offset, limit);
    }

    @Override
    public List<DiscussPost> queryAllByLimitByPersonalized(List<Integer> publishedIds, List<Integer> likedIds, List<Integer> commentIds, List<String> tags,int deadline,int offset, int limit) {
        return iDiscussPostMapper.queryAllByLimitByPersonalized( publishedIds,  likedIds,  commentIds, tags,deadline,offset, limit);
    }


    /**
     * @Description 新增数据
     * @author "Gepeng18"
     * @date 2020-03-27 07:57:21
     * @param post 实例对象
     * @return 实例对象
     */
    @Override
    public int insert(DiscussPost post) {
        if (post == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        // 转义HTML标记
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        String content = HtmlUtils.htmlEscape(post.getContent());
        //特殊处理一下 "就不转义了 否则会导致代码显示出问题
        String newContent = content.replaceAll("&quot;", "\"");
        post.setContent(newContent);

        // 过滤敏感词
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));

        return iDiscussPostMapper.insert(post);
    }

    @Override
    public DiscussPost findDiscussPostById(int id) {
        return iDiscussPostMapper.queryById(id);
    }

    @Override
    public int updateCommentCount(int id, int commentCount) {
        DiscussPost query = queryById(id);
        query.setCommentCount(commentCount);
        return iDiscussPostMapper.update(query);
    }

    @Override
    public int updateType(int id, int type) {
        // 置顶，1. 删除缓存(缓存的DO状态变了) 2. 将redis的list从中间某一个部分提高最开始
        if(type == 1){
            clearCache(id);
            delRedisTagList(iDiscussPostMapper.queryById(id));
            redisTemplate.opsForList().remove(RedisKeyUtil.getLatestPostsList(),0,id);
            redisTemplate.opsForList().leftPush(RedisKeyUtil.getLatestPostsList(),id);
        }
        DiscussPost query = queryById(id);
        query.setType(type);
        return iDiscussPostMapper.update(query);
    }

    @Override
    public int updateStatus(int id, int status) {
        if(status == 2){
            redisTemplate.opsForZSet().remove(RedisKeyUtil.getHotPostsList(),id);
            redisTemplate.opsForList().remove(RedisKeyUtil.getLatestPostsList(),0,id);
        }

        DiscussPost query = queryById(id);
        query.setStatus(status);
        return iDiscussPostMapper.update(query);
    }

    @Override
    public int updateScore(int id, double score) {
        DiscussPost query = queryById(id);
        query.setScore(score);
        return iDiscussPostMapper.update(query);
    }



    /**
     * @Description 通过ID查询单条数据
     * @author "Gepeng18"
     * @date 2020-03-27 07:57:21
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public DiscussPost queryById(Integer id) {
        DiscussPost post = getCache(id);
        if (post == null) {
            post = initCache(id);
        }
        return post;
    }


    /**
     * 1.优先从缓存中取值
     * @param postId
     * @return
     */
    private DiscussPost getCache(int postId) {
        logger.debug("caffeine没查到，从redis中查询 id = "+postId+" 的帖子");
        String postKey = RedisKeyUtil.getPostDOKey(postId);
        return (DiscussPost) redisTemplate.opsForValue().get(postKey);
    }

    /**
     *  2.取不到时初始化缓存数据
     * @param postId
     * @return
     */
    private DiscussPost initCache(int postId) {
        logger.debug("redis没查到，从mysql中查询 id = "+postId+" 的帖子");
        DiscussPost post = iDiscussPostMapper.queryById(postId);
        String postKey = RedisKeyUtil.getPostDOKey(postId);
        redisTemplate.opsForValue().set(postKey, post);
        return post;
    }

    /**
     * /3.数据变更时清除缓存数据
     * @param postId
     */
    private void clearCache(int postId) {
        logger.debug("清除 id = "+postId+"的帖子缓存");
        postListCache.invalidate(String.valueOf(postId));
        String postKey = RedisKeyUtil.getPostDOKey(postId);
        redisTemplate.delete(postKey);
    }


    /**
     * @Description 查询多条数据
     * @author "Gepeng18"
     * @date 2020-03-27 07:57:21
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    @Override
    public List<DiscussPost> queryAllByLimit(int orderMode, int offset, int limit) {
        return iDiscussPostMapper.queryAllByLimit(DiscussPost.builder().build(),orderMode,offset, limit);
    }

    /**
     * @Description 查询全部数据
     * @author "Gepeng18"
     * @date 2020-03-27 07:57:21
     * @return 对象列表
     */
    @Override
    public List<DiscussPost> queryAll() {
        return iDiscussPostMapper.queryAll();
    }

    /**
     * @Description 实体作为筛选条件查询数据
     * @author "Gepeng18"
     * @date 2020-03-27 07:57:21
     * @param discussPost 实例对象
     * @return 对象列表
     */
    @Override
    public List<DiscussPost> queryAll(DiscussPost discussPost) {
        return iDiscussPostMapper.queryAll(discussPost);
    }

    /**
     * @Description 查询实体数量
     * @author "Gepeng18"
     * @date 2020-03-27 07:57:21
     * @return 数量
     */
    @Override
    public int queryCount() {
        return iDiscussPostMapper.queryCount();
    }

    /**
     * @Description 实体作为筛选条件查询数量
     * @author "Gepeng18"
     * @date 2020-03-27 07:57:21
     * @param discussPost 实例对象
     * @return 数量
     */
    @Override
    public int queryCount(DiscussPost discussPost) {
        // 主页查询
        if(discussPost == null)
            return redisTemplate.opsForZSet().size(RedisKeyUtil.getHotPostsList()).intValue();
        // 标签查询
        if(discussPost.getTags()!=null)
            return redisTemplate.opsForZSet().size(RedisKeyUtil.getTagPostsList(discussPost.getTags())).intValue();
        // 其他查询
        logger.debug("load post rows from DB.");
        return iDiscussPostMapper.queryCount(discussPost);

    }


    /**
     * @Description 通过主键删除数据
     * @author "Gepeng18"
     * @date 2020-03-27 07:57:21
     * @param id 主键
     * @return 是否成功
     */
    @Deprecated
    @Override
    public boolean deleteById(Integer id) {
        redisTemplate.opsForZSet().remove(RedisKeyUtil.getHotPostsList(),id);
        redisTemplate.opsForList().remove(RedisKeyUtil.getLatestPostsList(),0,id);
        return iDiscussPostMapper.deleteById(id) > 0;
    }

}
