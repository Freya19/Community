package site.pyyf.blog.controller;

import site.pyyf.community.config.AliyunConfig;
import site.pyyf.community.entity.Comment;
import site.pyyf.community.entity.User;
import site.pyyf.commons.utils.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Controller
public class BlogCommentController extends  BaseController implements CommunityConstant {

    @Autowired
    private AliyunConfig aliyunConfig;

    //获取评论并显示
    @GetMapping("/blog/comments/{blogId}")
    public String comments(@PathVariable Integer blogId, Model model) {

        // 评论: 给帖子的评论
        // 回复: 给评论的评论
        // 评论列表
        final Comment QueryBlogComment= Comment.builder().entityType(ENTITY_TYPE_BLOG).entityId(blogId).build();
        List<Comment> commentList = iCommentService.queryAll(QueryBlogComment);
        // 评论VO列表
        List<Map<String, Object>> commentVOS = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                // 评论VO
                Map<String, Object> commentVO = new HashMap<>();
                // 评论
                commentVO.put("comment", comment);
                // 作者
                commentVO.put("user", iUserService.queryById(comment.getUserId()));

                // 回复列表
                List<Comment> replyList = iCommentService.queryAll(
                        Comment.builder().entityType(ENTITY_TYPE_BLOG_COMMENT).entityId(comment.getId()).build());
                // 回复VO列表
                List<Map<String, Object>> replyVOS = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVO = new HashMap<>();
                        // 回复
                        replyVO.put("reply", reply);
                        // 作者
                        replyVO.put("user", iUserService.queryById(reply.getUserId()));
                        // 回复目标
                        User target = iUserService.queryById(iCommentService.queryById(reply.getTargetId()).getUserId());
                        replyVO.put("target", target);
                        replyVOS.add(replyVO);
                    }
                }
                commentVO.put("replyVOS", replyVOS);

                commentVOS.add(commentVO);
            }
        }
        model.addAttribute("blog",iBlogService.queryById(blogId));
        model.addAttribute("commentVOS", commentVOS);
        return "blog/blog :: commentList";
    }

    @PostMapping("/blog/comments/{blogId}")
    public String post(@PathVariable Integer blogId, @RequestParam("targetId") Integer targetId, Comment comment, User user) {
        //评论博客
        if(targetId==-1){
            comment.setEntityId(blogId);
            comment.setEntityType(ENTITY_TYPE_BLOG);
            User blogUser = iUserService.queryById(iBlogService.queryById(blogId).getUserId());
            iCommentService.notifyUser(EMAIL_TYPE_BLOG_USER,blogUser.getUsername(),blogUser.getEmail(),
                    String.valueOf(blogId),iBlogService.queryById(blogId).getTitle(),comment.getContent());
        }
        else{
            //回复评论
            final Comment targetComment = iCommentService.queryById(targetId);

            if(targetComment.getEntityType()==ENTITY_TYPE_BLOG)
                //回复的是一级评论
                comment.setEntityId(targetComment.getId());
            else
                //回复的是二级评论
                comment.setEntityId(targetComment.getEntityId());
            //回复评论
            comment.setEntityType(ENTITY_TYPE_BLOG_COMMENT);
            comment.setTargetId(targetId);

            User commentUser = iUserService.queryById(iCommentService.queryById(targetId).getUserId());
            iCommentService.notifyUser(EMAIL_TYPE_COMMENT_USER,commentUser.getUsername(),commentUser.getEmail(),
                    String.valueOf(blogId),iBlogService.queryById(blogId).getTitle(),comment.getContent());
        }

        if (loginUser != null) {
            comment.setUserId(loginUser.getId());
        } else {
            //对博客评论时，如未登陆则创建用户后评论
            User insertUser = iUserService.insert(user);
            comment.setUserId(insertUser.getId());
        }
        iCommentService.insert(comment);

        return "redirect:/blog/comments/" + blogId;
    }
}
