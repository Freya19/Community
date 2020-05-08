package site.pyyf.blog.dao;

import site.pyyf.blog.entity.Blog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description (Blog)表数据库访问层
 *
 * @author "Gepeng18"
 * @since 2020-03-25 20:47:27
 */
@Mapper
public interface IBlogMapper {

    /**
     * @Description 通过ID查询单条数据
     * @author "Gepeng18"
     * @date 2020-03-25 20:47:27
     * @param id 主键
     * @return 实例对象
     */
    Blog queryById(Integer id);

    /**
     * @Description 查询全部数据
     * @author "Gepeng18"
     * @date 2020-03-25 20:47:27
     * @return 对象列表
     */
    List<Blog> queryAll();

    /**
     * @Description 通过实体作为筛选条件查询数据
     * @author "Gepeng18"
     * @date 2020-03-25 20:47:27
     * @param blog 实例对象
     * @return 对象列表
     */
    List<Blog> queryAll(Blog blog);

    /**
     * @Description 查询实体数量
     * @author "Gepeng18"
     * @date 2020-03-25 20:47:27
     * @return 数量
     */
    int queryCount();

     /**
     * @Description 通过实体作为筛选条件查询数量
     * @author "Gepeng18"
     * @date 2020-03-25 20:47:27
     * @param blog 实例对象
     * @return 数量
     */
    int queryCount(Blog blog);

    /**
     * @Description 通过实体作为筛选条件查询指定行
     * @author "Gepeng18"
     * @date 2020-03-25 20:47:27
     * @param blog 实例对象
     * @return 对象列表
     */
    List<Blog> queryAllByLimit(@Param("blog") Blog blog, @Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * @Description 新增数据
     * @author "Gepeng18"
     * @date 2020-03-25 20:47:27
     * @param blog 实例对象
     * @return 影响行数
     */
    int insert(Blog blog);

    /**
     * @Description 修改数据
     * @author "Gepeng18"
     * @date 2020-03-25 20:47:27
     * @param blog 实例对象
     * @return 影响行数
     */
    int update(Blog blog);

    /**
     * @Description 通过主键删除数据
     * @author "Gepeng18"
     * @date 2020-03-25 20:47:27
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

    List<String> queryGroupByMonth();

    List<Blog> queryByMonth(String month);
}