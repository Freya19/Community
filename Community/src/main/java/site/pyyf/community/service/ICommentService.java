package site.pyyf.community.service;

import site.pyyf.community.entity.Comment;

import java.util.List;

/**
 * @Description (Comment)表服务接口
 *
 * @author "Gepeng18"
 * @since 2020-03-25 21:36:10
 */
public interface ICommentService {

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
     * @author makejava
     * @date 2020-03-25 21:36:10
     * @return 对象列表
     */
    List<Comment> queryAll();
    
    /**
     * @Description 实体作为筛选条件查询数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:10
     * @param comment 实例对象
     * @return 对象列表
     */
    List<Comment> queryAll(Comment comment);
   
    /**
     * @Description 查询实体数量
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:10
     * @return 数量
     */
    int queryCount();
    
    /**
     * @Description 实体作为筛选条件查询数量
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:10
     * @param comment 实例对象
     * @return 数量
     */
    int queryCount(Comment comment);
   
   
    /**
     * @Description 查询多条数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:10
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<Comment> queryAllByLimit(int offset, int limit);

    /**
     * @Description 实体作为筛选条件查询指定行
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:10
     * @param comment 实例对象
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<Comment> queryAllByLimit(Comment comment, int offset, int limit);


    /**
     * @Description 新增数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:10
     * @param comment 实例对象
     * @return 实例对象
     */
    Comment insert(Comment comment);

    /**
     * @Description 修改数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:10
     * @param comment 实例对象
     * @return 实例对象
     */
    Comment update(Comment comment);

    /**
     * @Description 通过主键删除数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:10
     * @param id 主键
     * @return 是否成功
     */
    boolean deleteById(Integer id);

    void notifyUser(int emailType, String username, String email, String blogId, String blogTitle, String commentContent);

}