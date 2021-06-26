package site.pyyf.forum.service.impl;

import site.pyyf.forum.entity.Comment;
import site.pyyf.forum.entity.Event;
import site.pyyf.commons.utils.CommunityConstant;
import site.pyyf.forum.service.ICommentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description (Comment)表服务实现类
 *
 * @author "Gepeng18"
 * @since 2020-03-25 20:56:51
 */
@Service
public class CommentServiceImpl extends BaseService implements ICommentService, CommunityConstant {

    @Value("${blog.path.domain}")
    private String domain;

    /**
     * @Description 通过ID查询单条数据
     * @author "Gepeng18"
     * @date 2020-03-25 20:56:51
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public Comment queryById(Integer id) {
        return iCommentMapper.queryById(id);
    }

    /**
     * @Description 查询多条数据
     * @author "Gepeng18"
     * @date 2020-03-25 20:56:51
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    @Override
    public List<Comment> queryAllByLimit(int offset, int limit) {
        return iCommentMapper.queryAllByLimit(Comment.builder().build(),offset, limit);
    }

    /**
     * @Description 查询全部数据
     * @author "Gepeng18"
     * @date 2020-03-25 20:56:51
     * @return 对象列表
     */
    @Override
    public List<Comment> queryAll() {
        return iCommentMapper.queryAll();
    }

    /**
     * @Description 实体作为筛选条件查询数据
     * @author "Gepeng18"
     * @date 2020-03-25 20:56:51
     * @param comment 实例对象
     * @return 对象列表
     */
    @Override
    public List<Comment> queryAll(Comment comment) {
        return iCommentMapper.queryAll(comment);
    }

    /**
     * @Description 查询实体数量
     * @author "Gepeng18"
     * @date 2020-03-25 20:56:51
     * @return 数量
     */
    @Override
    public int queryCount() {
        return iCommentMapper.queryCount();
    }

    /**
     * @Description 实体作为筛选条件查询数量
     * @author "Gepeng18"
     * @date 2020-03-25 20:56:51
     * @param comment 实例对象
     * @return 数量
     */
    @Override
    public int queryCount(Comment comment) {
        return iCommentMapper.queryCount(comment);
    }

    /**
     * @Description 实体作为筛选条件查询指定行
     * @author "Gepeng18"
     * @date 2020-03-25 20:56:51
     * @param comment 实例对象
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    @Override
    public List<Comment> queryAllByLimit(Comment comment,int offset, int limit) {
        return iCommentMapper.queryAllByLimit(comment,offset, limit);
    }

    /**
     * @Description 新增数据
     * @author "Gepeng18"
     * @date 2020-03-25 20:56:51
     * @param comment 实例对象
     * @return 实例对象
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public Comment insert(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        // 添加评论
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        int rows = iCommentMapper.insert(comment);

        // 更新帖子评论数量
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            int count = iCommentMapper.queryCount(Comment.builder().entityType(comment.getEntityType()).entityId(comment.getEntityId()).build());
            discussPostService.updateCommentCount(comment.getEntityId(), count);
        }

        return comment;
    }

    /**
     * @Description 修改数据
     * @author "Gepeng18"
     * @date 2020-03-25 20:56:51
     * @param comment 实例对象
     * @return 实例对象
     */
    @Override
    public Comment update(Comment comment) {
        this.iCommentMapper.update(comment);
        return queryById(comment.getId());
    }

    /**
     * @Description 通过主键删除数据
     * @author "Gepeng18"
     * @date 2020-03-25 20:56:51
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(Integer id) {
        return iCommentMapper.deleteById(id) > 0;
    }

    @Override
    public void notifyUser(int emailType, String username, String email, String blogId, String blogTitle, String commentContent){
        Map<String, Object> emailSetting = new HashMap<>();
        String url = domain  + "/blog/" + blogId;
        emailSetting.put("username",username);
        emailSetting.put("url",url);
        emailSetting.put("title",blogTitle);
        emailSetting.put("content",commentContent);
        emailSetting.put("emailType",emailType);
        emailSetting.put("email",email);
        Event event = Event.builder().topic(TOPIC_NOTIFY).data(emailSetting).build();
        eventProducer.fireEvent(event);

    }


}