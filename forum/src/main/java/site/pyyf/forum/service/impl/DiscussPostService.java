package site.pyyf.forum.service.impl;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import site.pyyf.forum.entity.DiscussPost;
import site.pyyf.forum.service.IDiscussPostService;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DiscussPostService extends BaseService implements IDiscussPostService {

    private static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    // Caffeine核心接口: Cache, LoadingCache, AsyncLoadingCache

    // 帖子列表缓存
    private LoadingCache<String, List<DiscussPost>> postListCache;

    // 帖子总数缓存
    private LoadingCache<String, Integer> postRowsCache;

    @PostConstruct
    public void init() {
        // 初始化帖子列表缓存
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Nullable
                    @Override
                    public List<DiscussPost> load(@NonNull String key) throws Exception {
                        if (key == null || key.length() == 0) {
                            throw new IllegalArgumentException("参数错误!");
                        }

                        String[] params = key.split(":");
                        if (params == null || params.length != 3) {
                            throw new IllegalArgumentException("参数错误!");
                        }

                        int offset = Integer.valueOf(params[0]);
                        int limit = Integer.valueOf(params[1]);
                        String tag = String.valueOf(params[2]);

                        // 二级缓存: Redis -> mysql

                        logger.debug("load post list from DB.");
                        DiscussPost query = DiscussPost.builder().userId(-1).tags(tag).build();
                        return iDiscussPostMapper.queryAllByLimit(query, 1,offset, limit);
                    }
                });
        // 初始化帖子总数缓存
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, Integer>() {
                    @Nullable
                    @Override
                    public Integer load(@NonNull String key) throws Exception {

                        if (key == null || key.length() == 0) {
                            throw new IllegalArgumentException("参数错误!");
                        }

                        String[] params = key.split(":");
                        if (params == null || params.length != 2) {
                            throw new IllegalArgumentException("参数错误!");
                        }

                        int userId = Integer.valueOf(params[0]);
                        String tag = String.valueOf(params[1]);
                        logger.debug("load post rows from DB.");
                        DiscussPost query = DiscussPost.builder().userId(userId).tags(tag).build();
                        return iDiscussPostMapper.queryCount(query);
                    }
                });
    }

    @Override
    public List<DiscussPost> queryAllByLimit(DiscussPost discussPost, int orderMode, int offset, int limit) {
        if (discussPost.getUserId() == -1 && orderMode==1) {
            return postListCache.get(offset + ":" + limit+ ":"+discussPost.getTags());
        }

        logger.debug("load post list from DB.");
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

    public DiscussPost findDiscussPostById(int id) {
        return iDiscussPostMapper.queryById(id);
    }

    public int updateCommentCount(int id, int commentCount) {
        DiscussPost query = queryById(id);
        query.setCommentCount(commentCount);
        return iDiscussPostMapper.update(query);
    }

    public int updateType(int id, int type) {
        DiscussPost query = queryById(id);
        query.setType(type);
        return iDiscussPostMapper.update(query);
    }

    public int updateStatus(int id, int status) {
        DiscussPost query = queryById(id);
        query.setStatus(status);
        return iDiscussPostMapper.update(query);
    }

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
        return iDiscussPostMapper.queryById(id);
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
    public List<DiscussPost> queryAllByLimit(int orderMode,int offset, int limit) {
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
        if (discussPost.getUserId() == -1) {
            return postRowsCache.get(discussPost.getUserId() +":"+discussPost.getTags());
        }

        logger.debug("load post rows from DB.");
        return iDiscussPostMapper.queryCount(discussPost);

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

    /**
     * @Description 通过主键删除数据
     * @author "Gepeng18"
     * @date 2020-03-27 07:57:21
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(Integer id) {
        return iDiscussPostMapper.deleteById(id) > 0;
    }

}
