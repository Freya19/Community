package site.pyyf.forum.controller;

import site.pyyf.commons.utils.TagsVO;
import site.pyyf.forum.entity.*;
import site.pyyf.commons.utils.CommunityConstant;
import site.pyyf.commons.utils.CommunityUtil;
import site.pyyf.commons.utils.RedisKeyUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController extends CommunityBaseController implements CommunityConstant {

    @RequestMapping(method = RequestMethod.GET)
    public String getInputPage(Model model){
        //获取发布页下面的所有标签
        model.addAttribute("tags", TagsVO.get());
        return "forum/publish";
    }


    /**
     * 编辑帖子
     * @param model model
     * @param discussPostId 帖子id
     * @return 发布页面
     */
    @RequestMapping(path="/edit/{discussPostId}",method = RequestMethod.GET)
    public String getEditPage(Model model,@PathVariable("discussPostId") int discussPostId){
        DiscussPost discussPost = iDiscussPostService.queryById(discussPostId);
        model.addAttribute("title", discussPost.getTitle());
        model.addAttribute("content", discussPost.getContent());
        model.addAttribute("tag", discussPost.getTags());
        model.addAttribute("id", discussPost.getId());
        model.addAttribute("tags", TagsVO.get());
        return "forum/publish";
    }


    /**
     * 新增帖子
     * @param discussPost 帖子
     * @return 首页
     */
    @RequestMapping(method = RequestMethod.POST)
    public String addDiscussPost(DiscussPost discussPost) {
        User user = hostHolder.getUser();
        // 1. tag验证
        if ( discussPost.getTags()==null || discussPost.getTags().equals("")) {
            discussPost.setTags("-1");
        }

        // 2. 组建如果DiscussPost。
        //    传来的discussPost有id，那表明是修改完毕后发送，所以是更新，因为getEditPage返回编辑窗口时，是将id也传给前端;
        //    而单纯的发布帖子是没有id的
        if(discussPost.getId()!=null){
            // ---  更新帖子（编辑）----
            final DiscussPost update = DiscussPost.builder()
                    .id(discussPost.getId())
                    .tags(discussPost.getTags())
                    .createTime(new Date())
                    .title(discussPost.getTitle())
                    .content(discussPost.getContent())
                    .build();

            //3. 修改数据库
            iDiscussPostService.update(update);
            //4. 更新ES （ES-非关系型数据库）
//            elasticsearchService.saveDiscussPost(update);
            Event eventES = new Event()
                    .setTopic(TOPIC_UPDATE_ES)
                    .setEntityId(discussPost.getId());
            eventProducer.fireEvent(eventES);

        }else{
            //  -----  新增帖子  -------
            // 3. 插入数据库
            discussPost.setUserId(user.getId()).setCreateTime(new Date());
            iDiscussPostService.insert(discussPost);

            // 4. 触发更新ES事件
            Event eventES = new Event()
                    .setTopic(TOPIC_UPDATE_ES)
                    .setEntityId(discussPost.getId());
            eventProducer.fireEvent(eventES);

            // 5. 触发发帖事件  ------ 给粉丝推送发帖动态
            Event eventPublish = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(user.getId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(discussPost.getId());
            eventProducer.fireEvent(eventPublish);
        }

        // 5. 计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, discussPost.getId());

        return "redirect:/index";
    }

    /**
     * 查看帖子 ---- 主要涉及到看帖后 个性化推荐
     */
    @RequestMapping(path = "/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {

        // 1. 获取指定帖子并为java代码添加编译模块
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

        // 2. 获取帖子的评论列表。评论: 给帖子的评论 ; 回复: 给评论的评论
        /**
         * 获取评论列表总过程：
         * 1. 根据帖子找到所有的直接评论（通过帖子和帖子id字段）
         * 2. 根据每个评论找到评论的回复（通过评论和评论id字段）
         *   2.1 找到评论的回复时将目标target找到，前端可以显示 Gepeng18 回复 Freya
         *   2.2 有的是直接评论评论，所以targetId=0,这时前端不显示；
         *   2.3 如果是回复评论的，则targetId=对应的userId,这时需要将user取出
         */
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
                        // 回复目标,如果不是回复任何人，则targetId=0
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

        // 3. 热门问题
        List<DiscussPost> hotPosts = iDiscussPostService
                .queryAllByLimit(DiscussPost.builder().userId(-1).tags("-1").build(),3, 0, 5);
        model.addAttribute("hotPosts", hotPosts);

        //4. 相关问题
        int relatedPostsCount  = 7;
        List<DiscussPost> preRelatedPosts = iDiscussPostService.queryAllByLimit(DiscussPost.builder().userId(-1).tags(post.getTags()).build(), 1, 0, relatedPostsCount);
        //相关问题肯定不能包括自己，所以搜出relatedPostsCount个，如果自己在就去掉自己，否则去掉最后一个
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
            // 5. 触发看帖事件 ----
            String[] tags = post.getTags().split(",|，");
            if(tags.length>0)
                event.setData("viewTags",tags);
            eventProducer.fireEvent(event);
        }

        return "forum/discuss-detail";
    }

    /**
     * 置顶，其实就是修改数据库的某一位
     * @param id 帖子id
     */
    @RequestMapping(path = "/top", method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int id) {
        iDiscussPostService.updateType(id, 1);

        // 触发更新ES事件，重新覆盖ES，因为es搜索出所有帖子后会按照类型排序
        Event event = new Event()
                .setTopic(TOPIC_UPDATE_ES)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    /**
     * 加精
     * @param id  帖子id
     */
    @RequestMapping(path = "/wonderful", method = RequestMethod.POST)
    @ResponseBody
    public String setWonderful(int id) {
        iDiscussPostService.updateStatus(id, 1);

        // 触发发帖事件
        Event event = new Event()
                .setTopic(TOPIC_UPDATE_ES)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        // 计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, id);

        return CommunityUtil.getJSONString(0);
    }

    /**
     *  删除
     * @param id
     * @return
     */
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
