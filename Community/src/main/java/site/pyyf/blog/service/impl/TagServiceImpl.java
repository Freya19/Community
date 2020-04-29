package site.pyyf.blog.service.impl;

import site.pyyf.blog.service.ITagService;
import site.pyyf.community.entity.Tag;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description (Tag)表服务实现类
 *
 * @author "Gepeng18"
 * @since 2020-03-25 21:36:12
 */
@Service
public class TagServiceImpl extends BaseService implements ITagService {


    /**
     * @Description 通过ID查询单条数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public Tag queryById(Integer id) {
        return iTagMapper.queryById(id);
    }

    /**
     * @Description 查询多条数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    @Override
    public List<Tag> queryAllByLimit(int offset, int limit) {
        return iTagMapper.queryAllByLimit(Tag.builder().build(),offset, limit);
    }
    
    /**
     * @Description 查询全部数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @return 对象列表
     */
    @Override
    public List<Tag> queryAll() {
        return iTagMapper.queryAll();
    }
    
     /**
     * @Description 实体作为筛选条件查询数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @param tag 实例对象
     * @return 对象列表
     */
    @Override
    public List<Tag> queryAll(Tag tag) {
        return iTagMapper.queryAll(tag);
    }
    
    /**
     * @Description 查询实体数量
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @return 数量
     */
    @Override
    public int queryCount() {
        return iTagMapper.queryCount();
    }
    
    /**
     * @Description 实体作为筛选条件查询数量
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @param tag 实例对象
     * @return 数量
     */
    @Override
    public int queryCount(Tag tag) {
        return iTagMapper.queryCount(tag);
    }

    /**
     * @Description 实体作为筛选条件查询指定行
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @param tag 实例对象
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    @Override
    public List<Tag> queryAllByLimit(Tag tag,int offset, int limit) {
        return iTagMapper.queryAllByLimit(tag,offset, limit);
    }
    
    /**
     * @Description 新增数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @param tag 实例对象
     * @return 实例对象
     */
    @Override
    public Tag insert(Tag tag) {
        this.iTagMapper.insert(tag);
        return tag;
    }

    /**
     * @Description 修改数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @param tag 实例对象
     * @return 实例对象
     */
    @Override
    public Tag update(Tag tag) {
        this.iTagMapper.update(tag);
        return queryById(tag.getId());
    }

    /**
     * @Description 通过主键删除数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(Integer id) {
        return iTagMapper.deleteById(id) > 0;
    }
}