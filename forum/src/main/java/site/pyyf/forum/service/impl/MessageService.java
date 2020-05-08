package site.pyyf.forum.service.impl;

import site.pyyf.forum.entity.Message;
import site.pyyf.forum.service.IMessageService;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class MessageService extends BaseService implements IMessageService {

    public List<Message> findConversations(int userId, int offset, int limit) {
        return iMessageMapper.selectConversations(userId, offset, limit);
    }

    public int findConversationCount(int userId) {
        return iMessageMapper.selectConversationCount(userId);
    }

    public List<Message> findLetters(String conversationId, int offset, int limit) {
        return iMessageMapper.selectLetters(conversationId, offset, limit);
    }

    public int findLetterCount(String conversationId) {
        return iMessageMapper.selectLetterCount(conversationId);
    }

    public int findLetterUnreadCount(int userId, String conversationId) {
        return iMessageMapper.selectLetterUnreadCount(userId, conversationId);
    }

    public int addMessage(Message message) {
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filter(message.getContent()));
        return iMessageMapper.insert(message);
    }

    public int readMessage(List<Integer> ids) {
        return iMessageMapper.updateStatus(ids, 1);
    }

    public Message findLatestNotice(int userId, String topic) {
        return iMessageMapper.selectLatestNotice(userId, topic);
    }

    public int findNoticeCount(int userId, String topic) {
        return iMessageMapper.selectNoticeCount(userId, topic);
    }

    public int findNoticeUnreadCount(int userId, String topic) {
        return iMessageMapper.selectNoticeUnreadCount(userId, topic);
    }

    public List<Message> findNotices(int userId, String topic, int offset, int limit) {
        return iMessageMapper.selectNotices(userId, topic, offset, limit);
    }
    /**
     * @Description 通过ID查询单条数据
     * @author "Gepeng18"
     * @date 2020-03-27 08:07:27
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public Message queryById(Integer id) {
        return iMessageMapper.queryById(id);
    }

    /**
     * @Description 查询多条数据
     * @author "Gepeng18"
     * @date 2020-03-27 08:07:27
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    @Override
    public List<Message> queryAllByLimit(int offset, int limit) {
        return iMessageMapper.queryAllByLimit(Message.builder().build(),offset, limit);
    }

    /**
     * @Description 查询全部数据
     * @author "Gepeng18"
     * @date 2020-03-27 08:07:27
     * @return 对象列表
     */
    @Override
    public List<Message> queryAll() {
        return iMessageMapper.queryAll();
    }

    /**
     * @Description 实体作为筛选条件查询数据
     * @author "Gepeng18"
     * @date 2020-03-27 08:07:27
     * @param message 实例对象
     * @return 对象列表
     */
    @Override
    public List<Message> queryAll(Message message) {
        return iMessageMapper.queryAll(message);
    }

    /**
     * @Description 查询实体数量
     * @author "Gepeng18"
     * @date 2020-03-27 08:07:27
     * @return 数量
     */
    @Override
    public int queryCount() {
        return iMessageMapper.queryCount();
    }

    /**
     * @Description 实体作为筛选条件查询数量
     * @author "Gepeng18"
     * @date 2020-03-27 08:07:27
     * @param message 实例对象
     * @return 数量
     */
    @Override
    public int queryCount(Message message) {
        return iMessageMapper.queryCount(message);
    }

    /**
     * @Description 实体作为筛选条件查询指定行
     * @author "Gepeng18"
     * @date 2020-03-27 08:07:27
     * @param message 实例对象
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    @Override
    public List<Message> queryAllByLimit(Message message,int offset, int limit) {
        return iMessageMapper.queryAllByLimit(message,offset, limit);
    }

    /**
     * @Description 新增数据
     * @author "Gepeng18"
     * @date 2020-03-27 08:07:27
     * @param message 实例对象
     * @return 实例对象
     */
    @Override
    public Message insert(Message message) {
        this.iMessageMapper.insert(message);
        return message;
    }

    /**
     * @Description 修改数据
     * @author "Gepeng18"
     * @date 2020-03-27 08:07:27
     * @param message 实例对象
     * @return 实例对象
     */
    @Override
    public Message update(Message message) {
        this.iMessageMapper.update(message);
        return queryById(message.getId());
    }

    /**
     * @Description 通过主键删除数据
     * @author "Gepeng18"
     * @date 2020-03-27 08:07:27
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(Integer id) {
        return iMessageMapper.deleteById(id) > 0;
    }

}
