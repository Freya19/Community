package site.pyyf.commons.event.Handler;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import site.pyyf.blog.controller.BaseController;
import site.pyyf.commons.event.EventHandler;
import site.pyyf.commons.utils.CommunityConstant;
import site.pyyf.forum.entity.Event;
import site.pyyf.forum.entity.Message;
import site.pyyf.forum.service.IFollowService;
import site.pyyf.forum.service.impl.MessageService;

import java.util.*;

public class MessageHandler extends BaseController implements EventHandler, CommunityConstant {

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
    }

    @Override
    public List<String> getSupportEventTypes() {
        return Arrays.asList(TOPIC_COMMENT, TOPIC_LIKE, TOPIC_FOLLOW);
    }
}
