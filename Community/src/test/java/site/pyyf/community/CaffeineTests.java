package site.pyyf.community;

import site.pyyf.CommunityApplication;
import site.pyyf.community.entity.DiscussPost;
import site.pyyf.community.service.impl.DiscussPostService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class CaffeineTests {

    @Autowired
    private DiscussPostService postService;

    @Test
    public void initDataForTest() {
        for (int i = 0; i < 300000; i++) {
            DiscussPost post = new DiscussPost();
            post.setUserId(111);
            post.setTitle("互联网求职暖春计划");
            post.setContent("今年的就业形势，确实不容乐观。过了个年，仿佛跳水一般，整个讨论区哀鸿遍野！19届真的没人要了吗？！18届被优化真的没有出路了吗？！大家的“哀嚎”与“悲惨遭遇”牵动了每日潜伏于讨论区的鹏方论坛小哥哥小姐姐们的心，于是鹏方论坛决定：是时候为大家做点什么了！为了帮助大家度过“寒冬”，鹏方论坛特别联合60+家企业，开启互联网求职暖春计划，面向18届&19届，拯救0 offer！");
            post.setCreateTime(new Date());
            post.setScore(Math.random() * 2000);
            postService.insert(post);
        }
    }

//    @Test
//    public void testCache() {
//        System.out.println(postService.findDiscussPosts(0, 0, 10, 1,0));
//        System.out.println(postService.findDiscussPosts(0, 0, 10, 1,0));
//        System.out.println(postService.findDiscussPosts(0, 0, 10, 1,0));
//        System.out.println(postService.findDiscussPosts(0, 0, 10, 0,0));
//    }

}
