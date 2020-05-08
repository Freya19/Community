package site.pyyf.forum.dao;

import site.pyyf.forum.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface IDiscussPostMapper {

    List<DiscussPost> selectDiscussPosts(int userId, int orderMode, int categoryId, String tag, int offset, int limit);

    /**
     * @Description 通过ID查询单条数据
     * @author "Gepeng18"
     * @date 2020-03-27 07:57:19
     * @param id 主键
     * @return 实例对象
     */
    DiscussPost queryById(Integer id);

    /**
     * @Description 查询全部数据
     * @author "Gepeng18"
     * @date 2020-03-27 07:57:19
     * @return 对象列表
     */
    List<DiscussPost> queryAll();

    /**
     * @Description 通过实体作为筛选条件查询数据
     * @author "Gepeng18"
     * @date 2020-03-27 07:57:19
     * @param discussPost 实例对象
     * @return 对象列表
     */
    List<DiscussPost> queryAll(DiscussPost discussPost);

    /**
     * @Description 通过实体数量
     * @author "Gepeng18"
     * @date 2020-03-27 07:57:19
     * @return 数量
     */
    int queryCount();

    /**
     * @Description 通过实体作为筛选条件查询数量
     * @author "Gepeng18"
     * @date 2020-03-27 07:57:19
     * @param discussPost 实例对象
     * @return 数量
     */
    int queryCount(DiscussPost discussPost);

    /**
     * @Description 通过实体作为筛选条件查询指定行
     * @author "Gepeng18"
     * @date 2020-03-27 07:57:19
     * @param discussPost 实例对象
     * @return 对象列表
     */
    List<DiscussPost> queryAllByLimit(@Param("discussPost") DiscussPost discussPost, @Param("orderMode") int orderMode, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * @Description 新增数据
     * @author "Gepeng18"
     * @date 2020-03-27 07:57:19
     * @param discussPost 实例对象
     * @return 影响行数
     */
    int insert(DiscussPost discussPost);

    /**
     * @Description 修改数据
     * @author "Gepeng18"
     * @date 2020-03-27 07:57:19
     * @param discussPost 实例对象
     * @return 影响行数
     */
    int update(DiscussPost discussPost);

    /**
     * @Description 通过主键删除数据
     * @author "Gepeng18"
     * @date 2020-03-27 07:57:19
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

    List<DiscussPost> queryAllByLimitByPersonalized(List<Integer> publishedIds, List<Integer> likedIds, List<Integer> commentIds, List<String> tags, int deadline, int offset, int limit);
}
