package site.pyyf.community.dao;

import site.pyyf.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description (Comment)表数据库访问层
 *
 * @author "Gepeng18"
 * @since 2020-03-25 21:36:10
 */
@Mapper
public interface ICommentMapper {

    /**
     * @Description 通过ID查询单条数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:10
     * @param id 主键
     * @return 实例对象
     */
    Comment queryById(Integer id);

    /**
     * @Description 查询全部数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:10
     * @return 对象列表
     */
    List<Comment> queryAll();

    /**
     * @Description 通过实体作为筛选条件查询数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:10
     * @param comment 实例对象
     * @return 对象列表
     */
    List<Comment> queryAll(Comment comment);

    /**
     * @Description 通过实体数量
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:10
     * @return 数量
     */
    int queryCount();
    
     /**
     * @Description 通过实体作为筛选条件查询数量
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:10
     * @param comment 实例对象
     * @return 数量
     */
    int queryCount(Comment comment);

    /**
     * @Description 通过实体作为筛选条件查询指定行
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:10
     * @param comment 实例对象
     * @return 对象列表
     */
    List<Comment> queryAllByLimit(@Param("comment") Comment comment, @Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * @Description 新增数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:10
     * @param comment 实例对象
     * @return 影响行数
     */
    int insert(Comment comment);

    /**
     * @Description 修改数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:10
     * @param comment 实例对象
     * @return 影响行数
     */
    int update(Comment comment);

    /**
     * @Description 通过主键删除数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:10
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

}