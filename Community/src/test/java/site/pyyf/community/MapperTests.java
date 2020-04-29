package site.pyyf.community;

import site.pyyf.CommunityApplication;
import site.pyyf.community.dao.IUserMapper;
import site.pyyf.community.entity.Message;
import site.pyyf.community.entity.User;
import site.pyyf.community.dao.IDiscussPostMapper;
import site.pyyf.community.dao.IMessageMapper;
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
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {

    @Autowired
    private IUserMapper iUserMapper;

    @Autowired
    private IDiscussPostMapper discussPostMapper;

    @Autowired
    private IMessageMapper messageMapper;

    @Test
    public void testSelectUser() {
        User user = iUserMapper.queryById(101);
        System.out.println(user);

        user = iUserMapper.queryAll(User.builder().username("liubei").build()).get(0);
        System.out.println(user);

        user = iUserMapper.queryAll(User.builder().username("pyyf").build()).get(0);
        System.out.println(user);
    }

    @Test
    public void testInsertUser() {
        User user = new User();
        user.setUsername("test");
        user.setPassword("123456");
        user.setSalt("abc");
        user.setEmail("test@qq.com");
        user.setHeaderUrl("https://pyyf.oss-cn-hangzhou.aliyuncs.com/community/icons/cloud.png");
        user.setCreateTime(new Date());

        int rows = iUserMapper.insert(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

    @Test
    public void updateUser() {
        final User user = iUserMapper.queryById(150);
        user.setStatus(1);
        int rows = iUserMapper.update(user);
        System.out.println(rows);

        user.setHeaderUrl("https://pyyf.oss-cn-hangzhou.aliyuncs.com/community/icons/cloud.png");
        rows = iUserMapper.update(user );
        System.out.println(rows);

        user.setPassword("hello");
        rows = iUserMapper.update(user);
        System.out.println(rows);
    }

//    @Test
//    public void testSelectPosts() {
//        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(149, 0,0, 0, 10);
//        for (DiscussPost post : list) {
//            System.out.println(post);
//        }
//
//        int rows = discussPostMapper.selectDiscussPostRows(149,0);
//        System.out.println(rows);
//    }


    @Test
    public void testSelectLetters() {
        List<Message> list = messageMapper.selectConversations(111, 0, 20);
        for (Message message : list) {
            System.out.println(message);
        }

        int count = messageMapper.selectConversationCount(111);
        System.out.println(count);

        list = messageMapper.selectLetters("111_112", 0, 10);
        for (Message message : list) {
            System.out.println(message);
        }

        count = messageMapper.selectLetterCount("111_112");
        System.out.println(count);

        count = messageMapper.selectLetterUnreadCount(131, "111_131");
        System.out.println(count);

    }

}
