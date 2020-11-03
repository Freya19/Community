package site.pyyf.forum;


import org.apache.commons.lang3.StringUtils;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.security.core.parameters.P;
import site.pyyf.ForumApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import site.pyyf.commons.utils.RedisKeyUtil;
import site.pyyf.forum.dao.IDiscussPostMapper;
import site.pyyf.forum.dao.elasticsearch.DiscussPostRepository;
import site.pyyf.forum.entity.DiscussPost;
import site.pyyf.forum.entity.Tag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = ForumApplication.class)
public class RecoverTests {

    @Autowired
    private IDiscussPostMapper iDiscussPostMapper;

    @Autowired
    protected RedisTemplate redisTemplate;

    @Test
    public void initCache() {
        ArrayList<String> keys = new ArrayList<>();
        keys.add("post:DO");
        for (String key : keys) {
            Set objects = redisTemplate.keys(key + "*");
            for (Object object : objects) {
                Object o = redisTemplate.opsForValue().get(object);
                iDiscussPostMapper.insert((DiscussPost) o);
            }

        }

//        int offset = 0;
//        int limit = 20;
//        List<DiscussPost> allPosts = new ArrayList<>();
//        while (offset <= allPosts.size()) {
//            List<DiscussPost> currentPosts = iDiscussPostMapper.queryAllByLimit(DiscussPost.builder().build(), 0, offset, limit);
//            allPosts.addAll(currentPosts);
//            for (DiscussPost discussPost : currentPosts) {
//                iDiscussPostMapper.insert(discussPost);
//            }
//            offset += limit;
//        }
    }



}
