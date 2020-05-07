package site.pyyf.community.quartz;

import site.pyyf.commons.utils.TagCache;
import site.pyyf.community.dao.IDiscussPostMapper;
import site.pyyf.community.entity.DiscussPost;
import site.pyyf.community.entity.Tag;
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

        Map<String, Tag> hotTags = new HashMap<>();
        /* ------------------- 统计所有帖子中的标签的分数和数量，存在hotTag中 ----------------- */
        /* ------------------- name为key， count和score为value ----------------- */
        while (discussPosts.size() >= offset) {
            discussPosts = iDiscussPostMapper.queryAllByLimit(DiscussPost.builder().build(), 0, offset, limit);
            for (DiscussPost discussPost : discussPosts) {
                //tags字段一定有值
                String[] tags = StringUtils.split(discussPost.getTags(), ",|，");
                if ("-1".equals(tags[0])) {
                    continue;
                }
                for (String tag : tags) {
                    Tag hotTag = hotTags.get(tag);
                    if (hotTag != null) {
                        hotTag.getData().put("score",((Double)(hotTag.getData().get("score"))).doubleValue() + discussPost.getScore());
                        hotTag.setCount(hotTag.getCount() + 1);
                    } else {
                        hotTags.put(tag, new Tag(tag,1). putData("score",discussPost.getScore()));
                    }
                }
            }
            offset += limit;
        }
        /* ------------------- 将hotTags通过updateTags进行处理 ----------------- */
        tagCache.setTags(hotTags);
        log.info("hotTagSchedule stop {}", new Date());
    }
}
