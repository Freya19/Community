package site.pyyf.community.quartz;

import site.pyyf.commons.utils.HotTagCache;
import site.pyyf.community.dao.IDiscussPostMapper;
import site.pyyf.community.entity.DiscussPost;
import site.pyyf.community.entity.HotTag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * Created by codedrinker on 2019/8/1.
 */
@Component
@Slf4j
public class HotTagTasks {

    @Autowired
    private IDiscussPostMapper iDiscussPostMapper;

    @Autowired
    private HotTagCache hotTagCache;

    @PostConstruct
    @Scheduled(fixedRate = 1000 * 60*60*3)
    public void hotTagSchedule() {
        int offset = 0;
        int limit = 20;
        log.info("hotTagSchedule start {}", new Date());
        List<DiscussPost> discussPosts = new ArrayList<>();

        Map<String, HotTag> hotTags = new HashMap<>();
        /* ------------------- 统计所有帖子中的标签的分数和数量，存在hotTag中 ----------------- */
        /* ------------------- name为key， count和score为value ----------------- */
        while (offset == 0 || discussPosts.size() == limit) {
            discussPosts = iDiscussPostMapper.queryAllByLimit(DiscussPost.builder().build(),0,offset, limit);
            for (DiscussPost discussPost : discussPosts) {
                String[] tags = StringUtils.split(discussPost.getTags(), ",|，");
                if(tags==null)
                    continue;
                for (String tag : tags) {
                    if(tag.equals("-1"))
                        continue;
                    HotTag hotTag = hotTags.get(tag);
                    if (hotTag != null) {
                        hotTag.setScore(hotTag.getScore() + discussPost.getScore());
                        hotTag.setCount(hotTag.getCount() + 1);
                    } else {
                        hotTags.put(tag, new HotTag(tag,discussPost.getScore(),1));
                    }
                }
            }
            offset += limit;
        }
        /* ------------------- 将hotTags通过updateTags进行处理 ----------------- */
        hotTagCache.setTags(hotTags);
        log.info("hotTagSchedule stop {}", new Date());
    }
}
