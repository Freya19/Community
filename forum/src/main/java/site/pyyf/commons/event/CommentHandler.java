package site.pyyf.commons.event;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import site.pyyf.blog.controller.BaseController;
import site.pyyf.commons.utils.CommunityConstant;
import site.pyyf.commons.utils.RedisKeyUtil;
import site.pyyf.forum.entity.Event;
import site.pyyf.forum.entity.Feed;
import site.pyyf.forum.entity.Message;
import site.pyyf.forum.entity.User;
import site.pyyf.forum.service.IFollowService;
import site.pyyf.forum.service.impl.MessageService;
import java.util.*;

@Deprecated
@Component
public class CommentHandler extends BaseController implements EventHandler, CommunityConstant {

    @Autowired
    protected RedisTemplate redisTemplate;

    @Autowired
    private MessageService messageService;

    @Autowired
    private IFollowService iFollowService;

    @Override
    public void doHandle(Event event) {
        // 发送站内通知
        // （userId点赞了某个帖子，实体记录在content中（谁点赞的，点赞了哪个帖子，这是实体，都记录在content中），
        // 这个通知要发给谁，记录在message的toid中，点赞操作记录在conversationId中
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());


        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());

        if (!event.getData().isEmpty()) {
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }

        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);

        /* ------------------- 帖子则发feed ----------------- */
        //feed表示userId(名字为userName)发布了（feedType）entityId（entityType类型的）
        if (event.getEntityType() == ENTITY_TYPE_POST) {

            Feed feed = Feed.builder().feedType(topicToFeedType(event.getTopic()))
                    .createTime(new Date())
                    .userId(event.getUserId())
                    .userName(iUserService.queryById(event.getUserId()).getUsername())
                    .entityType(event.getEntityType())
                    .entityId(event.getEntityId()).build();
            iFeedService.addFeed(feed);


            // 获得所有粉丝
            List<Map<String, Object>> followers = iFollowService.findFans(feed.getUserId(), 0, Integer.MAX_VALUE);

            // 给所有5天内登陆过的粉丝推事件
            for (Map<String, Object> userAndFollowTime : followers) {
                User user = (User) (userAndFollowTime.get("user"));
                if ((int) ((new Date().getTime() - user.getLoginTime().getTime()) / (1000 * 3600 * 24)) < 5) {
                    // 把这个feed扔到redis的timeline中
                    String timelineKey = RedisKeyUtil.getLatestTimelineKey(user.getId());
                    redisTemplate.opsForList().leftPush(timelineKey, feed);

                    // 限制最长长度，如果timelineKey的长度过大，就删除后面的新鲜事
                    while (redisTemplate.opsForList().size(timelineKey) > FEEDTIMELINECOUNT) {
                        redisTemplate.opsForList().rightPop(timelineKey);
                    }
                }
            }
        }
    }

    @Override
    public List<String> getSupportEventTypes() {
        return Arrays.asList(TOPIC_COMMENT, TOPIC_LIKE, TOPIC_FOLLOW);
    }

    private static int topicToFeedType(String topic) {
        if (topic.equals(TOPIC_LIKE)) {
            return FEED_LIKE;
        } else if (topic.equals(TOPIC_COMMENT)) {
            return FEED_COMMENT;
        } else if (topic.equals(TOPIC_PUBLISH)) {
            return FEED_PUBLISH;
        } else {
            throw new RuntimeException("无效的类别参数");
        }
    }
}
