package site.pyyf.commons.event.Handler;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import site.pyyf.blog.controller.BaseController;
import site.pyyf.commons.event.EventHandler;
import site.pyyf.commons.utils.CommunityConstant;
import site.pyyf.commons.utils.RedisKeyUtil;
import site.pyyf.forum.entity.Event;

import java.util.Arrays;
import java.util.List;


@Component
public class ViewHander extends BaseController implements EventHandler, CommunityConstant {
    @Autowired
    protected RedisTemplate redisTemplate;

    @Override
    public void doHandle(Event event) {
        /* ------------------- 帖子则发feed ----------------- */
        /* ------------------- 将event的data的viewTags中的数据全部取出来放到redis中 ----------------- */
        if (event.getEntityType() == ENTITY_TYPE_POST) {
            String timelineKey = RedisKeyUtil.getLatestViewTagsKey(event.getUserId());

            //fastjson将数组转化为了jsonarray,所以这里先将jsonarry转化为string,再通过parseArray方法转为list
            List<String> tags = JSONObject.parseArray(JSONObject.toJSONString(event.getData().get("viewTags"),
                    SerializerFeature.WriteClassName), String.class);
            if(tags.size()>0){
                for(String tag:tags){
                    redisTemplate.opsForList().leftPush(timelineKey, tag);
                    // 限制最长长度，如果timelineKey的长度过大，就删除后面的新鲜事
                    while (redisTemplate.opsForList().size(timelineKey) > FEEDTAGCOUNT)
                        redisTemplate.opsForList().rightPop(timelineKey);
                }
            }
        }
    }

    @Override
    public List<String> getSupportEventTypes() {
        return Arrays.asList(TOPIC_VIEW);
    }
}
