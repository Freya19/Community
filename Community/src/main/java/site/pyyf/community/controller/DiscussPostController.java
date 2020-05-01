package site.pyyf.community.controller;

import site.pyyf.commons.utils.TagCache;
import site.pyyf.community.entity.*;
import site.pyyf.commons.utils.CommunityConstant;
import site.pyyf.commons.utils.CommunityUtil;
import site.pyyf.commons.utils.RedisKeyUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import site.pyyf.community.entity.*;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController extends BaseController implements CommunityConstant {

    @RequestMapping(method = RequestMethod.GET)
    public String getInputPage(Model model){
        model.addAttribute("tags", TagCache.get());
        return "site/publish";
    }

    @RequestMapping(path="/edit/{discussPostId}",method = RequestMethod.GET)
    public String getEditPage(Model model,@PathVariable("discussPostId") int discussPostId){
        DiscussPost discussPost = iDiscussPostService.queryById(discussPostId);
        model.addAttribute("title", discussPost.getTitle());
        model.addAttribute("content", discussPost.getContent());
        model.addAttribute("tag", discussPost.getTags());
        model.addAttribute("id", discussPost.getId());
        model.addAttribute("tags", TagCache.get());
        return "site/publish";
    }


    @RequestMapping(method = RequestMethod.POST)
    public String addDiscussPost(DiscussPost discussPost) {
        User user = hostHolder.getUser();
        if ( discussPost.getTags()==null || discussPost.getTags().equals("")) {
            discussPost.setTags("-1");
        }

        if(discussPost.getId()!=null){
            final DiscussPost update = DiscussPost.builder()
                    .id(discussPost.getId())
                    .tags(discussPost.getTags())
                    .createTime(new Date())
                    .title(discussPost.getTitle())
                    .content(discussPost.getContent())
                    .build();

            //修改数据库以及elasticsearch
            iDiscussPostService.update(update);
            elasticsearchService.saveDiscussPost(update);

        }else{
            discussPost.setUserId(user.getId()).setCreateTime(new Date());
            iDiscussPostService.insert(discussPost);

            // 触发发帖事件
            Event event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(user.getId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(discussPost.getId());
            eventProducer.fireEvent(event);
        }

        // 计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, discussPost.getId());

        // 报错的情况,将来统一处理.
        return "redirect:/index";
    }

    @RequestMapping(path = "/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {

        // 帖子
        DiscussPost post = iDiscussPostService.queryById(discussPostId);
        post.setContent(iCodePreviewService.addCompileModule(new StringBuilder(post.getContent()),"java",1).toString());

        model.addAttribute("post", post);
        // 作者
        User user = iUserService.queryById(post.getUserId());
        model.addAttribute("user", user);
        // 点赞数量
        Long likeCount = iLikeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount", likeCount);
        // 点赞状态
        int likeStatus = hostHolder.getUser() == null ? 0 :
                iLikeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStatus", likeStatus);

        // 评论分页信息
        page.setLimit(5);
        page.setPath("/discuss/" + discussPostId);
        page.setRows(post.getCommentCount());

        // 评论: 给帖子的评论
        // 回复: 给评论的评论
        // 评论列表
        List<Comment> commentList = iCommentService.queryAllByLimit(
                Comment.builder().entityType(ENTITY_TYPE_POST).entityId(post.getId()).build()
                , page.getOffset(), page.getLimit());
        // 评论VO列表
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                // 评论VO
                Map<String, Object> commentVo = new HashMap<>();
                // 评论
                commentVo.put("comment", comment);
                // 作者
                commentVo.put("user", iUserService.queryById(comment.getUserId()));
                // 点赞数量
                likeCount = iLikeService.findEntityLikeCount(ENTITY_TYPE_POST_COMMENT, comment.getId());
                commentVo.put("likeCount", likeCount);
                // 点赞状态
                likeStatus = hostHolder.getUser() == null ? 0 :
                        iLikeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST_COMMENT, comment.getId());
                commentVo.put("likeStatus", likeStatus);

                // 回复列表
                List<Comment> replyList = iCommentService.queryAllByLimit(
                        Comment.builder().entityType(ENTITY_TYPE_POST_COMMENT).entityId(comment.getId()).build(), 0, Integer.MAX_VALUE);
                // 回复VO列表
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        // 回复
                        replyVo.put("reply", reply);
                        // 作者
                        replyVo.put("user", iUserService.queryById(reply.getUserId()));
                        // 回复目标
                        User target = reply.getTargetId() == 0 ? null : iUserService.queryById(reply.getTargetId());
                        replyVo.put("target", target);
                        // 点赞数量
                        likeCount = iLikeService.findEntityLikeCount(ENTITY_TYPE_POST_COMMENT, reply.getId());
                        replyVo.put("likeCount", likeCount);
                        // 点赞状态
                        likeStatus = hostHolder.getUser() == null ? 0 :
                                iLikeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST_COMMENT, reply.getId());
                        replyVo.put("likeStatus", likeStatus);

                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys", replyVoList);

                // 回复数量
                int replyCount = iCommentService.queryCount(Comment.builder().entityType(ENTITY_TYPE_POST_COMMENT).entityId(comment.getId()).build() );
                commentVo.put("replyCount", replyCount);

                commentVoList.add(commentVo);
            }
        }
        model.addAttribute("comments", commentVoList);
        //* ------------------- 判断帖子是谁发的，前端有编辑按钮 ----------------- */
        if(hostHolder.getUser()==null)
            model.addAttribute("loginUserId","-1");
        else
            model.addAttribute("loginUserId",hostHolder.getUser().getId());
        /* ------------------- 热门问题 ----------------- */
        List<DiscussPost> hotPosts = iDiscussPostService
                .queryAllByLimit(DiscussPost.builder().userId(-1).tags("-1").build(),3, 0, 5);
        model.addAttribute("hotPosts", hotPosts);
        /* ------------------- 相关问题 ----------------- */
        int relatedPostsCount  = 7;
        List<DiscussPost> preRelatedPosts = iDiscussPostService.queryAllByLimit(DiscussPost.builder().userId(-1).tags(post.getTags()).build(), 1, 0, relatedPostsCount);
        /* ------------------- 相关问题肯定不能包括自己，所以搜出5个，如果自己在就去掉自己，否则去掉最后一个 ----------------- */
        List<DiscussPost> relatedPosts = new ArrayList<>();
        for(DiscussPost  discussPost: preRelatedPosts)
            if(discussPost.getId() != discussPostId)
                relatedPosts.add(discussPost);
        if(relatedPosts.size()==relatedPostsCount)
            relatedPosts.remove(relatedPosts.size()-1);
        model.addAttribute("relatedPosts",relatedPosts);

        if(hostHolder.getUser()!=null){
            Event event = new Event()
                    .setTopic(TOPIC_VIEW)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(discussPostId);
            // 触发看帖事件
            String[] tags = post.getTags().split(",|，");
            if(tags.length>0)
                event.setData("viewTags",tags);
            eventProducer.fireEvent(event);
        }

        return "site/discuss-detail";
    }

    // 置顶
    @RequestMapping(path = "/top", method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int id) {
        iDiscussPostService.updateType(id, 1);

        // 触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    // 加精
    @RequestMapping(path = "/wonderful", method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id) {
        iDiscussPostService.updateStatus(id, 1);

        // 触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        // 计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, id);

        return CommunityUtil.getJSONString(0);
    }

    // 删除
    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String setDelete(int id) {
        iDiscussPostService.updateStatus(id, 2);
        // 触发删帖事件
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

}
