package site.pyyf.blog.service;

import site.pyyf.blog.entity.Blog;

import java.util.List;

/**
 * @Description (Blog)表服务接口
 *
 * @author "Gepeng18"
 * @since 2020-03-25 20:55:20
 */
public interface IBlogService {

    /**
     * @Description 通过ID查询单条数据
     * @author "Gepeng18"
     * @date 2020-03-25 20:55:20
     * @param id 主键
     * @return 实例对象
     */
    Blog queryById(Integer id);
    
     /**
     * @Description 查询全部数据
     * @author makejava
     * @date 2020-03-25 20:55:20
     * @return 对象列表
     */
    List<Blog> queryAll();
    
    /**
     * @Description 实体作为筛选条件查询数据
     * @author "Gepeng18"
     * @date 2020-03-25 20:55:20
     * @param blog 实例对象
     * @return 对象列表
     */
    List<Blog> queryAll(Blog blog);

    /**
     * @Description 查询实体数量
     * @author "Gepeng18"
     * @date 2020-03-25 20:56:51
     * @return 数量
     */
    int queryCount();

    /**
     * @Description 实体作为筛选条件查询数量
     * @author "Gepeng18"
     * @date 2020-03-25 20:55:20
     * @param blog 实例对象
     * @return 数量
     */
    int queryCount(Blog blog);
   
   
    /**
     * @Description 查询多条数据
     * @author "Gepeng18"
     * @date 2020-03-25 20:55:20
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<Blog> queryAllByLimit(int offset, int limit);

    /**
     * @Description 实体作为筛选条件查询指定行
     * @author "Gepeng18"
     * @date 2020-03-25 20:55:20
     * @param blog 实例对象
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<Blog> queryAllByLimit(Blog blog, int offset, int limit);

    
    /**
     * @Description 新增数据
     * @author "Gepeng18"
     * @date 2020-03-25 20:55:20
     * @param blog 实例对象
     * @return 实例对象
     */
    Blog insert(Blog blog);

    /**
     * @Description 修改数据
     * @author "Gepeng18"
     * @date 2020-03-25 20:55:20
     * @param blog 实例对象，tagIds 对应的标签字符串
     * @return 实例对象
     */
    Blog update(Blog blog);

    /**
     * @Description 通过主键删除数据
     * @author "Gepeng18"
     * @date 2020-03-25 20:55:20
     * @param id 主键
     * @return 是否成功
     */
    boolean deleteById(Integer id);

    List<String> queryGroupByMonth();

    List<Blog> queryByMonth(String month);

}