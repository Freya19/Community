package site.pyyf.community.dao;

import site.pyyf.community.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Description (Tag)表数据库访问层
 *
 * @author "Gepeng18"
 * @since 2020-03-25 21:36:12
 */
@Mapper
public interface ITagMapper {

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
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @return 对象列表
     */
    List<Tag> queryAll();

    /**
     * @Description 通过实体作为筛选条件查询数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @param tag 实例对象
     * @return 对象列表
     */
    List<Tag> queryAll(Tag tag);

    /**
     * @Description 通过实体数量
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @return 数量
     */
    int queryCount();
    
     /**
     * @Description 通过实体作为筛选条件查询数量
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @param tag 实例对象
     * @return 数量
     */
    int queryCount(Tag tag);

    /**
     * @Description 通过实体作为筛选条件查询指定行
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @param tag 实例对象
     * @return 对象列表
     */
    List<Tag> queryAllByLimit(@Param("tag") Tag tag, @Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * @Description 新增数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @param tag 实例对象
     * @return 影响行数
     */
    int insert(Tag tag);

    /**
     * @Description 修改数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @param tag 实例对象
     * @return 影响行数
     */
    int update(Tag tag);

    /**
     * @Description 通过主键删除数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

}