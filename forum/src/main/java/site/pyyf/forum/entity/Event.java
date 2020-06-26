package site.pyyf.forum.entity;

import lombok.*;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
@Builder
public class Event {

    private String topic;

    private int userId;

    private int entityType;

    private int entityId;

    private int entityUserId;

    //附加信息
    private Map<String, Object> data = new HashMap<>();

    public Event setData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

    /**
     * 发送邮件：
     *       topic:邮件  data:{"username": 昵称,
     *                         "url":邮件内容中的跳转url,
     *                         "tile":email的title,
     *                         "content":邮件内容,
     *                         "emailType":通知博客写作者,
     *                         "email":对方的邮箱}
     */

    /**
     * 点赞： A点赞了B的帖子
     *       topic:点赞  userId: A  entityType:帖子  entityId:帖子Id  entityUserId:B
     */

    /**
     * 看帖： A看了帖子
     *      topic:看帖   userId: A  entityType:帖子  entityId:帖子Id  data: {"viewTags":所有的标签}
     */

    /**
     * 删帖：A删除了某个帖子
     *      topic:删帖   userId:A   entityType:帖子  entityId:帖子Id
     *  当然，消费端只利用 entityId 就可以完成消费（删除es)
     */

    /**
     * 发帖：A发布了某个帖子
     *      topic:发布帖子   userId:A   entityType:帖子  entityId:帖子Id
     *  当然，消费端只利用 entityId 就可以完成消费（加入es+推feed)
     */

}
