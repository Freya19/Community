package site.pyyf.community.service;

import site.pyyf.community.entity.Feed;

import java.util.List;
import java.util.Set;

/**
 * Created by Gepeng18 on 2020/1/2.
 */

public interface IFeedService {


    List<Feed> getUserFeeds( List<Integer> userIds, int entityType,int count) ;

    boolean addFeed(Feed feed) ;
    Feed getById(int id);

    List<Feed> getFeeds();

    String getFeedContentByPostId(Integer postId, Set<Feed> published, Set<Feed> liked, Set<Feed> commented);


    /**
     * @Description 通过ID查询单条数据
     * @author "Freya19"
     * @date 2020-04-19 20:19:22
     * @param id 主键
     * @return 实例对象
     */
    Feed queryById(Integer id);

    /**
     * @Description 查询全部数据
     * @author makejava
     * @date 2020-04-19 20:19:22
     * @return 对象列表
     */
    List<Feed> queryAll();

    /**
     * @Description 实体作为筛选条件查询数据
     * @author "Freya19"
     * @date 2020-04-19 20:19:22
     * @param feed 实例对象
     * @return 对象列表
     */
    List<Feed> queryAll(Feed feed);

    /**
     * @Description 查询实体数量
     * @author "Freya19"
     * @date 2020-04-19 20:19:22
     * @return 数量
     */
    int queryCount();

    /**
     * @Description 实体作为筛选条件查询数量
     * @author "Freya19"
     * @date 2020-04-19 20:19:22
     * @param feed 实例对象
     * @return 数量
     */
    int queryCount(Feed feed);


    /**
     * @Description 查询多条数据
     * @author "Freya19"
     * @date 2020-04-19 20:19:22
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<Feed> queryAllByLimit(int offset, int limit);

    /**
     * @Description 实体作为筛选条件查询指定行
     * @author "Freya19"
     * @date 2020-04-19 20:19:22
     * @param feed 实例对象
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<Feed> queryAllByLimit(Feed feed,int offset, int limit);


    /**
     * @Description 新增数据
     * @author "Freya19"
     * @date 2020-04-19 20:19:22
     * @param feed 实例对象
     * @return 实例对象
     */
    Feed insert(Feed feed);

    /**
     * @Description 修改数据
     * @author "Freya19"
     * @date 2020-04-19 20:19:22
     * @param feed 实例对象
     * @return 实例对象
     */
    Feed update(Feed feed);

    /**
     * @Description 通过主键删除数据
     * @author "Freya19"
     * @date 2020-04-19 20:19:22
     * @param id 主键
     * @return 是否成功
     */
    boolean deleteById(Integer id);

}
