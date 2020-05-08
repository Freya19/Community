package site.pyyf.forum.service;

import site.pyyf.forum.entity.Message;

import java.util.List;


/**
 * @Description (Message)表服务接口
 *
 * @author "Gepeng18"
 * @since 2020-03-27 08:27:16
 */
public interface IMessageService {

    /**
     * @Description 通过ID查询单条数据
     * @author "Gepeng18"
     * @date 2020-03-27 08:27:16
     * @param id 主键
     * @return 实例对象
     */
    Message queryById(Integer id);
    
     /**
     * @Description 查询全部数据
     * @author makejava
     * @date 2020-03-27 08:27:16
     * @return 对象列表
     */
    List<Message> queryAll();
    
    /**
     * @Description 实体作为筛选条件查询数据
     * @author "Gepeng18"
     * @date 2020-03-27 08:27:16
     * @param message 实例对象
     * @return 对象列表
     */
    List<Message> queryAll(Message message);
   
    /**
     * @Description 查询实体数量
     * @author "Gepeng18"
     * @date 2020-03-27 08:27:16
     * @return 数量
     */
    int queryCount();
    
    /**
     * @Description 实体作为筛选条件查询数量
     * @author "Gepeng18"
     * @date 2020-03-27 08:27:16
     * @param message 实例对象
     * @return 数量
     */
    int queryCount(Message message);
   
   
    /**
     * @Description 查询多条数据
     * @author "Gepeng18"
     * @date 2020-03-27 08:27:16
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<Message> queryAllByLimit(int offset, int limit);

    /**
     * @Description 实体作为筛选条件查询指定行
     * @author "Gepeng18"
     * @date 2020-03-27 08:27:16
     * @param message 实例对象
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<Message> queryAllByLimit(Message message, int offset, int limit);

    
    /**
     * @Description 新增数据
     * @author "Gepeng18"
     * @date 2020-03-27 08:27:16
     * @param message 实例对象
     * @return 实例对象
     */
    Message insert(Message message);

    /**
     * @Description 修改数据
     * @author "Gepeng18"
     * @date 2020-03-27 08:27:16
     * @param message 实例对象
     * @return 实例对象
     */
    Message update(Message message);

    /**
     * @Description 通过主键删除数据
     * @author "Gepeng18"
     * @date 2020-03-27 08:27:16
     * @param id 主键
     * @return 是否成功
     */
    boolean deleteById(Integer id);

    List<Message> findConversations(int userId, int offset, int limit) ;
    
    int findConversationCount(int userId) ;

    List<Message> findLetters(String conversationId, int offset, int limit) ;

    int findLetterCount(String conversationId) ;

    int findLetterUnreadCount(int userId, String conversationId) ;

    int addMessage(Message message) ;

    int readMessage(List<Integer> ids) ;

    Message findLatestNotice(int userId, String topic) ;

    int findNoticeCount(int userId, String topic) ;

    int findNoticeUnreadCount(int userId, String topic);

    List<Message> findNotices(int userId, String topic, int offset, int limit) ;
}