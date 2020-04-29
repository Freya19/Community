package site.pyyf.blog.service;

import site.pyyf.community.entity.Tag;

import java.util.List;

/**
 * @Description (Tag)表服务接口
 *
 * @author "Gepeng18"
 * @since 2020-03-25 21:36:12
 */
public interface ITagService {

    /**
     * @Description 通过ID查询单条数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @param id 主键
     * @return 实例对象
     */
    Tag queryById(Integer id);
    
     /**
     * @Description 查询全部数据
     * @author makejava
     * @date 2020-03-25 21:36:12
     * @return 对象列表
     */
    List<Tag> queryAll();
    
    /**
     * @Description 实体作为筛选条件查询数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @param tag 实例对象
     * @return 对象列表
     */
    List<Tag> queryAll(Tag tag);
   
    /**
     * @Description 查询实体数量
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @return 数量
     */
    int queryCount();
    
    /**
     * @Description 实体作为筛选条件查询数量
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @param tag 实例对象
     * @return 数量
     */
    int queryCount(Tag tag);
   
   
    /**
     * @Description 查询多条数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<Tag> queryAllByLimit(int offset, int limit);

    /**
     * @Description 实体作为筛选条件查询指定行
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @param tag 实例对象
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<Tag> queryAllByLimit(Tag tag, int offset, int limit);

    
    /**
     * @Description 新增数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @param tag 实例对象
     * @return 实例对象
     */
    Tag insert(Tag tag);

    /**
     * @Description 修改数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @param tag 实例对象
     * @return 实例对象
     */
    Tag update(Tag tag);

    /**
     * @Description 通过主键删除数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @param id 主键
     * @return 是否成功
     */
    boolean deleteById(Integer id);

}