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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class IndexController extends BaseController implements CommunityConstant {


    @GetMapping("/blogs")
    public String index(Model model, Page page) {

        // 评论分页信息
        page.setLimit(8);
        page.setPath("/blogs");
        page.setRows(iBlogService.queryCount());

        List<Blog> blogs = iBlogService.queryAllByLimit(page.getOffset(), page.getLimit());
//        List<Blog> blogs = allBlog.stream().sorted(
//                (blog1, blog2) -> {
//                    int num1 = blog1.getRecommend() ? 1 : 0;
//                    int num2 = blog2.getRecommend() ? 1 : 0;
//                    return num1 - num2;
//                }).sorted((blog1, blog2) -> {
//            long num1 = System.currentTimeMillis() - blog1.getCreateTime().getTime(); //时间小的
//            long num2 = System.currentTimeMillis() - blog2.getCreateTime().getTime(); //时间大的
//            return (int) (num2 - num1);
//        }).collect(Collectors.toList());

        List<Map<String, Object>> blogUserAndCats = new ArrayList<>();
        for (Blog blog : blogs) {
            User query = User.builder().id(blog.getUserId()).build();
            List<User> users = iUserService.queryAll(query);
            HashMap<String, Object> blogUserAndCat = new HashMap<>();
            blogUserAndCat.put("blog", blog);
            blogUserAndCat.put("user", users.get(0));
            blogUserAndCat.put("cat", iCategoryService.queryById(blog.getCategoryId()));
            blogUserAndCats.add(blogUserAndCat);
        }

        Blog queryCondition = Blog.builder().recommend(true).build();
        List<Blog> recommendBlogs = iBlogService.queryAllByLimit(queryCondition, page.getOffset(), page.getLimit());

        List<Category> cats = iCategoryService.queryAllByLimit(Category.builder().entityType(ENTITY_TYPE_BLOG).build(), 0, 10);

        List<Tag> tags = iTagService.queryAllByLimit(Tag.builder().entityType(ENTITY_TYPE_BLOG).build(),0, 6);

        model.addAttribute("bucVO", blogUserAndCats);

        model.addAttribute("cats", cats.stream().sorted((cat1,cat2)->{return cat2.getCount()-cat1.getCount();}).collect(Collectors.toList()));
        model.addAttribute("tags", tags.stream().sorted((tag1,tag2)->{return tag2.getCount()-tag1.getCount();}).collect(Collectors.toList()));
        model.addAttribute("recommendBlogs", recommendBlogs);
        return "blog/index";
    }

    @PostMapping("/blog/search")
    public String search(Page page, @RequestParam String query, Model model) {
        Blog queryBlog = Blog.builder().content("%" + query + "%").build();
        model.addAttribute("page", iBlogService.queryAllByLimit(queryBlog, page.getOffset(), page.getLimit()));
        model.addAttribute("query", query);
        return "blog/search";
    }

    @GetMapping("/blog/{id}")
    public String blog(@PathVariable Integer id, Model model) {
        Blog blog = iBlogService.queryById(id);
        //为java代码添加在线编译模块

        //这里如果setcontent会使affeine去修改缓存，导致重复刷新页面时重复添加，所以这里单独造个数据
        model.addAttribute("newContent",iCodePreviewService.addCompileModule(new StringBuilder(blog.getContent()), "java", 1).toString());

        model.addAttribute("blog", blog);
        model.addAttribute("user", iUserService.queryById(blog.getUserId()));
        final List<Integer> tagIds = iBlog_tagService.queryTagIdByBlogId(blog.getUserId());
        List<Tag> tags = new ArrayList<>();
        for (Integer tagId : tagIds) {
            tags.add(iTagService.queryById(tagId));
        }
        model.addAttribute("tags", tags);
        return "blog/blog";
    }

    @GetMapping("/footer/recBlogs")
    public String newblogs(Model model) {
        Blog queryBlog = Blog.builder().recommend(true).build();
        model.addAttribute("recBlogs", iBlogService.queryAllByLimit(queryBlog, 0, 3));
        return "forum-fragments :: recBlogsList";
    }

}
