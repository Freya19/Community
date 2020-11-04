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
    public void insertMysql() {
        ArrayList<String> keys = new ArrayList<>();
        keys.add("post:DO");
        for (String key : keys) {
            Set objects = redisTemplate.keys(key + "*");
            for (Object object : objects) {
                Object o = redisTemplate.opsForValue().get(object);
                iDiscussPostMapper.insert((DiscussPost) o);
            }
        }
    }

    @Test
    public void printMysql() {
        List<DiscussPost> currentPosts = iDiscussPostMapper.queryAll();
        System.out.println(currentPosts);
    }

    @Test
    public void updateAuthor() {
        int src = 14;
        int dst = 6;
        List<DiscussPost> discussPosts = iDiscussPostMapper.queryAll();
        for (int i = 0; i < discussPosts.size(); i++) {
            if (discussPosts.get(i).getUserId()==src){
                DiscussPost newDis = discussPosts.get(i);

                newDis.setUserId(dst);
                iDiscussPostMapper.update(newDis);

            }
        }
    }


}
