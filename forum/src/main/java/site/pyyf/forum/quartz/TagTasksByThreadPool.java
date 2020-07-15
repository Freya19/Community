package site.pyyf.forum.quartz;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import site.pyyf.commons.utils.TagCache;
import site.pyyf.forum.dao.IDiscussPostMapper;
import site.pyyf.forum.entity.DiscussPost;
import site.pyyf.forum.entity.Tag;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.Future;

/**
 * 定时统计帖子标签
 */
@Component
@Slf4j
public class TagTasksByThreadPool {

    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    @Autowired
    private TagCache tagCache;

    @Autowired
    private IDiscussPostMapper iDiscussPostMapper;

    @PostConstruct
    public void calculateTags() {
        TagTask task = new TagTask();
        threadPoolTaskScheduler.scheduleAtFixedRate(task, 1000 * 60 * 60 * 3);
    }


    class TagTask implements Runnable {

        @Override
        public void run() {
            /* 1. 分批次取数据进行统计 */
            int offset = 0;
            int limit = 20;
            log.info("hotTagSchedule start {}", new Date());

            List<DiscussPost> discussPosts = new ArrayList<>();
            Set<String> allTags = new HashSet<>();
            /* ------------------- 先统计出帖子的所有标签名 ----------------- */
            // 60
            // 0  < 60
            // 20 < 60
            // 40 < 60
            // 60 < 60 结束
            while (offset < discussPosts.size()) {
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

            /* 2. 根据每个标签名搜出对应的帖子数量，并统计帖子总分，组成一个tag  */
            List<Tag> tagsVO = new ArrayList<>();
            for (String tagName : allTags) {
                discussPosts = iDiscussPostMapper.queryAll(DiscussPost.builder().tags(tagName).build());
                //设置标签中帖子的数量
                Tag tag = new Tag().setCount(discussPosts.size());
                double scoreSum = 0;
                for (DiscussPost discussPost : discussPosts) {
                    scoreSum += discussPost.getScore();
                }
                //标签的名字
                tag.setName(tagName);
                //标签中帖子的分数（排名）
                tag.putData("score", scoreSum);
                //最后将这个tag放到这里
                tagsVO.add(tag);
            }

            /* 3. 将tagsVO通过updateTags进行处理 */
            tagCache.setTags(tagsVO);
            log.info("tagSchedule stop {}", new Date());
        }
    }
}

