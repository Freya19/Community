package site.pyyf.community.service;

import site.pyyf.community.entity.Category;

import java.util.List;

/**
 * @Description (Category)表服务接口
 *
 * @author "Gepeng18"
 * @since 2020-03-25 21:36:11
 */
public interface ICategoryService {

    /**
     * @Description 通过ID查询单条数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:11
     * @param id 主键
     * @return 实例对象
     */
    Category queryById(Integer id);
    
     /**
     * @Description 查询全部数据
     * @author makejava
     * @date 2020-03-25 21:36:11
     * @return 对象列表
     */
    List<Category> queryAll();
    
    /**
     * @Description 实体作为筛选条件查询数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:11
     * @param category 实例对象
     * @return 对象列表
     */
    List<Category> queryAll(Category category);
   
    /**
     * @Description 查询实体数量
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:11
     * @return 数量
     */
    int queryCount();
    
    /**
     * @Description 实体作为筛选条件查询数量
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:11
     * @param category 实例对象
     * @return 数量
     */
    int queryCount(Category category);
   
   
    /**
     * @Description 查询多条数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:11
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<Category> queryAllByLimit(int offset, int limit);

    /**
     * @Description 实体作为筛选条件查询指定行
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:11
     * @param category 实例对象
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<Category> queryAllByLimit(Category category, int offset, int limit);

    
    /**
     * @Description 新增数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:11
     * @param category 实例对象
     * @return 实例对象
     */
    Category insert(Category category);

    /**
     * @Description 修改数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:11
     * @param category 实例对象
     * @return 实例对象
     */
    Category update(Category category);

    /**
     * @Description 通过主键删除数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:11
     * @param id 主键
     * @return 是否成功
     */
    boolean deleteById(Integer id);

}