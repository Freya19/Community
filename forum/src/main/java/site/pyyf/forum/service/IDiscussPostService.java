package site.pyyf.forum.service;

import com.github.benmanes.caffeine.cache.LoadingCache;
import site.pyyf.forum.entity.DiscussPost;

import java.util.List;

/**
 * @Description (DiscussPost)表服务接口
 *
 * @author "Gepeng18"
 * @since 2020-03-27 07:57:21
 */
public interface IDiscussPostService {
    /**
     * 获取caffine容器
     * @return
     */
    LoadingCache<String, DiscussPost> queryCaffineCache();

    /**
     * @Description 通过ID查询单条数据
     * @author "Gepeng18"
     * @date 2020-03-27 07:57:21
     * @param id 主键
     * @return 实例对象
     */
    DiscussPost queryRedisCache(Integer id);

     /**
     * @Description 查询全部数据
     * @author makejava
     * @date 2020-03-27 07:57:21
     * @return 对象列表
     */
    List<DiscussPost> queryAll();

    /**
     * @Description 实体作为筛选条件查询数据
     * @author "Gepeng18"
     * @date 2020-03-27 07:57:21
     * @param discussPost 实例对象
     * @return 对象列表
     */
    List<DiscussPost> queryAll(DiscussPost discussPost);

    /**
     * @Description 查询实体数量
     * @author "Gepeng18"
     * @date 2020-03-27 07:57:21
     * @return 数量
     */
    int queryCount();

    /**
     * @Description 实体作为筛选条件查询数量
     * @author "Gepeng18"
     * @date 2020-03-27 07:57:21
     * @param discussPost 实例对象
     * @return 数量
     */
    int queryCount(DiscussPost discussPost);


    /**
     * @Description 查询多条数据
     * @author "Gepeng18"
     * @date 2020-03-27 07:57:21
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<DiscussPost> queryAllByLimit(int orderMode, int offset, int limit);

    List<DiscussPost> queryAllByLimit(DiscussPost discussPost, int orderMode, int offset, int limit);

    public List<DiscussPost> queryAllByLimitByPersonalized(List<Integer> publishedIds, List<Integer> likedIds, List<Integer> commentIds, List<String> tags,int deadline,int offset, int limit) ;

    /**
     * @Description 新增数据
     * @author "Gepeng18"
     * @date 2020-03-27 07:57:21
     * @param post 实例对象
     * @return 实例对象
     */
    int insert(DiscussPost post);

    /**
     * @Description 修改数据
     * @author "Gepeng18"
     * @date 2020-03-27 07:57:21
     * @param discussPost 实例对象
     * @return 实例对象
     */
    DiscussPost update(DiscussPost discussPost);


    /**
     * @Description 保存数据
     * @author "Freya19"
     * @date 2020-07-25 15:44
     * @param discussPost 帖子
     * @return 帖子
     */
    DiscussPost save(DiscussPost discussPost);

    /**
     * @Description 通过主键删除数据
     * @author "Gepeng18"
     * @date 2020-03-27 07:57:21
     * @param id 主键
     * @return 是否成功
     */
    boolean deleteById(Integer id);

    DiscussPost findDiscussPostById(int id);

    int updateCommentCount(int id, int commentCount);

    int updateType(int id, int type);

    int updateStatus(int id, int status);

    int updateScore(int id, double score) ;
}