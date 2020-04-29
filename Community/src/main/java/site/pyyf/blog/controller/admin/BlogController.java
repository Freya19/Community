package site.pyyf.blog.controller.admin;

import site.pyyf.blog.controller.BaseController;
import site.pyyf.blog.entity.Blog;
import site.pyyf.commons.utils.TagCache;
import site.pyyf.community.entity.Category;
import site.pyyf.community.entity.User;
import site.pyyf.commons.utils.CommunityConstant;
import site.pyyf.commons.utils.CommunityUtil;
import site.pyyf.community.entity.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/admin/blog")
public class BlogController extends BaseController implements CommunityConstant {

    private static final String INPUT = "blog/admin/publish";
    private static final String LIST = "blog/admin/blogs";
    private static final String REDIRECT_LIST = "redirect:/admin/blog/blogs";

    @GetMapping("/blogs")
    public String blogs(Model model, Page page) {
        page.setLimit(8);
        page.setPath("/admin/blog/blogs");
        page.setRows(iBlogService.queryCount());

        List<Blog> blogs = iBlogService.queryAllByLimit(page.getOffset(), page.getLimit());
        List<Map<String, Object>> bucVOS = new ArrayList<>();
        for(Blog blog:blogs){
            User user = User.builder().id(blog.getId()).build();
            HashMap<String, Object> bucVO = new HashMap<>();
            bucVO.put("blog",blog);
            bucVO.put("user",user);
            bucVO.put("cat",iCategoryService.queryById(blog.getCategoryId()));
            bucVOS.add(bucVO);
        }
        List<Category> cats = iCategoryService.queryAll();
        model.addAttribute("cats", cats);
        model.addAttribute("bucVOS", bucVOS);
        return LIST;
    }

    @PostMapping("/blogs/search")
    public String search(Page page, Model model) {
        page.setLimit(8);
        page.setPath("/admin/blog/blogs/search");
        page.setRows(iBlogService.queryCount());

        List<Blog> blogs = iBlogService.queryAllByLimit(page.getOffset(), page.getLimit());
        List<Map<String, Object>> bucVOS = new ArrayList<>();
        for(Blog blog:blogs){
            User query = User.builder().id(blog.getId()).build();
            List<User> users = iUserService.queryAll(query);
            HashMap<String, Object> bucVO = new HashMap<>();
            bucVO.put("blog",blog);
            bucVO.put("user",users.get(0));
            bucVO.put("cat",iCategoryService.queryById(blog.getCategoryId()));
            bucVOS.add(bucVO);
        }
        model.addAttribute("bucVOS", bucVOS);
        return "blog/admin/blogs :: blogList";
    }

//    废弃
//    @GetMapping("/blog")
//    public String getInputPage(Model model) {
//        setTypeAndTag(model);
//        return INPUT;
//    }

//    //废弃
//    private void setTypeAndTag(Model model) {
//        model.addAttribute("cats", iCategoryService.queryAll(Category.builder().entityType(ENTITY_TYPE_BLOG).build()));
//        model.addAttribute("tags", iTagService.queryAll(Tag.builder().entityType(ENTITY_TYPE_BLOG).build()));
//    }


    @GetMapping("/blog")
    public String getInputPage(Model model) {
        model.addAttribute("tags", TagCache.get());
        model.addAttribute("cats", iCategoryService.queryAll(Category.builder().entityType(ENTITY_TYPE_BLOG).build()));
        return INPUT;
    }

    @GetMapping("/blog/{id}")
    public String getEditPage(@PathVariable Integer id, Model model) {
        Blog blog = iBlogService.queryById(id);
        model.addAttribute("id",blog.getId());
        model.addAttribute("title",blog.getTitle());
        model.addAttribute("content",blog.getContent());
        model.addAttribute("tag",blog.getTags());
        model.addAttribute("tags", TagCache.get());
        model.addAttribute("cat",iCategoryService.queryById(blog.getCategoryId()));
        model.addAttribute("cats", iCategoryService.queryAll(Category.builder().entityType(ENTITY_TYPE_BLOG).build()));
        model.addAttribute("flag",blog.getFlag());
        return INPUT;
    }

    @PostMapping("/blog")
    public String addBlog(Blog blog, RedirectAttributes attributes) {
        if (loginUser == null) {
            return CommunityUtil.getJSONString(403, "你还没有登录哦!");
        }
            if(blog.getFlag()==null||blog.getFlag().equals("")){
            blog.setFlag("原创");
        }

        String description = CommunityUtil.extractChinese(blog.getContent());
        if(description.length()>100)
            description = description.substring(0,100);
        blog.setUserId(loginUser.getId()).setDescription(description);

        Blog b;
        if (blog.getId() == null) {
            b =  iBlogService.insert(blog);
        } else {
            b = iBlogService.update(blog);
        }

        if (b == null ) {
            attributes.addFlashAttribute("message", "操作失败");
        } else {
            attributes.addFlashAttribute("message", "操作成功");
        }

        return "redirect:/blogs";
    }

    @GetMapping("/blog/delete/{id}")
    public String delete(@PathVariable Integer id,RedirectAttributes attributes) {
        iBlogService.deleteById(id);
        attributes.addFlashAttribute("message", "删除成功");
        return REDIRECT_LIST;
    }


}