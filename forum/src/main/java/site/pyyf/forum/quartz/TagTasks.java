package site.pyyf.forum.quartz;

import site.pyyf.commons.utils.TagCache;
import site.pyyf.forum.dao.IDiscussPostMapper;
import site.pyyf.forum.entity.DiscussPost;
import site.pyyf.forum.entity.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * @author Gepeng18
 * @date 2019/8/1
 */
@Component
@Slf4j
public class TagTasks {

    @Autowired
    private IDiscussPostMapper iDiscussPostMapper;

    @Autowired
    private TagCache tagCache;

    @PostConstruct
    @Scheduled(fixedRate = 1000 * 60 * 60 * 3)
    public void hotTagSchedule() {
        //分批次取数据进行统计
        int offset = 0;
        int limit = 20;
        log.info("hotTagSchedule start {}", new Date());
        List<DiscussPost> discussPosts = new ArrayList<>();

        Set<String> allTags = new HashSet<>();
        /* ------------------- 先统计出帖子的所有标签名 ----------------- */
        while (discussPosts.size() >= offset) {
            discussPosts = iDiscussPostMapper.queryAllByLimit(DiscussPost.builder().build(), 0, offset, limit);
            for (DiscussPost discussPost : discussPosts) {
                //tags字段一定有值
                String[] tags = StringUtils.split(discussPost.getTags(), ",|，");
                if ("-1".equals(tags[0])) {
                    continue;
                }
                for (String tag : tags) {
                    allTags.add(tag);
                }
            }
            offset += limit;
        }

        /* ------------------- 根据每个标签名搜出对应的帖子数量，并统计帖子总分，组成一个tag ----------------- */
        List<Tag> tagsVO = new ArrayList<>();
        for(String tagName: allTags){
            discussPosts = iDiscussPostMapper.queryAll(DiscussPost.builder().tags(tagName).build());
            Tag tag = new Tag().setCount(discussPosts.size());
            double scoreSum = 0;
            for(DiscussPost discussPost:discussPosts){
                scoreSum += discussPost.getScore();
            }
            tag.setName(tagName);
            tag.putData("score", scoreSum);
            tagsVO.add(tag);
        }

        /* ------------------- 将tagsVO通过updateTags进行处理 ----------------- */
        tagCache.setTags(tagsVO);
        log.info("tagSchedule stop {}", new Date());
    }
}
