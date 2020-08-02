package site.pyyf.forum.quartz;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import site.pyyf.forum.entity.DiscussPost;
import site.pyyf.forum.service.impl.DiscussPostService;
import site.pyyf.forum.service.impl.ElasticsearchService;
import site.pyyf.forum.service.impl.LikeService;
import site.pyyf.commons.utils.CommunityConstant;
import site.pyyf.commons.utils.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Slf4j
public class PostScoreRefreshJob implements CommunityConstant {

    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    // 鹏方论坛纪元
    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化鹏方论坛纪元失败!", e);
        }
    }

    @PostConstruct
    public void calculateTags() {
        ScoreTask task = new ScoreTask();
        threadPoolTaskScheduler.scheduleAtFixedRate(task, 1000 * 60 * 60 * 3);
    }

    class ScoreTask implements Runnable {
        @Override
        public void run() {
            String redisKey = RedisKeyUtil.getPostScoreKey();
            BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);

            if (operations.size() == 0) {
                logger.debug("[任务取消] 没有需要刷新的帖子!");
                return;
            }

            logger.debug("[任务开始] 正在刷新帖子分数: " + operations.size());
            while (operations.size() > 0) {
                refresh((Integer) operations.pop());
            }
            logger.debug("[任务结束] 帖子分数刷新完毕!");
        }
    }

    private void refresh(int postId) {
        DiscussPost post = discussPostService.findDiscussPostById(postId);

        if (post == null) {
            logger.error("该帖子不存在: id = " + postId);
            return;
        }

        // 是否精华
        boolean wonderful = post.getStatus() == 1;
        // 评论数量
        int commentCount = post.getCommentCount();
        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);

        // 计算权重
        double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
        // 分数 = 帖子权重 + 距离天数
        double score = Math.log10(Math.max(w, 1))
                + (post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);
        // 更新帖子分数
        discussPostService.updateScore(postId, score);
        // 同步搜索数据
        post.setScore(score);
        elasticsearchService.saveDiscussPost(post);

        redisTemplate.opsForZSet().add(RedisKeyUtil.getHotPostsList(),postId,score);
        }

}
