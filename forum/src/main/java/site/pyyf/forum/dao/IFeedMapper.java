package site.pyyf.forum.dao;

import site.pyyf.forum.entity.Feed;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Created by Gepeng18 on 2020/1/2.
 */
@Mapper
public interface IFeedMapper {
    String TABLE_NAME = " feed ";
    String INSERT_FIELDS = " feed_type,  create_time, user_id,user_name,entity_type,entity_id ";
    String SELECT_FIELDS = " id, " + INSERT_FIELDS;

    @Insert({"insert into ", TABLE_NAME, "(", INSERT_FIELDS,
            ") values (#{feedType},#{createTime},#{userId},#{userName},#{entityType},#{entityId})"})
    int addFeed(Feed feed);

    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME, " where id=#{id}"})
    Feed getFeedById(int id);

    List<Feed> selectUserFeeds(@Param("userIds") List<Integer> userIds,
                               @Param("entityType") int entityType,
                               @Param("count") int count);


    /**
     * @Description 通过ID查询单条数据
     * @author "Freya19"
     * @date 2020-04-19 20:19:22
     * @param id 主键
     * @return 实例对象
     */
    Feed queryById(Integer id);

    /**
     * @Description 查询指定行数据
     * @author "Freya19"
     * @date 2020-04-19 20:19:22
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<Feed> queryAllByLimit(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * @Description 查询全部数据
     * @author "Freya19"
     * @date 2020-04-19 20:19:22
     * @return 对象列表
     */
    List<Feed> queryAll();

    /**
     * @Description 通过实体作为筛选条件查询数据
     * @author "Freya19"
     * @date 2020-04-19 20:19:22
     * @param feed 实例对象
     * @return 对象列表
     */
    List<Feed> queryAll(Feed feed);

    /**
     * @Description 通过实体数量
     * @author "Freya19"
     * @date 2020-04-19 20:19:22
     * @return 数量
     */
    int queryCount();

    /**
     * @Description 通过实体作为筛选条件查询数量
     * @author "Freya19"
     * @date 2020-04-19 20:19:22
     * @param feed 实例对象
     * @return 数量
     */
    int queryCount(Feed feed);

    /**
     * @Description 通过实体作为筛选条件查询指定行
     * @author "Freya19"
     * @date 2020-04-19 20:19:22
     * @param feed 实例对象
     * @return 对象列表
     */
    List<Feed> querySomeByLimit(@Param("feed") Feed feed, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * @Description 新增数据
     * @author "Freya19"
     * @date 2020-04-19 20:19:22
     * @param feed 实例对象
     * @return 影响行数
     */
    int insert(Feed feed);

    /**
     * @Description 修改数据
     * @author "Freya19"
     * @date 2020-04-19 20:19:22
     * @param feed 实例对象
     * @return 影响行数
     */
    int update(Feed feed);

    /**
     * @Description 通过主键删除数据
     * @author "Freya19"
     * @date 2020-04-19 20:19:22
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

}
