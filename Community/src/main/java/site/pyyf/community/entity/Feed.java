package site.pyyf.community.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Objects;

/**
 * Created by "gepeng" on 2020-04-108 06:08:31.
 * @Description feed流
 * userId 评论 点赞 还是 发布 ,将这些消息推送给每个粉丝
 * @param
 * @return
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feed {
    private Integer id;
    private Integer feedType; //1 评论  2 点赞  3 发布
    private Date createTime;
    //谁点的赞
    private Integer userId;
    private String userName;  //用户名
    private Integer entityType;
    private Integer entityId;

    //利用set去重，如用户点了个赞，又取消了，又点了，则feed中有两个，但我们只取一个
    //所以userid 给 某个帖子（entityType entityId）点了个赞（feedType） -- 只记录一次即可


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Feed feed = (Feed) o;
        return Objects.equals(feedType, feed.feedType) &&
                Objects.equals(userId, feed.userId) &&
                Objects.equals(entityType, feed.entityType) &&
                Objects.equals(entityId, feed.entityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(feedType, userId, entityType, entityId);
    }
}
