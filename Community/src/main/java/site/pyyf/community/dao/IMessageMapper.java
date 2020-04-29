package site.pyyf.community.dao;

import site.pyyf.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IMessageMapper {


    /**
     * @Description 通过ID查询单条数据
     * @author "Gepeng18"
     * @date 2020-03-27 08:07:26
     * @param id 主键
     * @return 实例对象
     */
    Message queryById(Integer id);

    /**
     * @Description 查询全部数据
     * @author "Gepeng18"
     * @date 2020-03-27 08:07:26
     * @return 对象列表
     */
    List<Message> queryAll();

    /**
     * @Description 通过实体作为筛选条件查询数据
     * @author "Gepeng18"
     * @date 2020-03-27 08:07:26
     * @param message 实例对象
     * @return 对象列表
     */
    List<Message> queryAll(Message message);

    /**
     * @Description 通过实体数量
     * @author "Gepeng18"
     * @date 2020-03-27 08:07:26
     * @return 数量
     */
    int queryCount();

    /**
     * @Description 通过实体作为筛选条件查询数量
     * @author "Gepeng18"
     * @date 2020-03-27 08:07:26
     * @param message 实例对象
     * @return 数量
     */
    int queryCount(Message message);

    /**
     * @Description 通过实体作为筛选条件查询指定行
     * @author "Gepeng18"
     * @date 2020-03-27 08:07:26
     * @param message 实例对象
     * @return 对象列表
     */
    List<Message> queryAllByLimit(@Param("message") Message message, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * @Description 新增数据
     * @author "Gepeng18"
     * @date 2020-03-27 08:07:26
     * @param message 实例对象
     * @return 影响行数
     */
    int insert(Message message);

    /**
     * @Description 修改数据
     * @author "Gepeng18"
     * @date 2020-03-27 08:07:26
     * @param message 实例对象
     * @return 影响行数
     */
    int update(Message message);

    /**
     * @Description 通过主键删除数据
     * @author "Gepeng18"
     * @date 2020-03-27 08:07:26
     * @param id 主键
     * @return 影响行数
     */
    int deleteById(Integer id);

    // 查询当前用户的会话列表,针对每个会话只返回一条最新的私信.
    List<Message> selectConversations(int userId, int offset, int limit);

    // 查询当前用户的会话数量.
    int selectConversationCount(int userId);

    // 查询某个会话所包含的私信列表.
    List<Message> selectLetters(String conversationId, int offset, int limit);

    // 查询某个会话所包含的私信数量.
    int selectLetterCount(String conversationId);

    // 查询未读私信的数量
    int selectLetterUnreadCount(int userId, String conversationId);

    // 修改消息的状态
    int updateStatus(List<Integer> ids, int status);

    // 查询某个主题下最新的通知
    Message selectLatestNotice(int userId, String topic);

    // 查询某个主题所包含的通知数量
    int selectNoticeCount(int userId, String topic);

    // 查询未读的通知的数量
    int selectNoticeUnreadCount(int userId, String topic);

    // 查询某个主题所包含的通知列表
    List<Message> selectNotices(int userId, String topic, int offset, int limit);

}
