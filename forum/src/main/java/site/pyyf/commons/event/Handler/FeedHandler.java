package site.pyyf.commons.event.Handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import site.pyyf.blog.controller.BaseController;
import site.pyyf.commons.event.EventHandler;
import site.pyyf.commons.utils.CommunityConstant;
import site.pyyf.commons.utils.RedisKeyUtil;
import site.pyyf.forum.entity.Event;
import site.pyyf.forum.entity.Feed;
import site.pyyf.forum.entity.User;
import site.pyyf.forum.service.IFollowService;
import site.pyyf.forum.service.impl.MessageService;

import java.util.*;

@Component
public class FeedHandler extends BaseController implements EventHandler, CommunityConstant {

    @Autowired
    protected RedisTemplate redisTemplate;

    @Autowired
    private IFollowService iFollowService;

    @Override
    public void doHandle(Event event) {
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
