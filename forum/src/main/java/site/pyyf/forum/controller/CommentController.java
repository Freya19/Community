package site.pyyf.forum.controller;

import site.pyyf.forum.entity.Comment;
import site.pyyf.forum.entity.DiscussPost;
import site.pyyf.forum.entity.Event;
import site.pyyf.commons.utils.CommunityConstant;
import site.pyyf.commons.utils.RedisKeyUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController extends CommunityBaseController implements CommunityConstant {

    @RequestMapping(path = "/{discussPostId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        // 1. 将评论插入mysql中，并更新帖子评论数
        iCommentService.insert(comment);

        // 2. 触发评论事件，交由异步处理，通知帖子作者有人评论
        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId", discussPostId);

        //通知哪位少侠
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            DiscussPost target = iDiscussPostService.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        } else if (comment.getEntityType() == ENTITY_TYPE_POST_COMMENT) {
            Comment target = iCommentService.queryById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        eventProducer.fireEvent(event);

        // 3. 触发发帖事件，交由异步处理，将帖子在ES中覆盖
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            event = new Event()
                    .setTopic((TOPIC_PUBLISH))
                    .setUserId(comment.getUserId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(discussPostId);
            eventProducer.fireEvent(event);

            // 4. 将帖子id加入Redis中，交由quartz计算帖子分数（增量计算）
            // 因为计算分数涉及的数据量比较多，所以只将变化（比如新增点赞、评论等）的帖子的id存入Redis中，然后交由Quartz定时取出，重新计算其分数
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey, discussPostId);
        }

        return "redirect:/discuss/" + discussPostId;
    }

}
