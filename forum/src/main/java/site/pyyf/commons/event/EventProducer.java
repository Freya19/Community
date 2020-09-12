package site.pyyf.commons.event;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import site.pyyf.commons.utils.RedisKeyUtil;
import site.pyyf.forum.entity.Event;


/**
 * Created by nowcoder on 2016/7/30.
 */
@Service
public class EventProducer {

    @Autowired
    protected RedisTemplate redisTemplate;

    public boolean fireEvent(Event event) {
        try {
            String json = JSONObject.toJSONString(event);
            redisTemplate.opsForList().leftPush(RedisKeyUtil.getTopicKey(), json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
