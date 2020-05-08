package site.pyyf.blog.controller;


import site.pyyf.blog.entity.Blog;
import site.pyyf.commons.utils.CommunityConstant;
import site.pyyf.forum.entity.Tag;
import site.pyyf.forum.entity.User;
import site.pyyf.forum.entity.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller

public class TagShowController extends BaseController implements CommunityConstant {

    @GetMapping("/blog/tag")
    public String getTag(@RequestParam(value = "tagName",defaultValue = "-1") String tagName, Page page, Model model) {
        List<Tag> tags = iTagService.queryAllByLimit(Tag.builder().entityType(ENTITY_TYPE_BLOG).build(),0,10000);

        tags = tags.stream().sorted((tag1, tag2) -> {
            return tag2.getCount() - tag1.getCount();
        }).collect(Collectors.toList());
        model.addAttribute("tags", tags);

        // 评论分页信息
        page.setLimit(8);
        page.setPath("/blogs");

        List<Blog> blogs = iBlogService.queryAllByLimit(Blog.builder().tags(tagName).build(),page.getOffset(), page.getLimit());
        List<Map<String, Object>> buctVOS = new ArrayList<>();
        for(Blog blog:blogs){
            User query = User.builder().id(blog.getUserId()).build();
            List<User> users = iUserService.queryAll(query);
            HashMap<String, Object> buctVO = new HashMap<>();
            /* ------------------- 标签名是博客的一份子，而种类名存在别的表中 ----------------- */
            buctVO.put("blog",blog);
            buctVO.put("user",users.get(0));
            buctVO.put("cat",iCategoryService.queryById(blog.getCategoryId()));
            buctVOS.add(buctVO);
        }

        model.addAttribute("buctVOS", buctVOS);
        page.setRows(iBlogService.queryCount(Blog.builder().tags(tagName).build()));
        model.addAttribute("activeTagName", tagName);
        return "blog/tags";
    }
}
