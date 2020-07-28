package site.pyyf.forum.controller;

import site.pyyf.forum.entity.Event;
import site.pyyf.forum.entity.User;
import site.pyyf.commons.utils.CommunityConstant;
import site.pyyf.commons.utils.CommunityUtil;
import site.pyyf.commons.utils.RedisKeyUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController extends CommunityBaseController implements CommunityConstant {


    @RequestMapping(path = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId, int postId) {
        User user = hostHolder.getUser();

        // 1. 点赞插入redis数据库（双向插入）
        iLikeService.like(user.getId(), entityType, entityId, entityUserId);

        //  -------------------------  下面就开始页面显示了  ----------------------
        // 2. 更新目标的点赞数量
        Long likeCount = iLikeService.findEntityLikeCount(entityType, entityId);

        // 3. 返回点赞状态，利于前端展示
        int likeStatus = iLikeService.findEntityLikeStatus(user.getId(), entityType, entityId);

        // 返回的结果
        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", (long)likeCount);
        map.put("likeStatus", likeStatus);

        // 4. 触发点赞事件，通知被点赞人: 谁 点赞 谁 的哪个帖子
        if (likeStatus == 1) {
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId);
            eventProducer.fireEvent(event);
        }

        //5. 重新计算帖子分数
        if(entityType == ENTITY_TYPE_POST) {
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey, postId);
        }

        return CommunityUtil.getJSONString(0, null, map);
    }

}
