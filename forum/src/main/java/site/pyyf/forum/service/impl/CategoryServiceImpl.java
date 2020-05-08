package site.pyyf.forum.service.impl;

import site.pyyf.forum.entity.Category;
import site.pyyf.forum.service.ICategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description (Category)表服务实现类
 *
 * @author "Gepeng18"
 * @since 2020-03-25 21:36:11
 */
@Service
public class CategoryServiceImpl extends BaseService implements ICategoryService {


    /**
     * @Description 通过ID查询单条数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:11
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public Category queryById(Integer id) {
        return iCategoryMapper.queryById(id);
    }

    /**
     * @Description 查询多条数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:11
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    @Override
    public List<Category> queryAllByLimit(int offset, int limit) {
        return iCategoryMapper.queryAllByLimit(Category.builder().build(),offset, limit);
    }
    
    /**
     * @Description 查询全部数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:11
     * @return 对象列表
     */
    @Override
    public List<Category> queryAll() {
        return iCategoryMapper.queryAll();
    }
    
     /**
     * @Description 实体作为筛选条件查询数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:11
     * @param category 实例对象
     * @return 对象列表
     */
    @Override
    public List<Category> queryAll(Category category) {
        return iCategoryMapper.queryAll(category);
    }
    
    /**
     * @Description 查询实体数量
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:11
     * @return 数量
     */
    @Override
    public int queryCount() {
        return iCategoryMapper.queryCount();
    }
    
    /**
     * @Description 实体作为筛选条件查询数量
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:11
     * @param category 实例对象
     * @return 数量
     */
    @Override
    public int queryCount(Category category) {
        return iCategoryMapper.queryCount(category);
    }

    /**
     * @Description 实体作为筛选条件查询指定行
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:11
     * @param category 实例对象
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    @Override
    public List<Category> queryAllByLimit(Category category,int offset, int limit) {
        return iCategoryMapper.queryAllByLimit(category,offset, limit);
    }
    
    /**
     * @Description 新增数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:11
     * @param category 实例对象
     * @return 实例对象
     */
    @Override
    public Category insert(Category category) {
        this.iCategoryMapper.insert(category);
        return category;
    }

    /**
     * @Description 修改数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:11
     * @param category 实例对象
     * @return 实例对象
     */
    @Override
    public Category update(Category category) {
        this.iCategoryMapper.update(category);
        return queryById(category.getId());
    }

    /**
     * @Description 通过主键删除数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:11
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(Integer id) {
        return iCategoryMapper.deleteById(id) > 0;
    }
}