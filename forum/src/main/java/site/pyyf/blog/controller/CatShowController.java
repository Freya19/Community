package site.pyyf.blog.controller;

import site.pyyf.blog.entity.Blog;
import site.pyyf.commons.utils.CommunityConstant;
import site.pyyf.forum.entity.Category;
import site.pyyf.forum.entity.Tag;
import site.pyyf.forum.entity.User;
import site.pyyf.forum.entity.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class CatShowController extends BaseController implements CommunityConstant {

    @GetMapping("/blog/cat/{id}")
    public String getCat(@PathVariable Integer id, Model model, Page page) {
        List<Category> cats = iCategoryService.queryAll(Category.builder().entityType(ENTITY_TYPE_BLOG).build());

        cats = cats.stream().sorted((cat1, cat2) -> {
            return cat2.getCount() - cat1.getCount();
        }).collect(Collectors.toList());
        model.addAttribute("cats", cats);

        if (id == -1) {
            id = cats.get(0).getId();
        }

        page.setLimit(8);
        page.setPath("/blogs");

        List<Blog> blogs = iBlogService.queryAllByLimit(Blog.builder().categoryId(id).build(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> buctVOS = new ArrayList<>();
        for(Blog blog:blogs){
            User query = User.builder().id(blog.getId()).build();
            List<User> users = iUserService.queryAll(query);
            HashMap<String, Object> buctVO = new HashMap<>();
            buctVO.put("blog",blog);
            buctVO.put("user",users.get(0));
            buctVO.put("cat",iCategoryService.queryById(blog.getCategoryId()));

            List<Tag> thisTags = new ArrayList<>();
            List<Integer> tagIds = iBlog_tagService.queryTagIdByBlogId(blog.getId());
            for(Integer tagId:tagIds){
                thisTags.add(iTagService.queryById(tagId));
            }
            buctVO.put("tags",thisTags);
            buctVOS.add(buctVO);
        }

        page.setRows(iBlogService.queryCount(Blog.builder().categoryId(id).build()));
        model.addAttribute("buctVOS", buctVOS);
        model.addAttribute("activeCatId", id);
        return "blog/cats";
    }
}
