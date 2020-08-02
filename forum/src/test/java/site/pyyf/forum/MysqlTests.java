package site.pyyf.forum;

import site.pyyf.ForumApplication;
import site.pyyf.forum.dao.IUserMapper;
import site.pyyf.forum.entity.DiscussPost;
import site.pyyf.forum.entity.Message;
import site.pyyf.forum.entity.User;
import site.pyyf.forum.dao.IDiscussPostMapper;
import site.pyyf.forum.dao.IMessageMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = ForumApplication.class)
public class MysqlTests {

    @Autowired
    public IDiscussPostMapper iDiscussPostMapper;
    @Test
    public void changeTags() {
        List<DiscussPost> discussPosts = iDiscussPostMapper.queryAll(DiscussPost.builder().build());
        for (DiscussPost discussPost:discussPosts){
            String[] tagsName = discussPost.getTags().split(",|，");
            StringBuilder builder = new StringBuilder();
            for(String tagName:tagsName)
                builder.append(tagName.trim().substring(0, 1).toUpperCase() + tagName.trim().substring(1).toLowerCase()).append(",");
            discussPost.setTags(builder.toString().substring(0,builder.length()-1));
            iDiscussPostMapper.update(discussPost);
        }
    }


}
