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
public class CacheTests {
    @Autowired
    private IDiscussPostMapper iDiscussPostMapper;

    @Autowired
    protected RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostRepository discussRepository;

    @Autowired
    private ElasticsearchTemplate elasticTemplate;


    @Test
    public void initCache() {
        // 1.1 先统计出帖子的所有标签名
        // 注意这里一开始discussPosts数量为0，所以offset一开始是0，然后搜到20个，则加进去，搜到则加进去，
        // discussPosts加的是实际数据，而offset每次固定加20,因此终有一次，offset会超过discussPosts.size()
        // 等于也需要算上，因为 初始化时 0==0 , 否则进不了循环
        int offset = 0;
        int limit = 20;
        Set<String> allTags = new HashSet<>();
        List<DiscussPost> discussPosts = new ArrayList<>();
        while (offset <= discussPosts.size()) {
            discussPosts.addAll(iDiscussPostMapper.queryAllByLimit(DiscussPost.builder().build(), 0, offset, limit));
            for (DiscussPost discussPost : discussPosts) {
                redisTemplate.opsForZSet().add(RedisKeyUtil.getHotPostsList(), discussPost.getId(), discussPost.getScore());
                // 按照时间查时，一开始是最新的，所以从右边插入即可
                redisTemplate.opsForList().rightPush(RedisKeyUtil.getLatestPostsList(), discussPost.getId());

                //tags字段一定有值
                String[] tags = StringUtils.split(discussPost.getTags(), ",|，");
                for (String tag : tags) {
                    String newTag = tag.trim().substring(0, 1).toUpperCase() + tag.trim().substring(1).toLowerCase();
                    allTags.add(newTag);
                }
            }
            offset += limit;
        }

        // 2. 根据每个标签名搜出对应的帖子数量，并添加到redis的tagName的list中
        for (String tagName : allTags) {
            discussPosts = iDiscussPostMapper.queryAll(DiscussPost.builder().tags(tagName).build());
            for (DiscussPost discussPost : discussPosts) {
                // 使用zset，time作为score进行排序
                redisTemplate.opsForZSet().add(RedisKeyUtil.getTagPostsList(tagName), discussPost.getId(), discussPost.getCreateTime().getTime());
            }
        }

        // 3. 使用redis的hash存储帖子标签及其数量
        for (String tagName : allTags) {
            discussPosts = iDiscussPostMapper.queryAll(DiscussPost.builder().tags(tagName).build());
            redisTemplate.opsForZSet().add(RedisKeyUtil.getTagsCount(), tagName, discussPosts.size());
        }

    }


    @Test
    public void delKey() {
        ArrayList<String> keys = new ArrayList<>();
        keys.add("post");
        keys.add("tag");
        for (String key : keys) {
            redisTemplate.delete(redisTemplate.keys(key+"*"));
        }
    }
}
