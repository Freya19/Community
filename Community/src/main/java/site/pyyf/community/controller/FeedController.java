package site.pyyf.community.controller;

import site.pyyf.commons.utils.CommunityConstant;
import site.pyyf.commons.utils.RedisKeyUtil;
import site.pyyf.community.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by "freya" on 2020-04-111 12:06:57.
 * @Description 活跃用户将其关注的人的动态“推”给用户
 * @param
 * @return
 */

@Controller
public class FeedController extends CommunityBaseController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(FeedController.class);

    @RequestMapping(path = {"/personalized"}, method = {RequestMethod.GET, RequestMethod.POST})
    private String getPushFeeds(Model model, Page page) {

        //判断用户多久没登录了，按照5天计算，5天内则是推的，5天前登录则让它拉
        if (hostHolder.getUser() == null)
            //这里让前端控制，未登录则禁止访问
        {
            return "redirect:/index";
        }

        //每访问第一页，就将redis缓存中的数据进行替换
        if (page.getCurrent() == 1) {
            String persistenceKey = RedisKeyUtil.getPersistenceTimelineKey(hostHolder.getUser().getId());
            String latestKey = RedisKeyUtil.getLatestTimelineKey(hostHolder.getUser().getId());
            //当redisTemplate.opsForList().range(latestKey, 0, -1)为空时，leftPushAll会报错
            redisTemplate.delete(persistenceKey);
            if (redisTemplate.opsForList().range(latestKey, 0, -1).size() > 0)
                redisTemplate.opsForList().leftPushAll(persistenceKey, redisTemplate.opsForList().range(latestKey, 0, -1));

            persistenceKey = latestKey = null;

            persistenceKey = RedisKeyUtil.getPersistenceViewTagsKey(hostHolder.getUser().getId());
            latestKey = RedisKeyUtil.getLatestViewTagsKey(hostHolder.getUser().getId());
            redisTemplate.delete(persistenceKey);
            if (redisTemplate.opsForList().range(latestKey, 0, -1).size() > 0)
                redisTemplate.opsForList().leftPushAll(persistenceKey, redisTemplate.opsForList().range(latestKey, 0, -1));

        }

        page.setRows(iDiscussPostService.queryCount());
        page.setPath("/personalized");

        List<DiscussPost> discussPosts = null;

        String tagKey = RedisKeyUtil.getPersistenceViewTagsKey(hostHolder.getUser().getId());
        List<String> tags = redisTemplate.opsForList().range(tagKey, 0, -1);

        List<Feed> feeds = iFeedService.getFeeds();

        final Set<Feed> published = feeds.stream().filter(feed -> {
            return feed.getFeedType() == FEED_PUBLISH;
        }).collect(Collectors.toSet());
        final Set<Feed> liked = feeds.stream().filter(feed -> {
            return feed.getFeedType() == FEED_LIKE;
        }).collect(Collectors.toSet());
        final Set<Feed> commented = feeds.stream().filter(feed -> {
            return feed.getFeedType() == FEED_COMMENT;
        }).collect(Collectors.toSet());

        discussPosts = iDiscussPostService
                .queryAllByLimitByPersonalized(
                        published.stream().map(feed -> {
                            return (Integer) feed.getEntityId();
                        }).collect(Collectors.toList()),
                        liked.stream().map(feed -> {
                            return (Integer) feed.getEntityId();
                        }).collect(Collectors.toList()),
                        commented.stream().map(feed -> {
                            return (Integer) feed.getEntityId();
                        }).collect(Collectors.toList()),
                        tags, 5, page.getOffset(), page.getLimit());

        List<Map<String, Object>> discussPostVOS = new ArrayList<>();
        if (discussPosts != null) {
            for (DiscussPost post : discussPosts) {

                Map<String, Object> discussPostVO = new HashMap<>();
                String feedContent = iFeedService.getFeedContentByPostId(post.getId(), published, liked, commented);
                if (feedContent.length() > 0) {
                    discussPostVO.put("feedContent", feedContent);
                } else
                    //简单粗暴，这样前端就好写了
                {
                    discussPostVO.put("feedContent", "");
                }
                discussPostVO.put("post", post);
                User user = iUserService.queryById(post.getUserId());
                discussPostVO.put("user", user);

                Long likeCount = iLikeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                discussPostVO.put("likeCount", (long) likeCount);

                discussPostVOS.add(discussPostVO);
            }
        }
        model.addAttribute("discussPosts", discussPostVOS);
        model.addAttribute("orderMode", 2);
        List<Tag> hotTags = tagCache.getShowTags();
        model.addAttribute("hotTags", hotTags);

        /* ------------------- 热门问题 ----------------- */
        List<DiscussPost> hotPosts = iDiscussPostService
                .queryAllByLimit(DiscussPost.builder().userId(-1).tags("-1").build(), ORDER_TOPANDSCORE, 0, 5);
        model.addAttribute("hotPosts", hotPosts);

        return "index";

    }

    /**
     * 个性化定制之 我的动态
     * @param userId 我关注的某个用户的id
     * @param model  前端需要的model
     * @param page  分页的page
     * @return   templates/site/feed.html页面
     */
    @RequestMapping(path = {"/user/feeds/{userId}"}, method = {RequestMethod.GET, RequestMethod.POST})
    private String getUserFeed(@PathVariable(value = "userId") int userId,Model model,Page page){
        page.setRows(iFeedService.queryCount(Feed.builder().entityType(ENTITY_TYPE_POST).userId(userId).build()));
        page.setPath("/user/feeds/"+userId);

        model.addAttribute("user",iUserService.queryById(userId));


        List<Feed> feeds = iFeedService.queryAllByLimit(Feed.builder().entityType(ENTITY_TYPE_POST).userId(userId).build(), page.getOffset(), page.getLimit());
        ArrayList<Map<String,Object>> feedAndPostTileVOS = new ArrayList<>();

        for(Feed feed:feeds){
            Map<String,Object> feedAndPostTileVO = new HashMap();
            feedAndPostTileVO.put("feed",feed);
            DiscussPost discussPost = iDiscussPostService.queryById(feed.getEntityId());
            feedAndPostTileVO.put("title", discussPost.getTitle());
            feedAndPostTileVO.put("id", discussPost.getId());
            feedAndPostTileVOS.add(feedAndPostTileVO);
        }
        model.addAttribute("feeds", feedAndPostTileVOS);

        return "site/feeds";
    }
}
