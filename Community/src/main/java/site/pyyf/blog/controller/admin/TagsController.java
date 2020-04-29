package site.pyyf.blog.controller.admin;

import site.pyyf.blog.controller.BaseController;
import site.pyyf.community.entity.Tag;
import site.pyyf.commons.utils.CommunityConstant;
import site.pyyf.community.entity.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/blog")
public class TagsController extends BaseController implements CommunityConstant {

    @GetMapping("/tags")
    public String getTags(Page page, Model model) {

        page.setLimit(8);
        page.setPath("/blogs");
        page.setRows(iTagService.queryCount(Tag.builder().entityType(ENTITY_TYPE_BLOG).build()));

        List<Tag> tags = iTagService.queryAllByLimit(page.getOffset(), page.getLimit());

        model.addAttribute("tags",tags);
        return "blog/admin/tags";
    }

    @GetMapping("/tag")
    public String getInputPage(Model model) {
        return "blog/admin/tags-input";
    }

    @GetMapping("/tag/{id}")
    public String getEditInput(@PathVariable Integer id, Model model) {
        model.addAttribute("tag", iTagService.queryById(id));
        return "blog/admin/tags-input";
    }

    @PostMapping("/tag")
    public String addTag(Tag tag, Model model) {
        if(tag.getEntityType()==null)
            tag.setEntityType(ENTITY_TYPE_POST);
        if (iTagService.queryCount(tag) >0) {
            //name与Type中的属性相同，表明某个属性校验失败，nameError为自定义，"不能添加重复的分类"为 message
            model.addAttribute("error","不能添加重复的标签");
            return "blog/admin/tags-input";
        }

        Tag t = null;
        if(tag.getId()==null)
            t= iTagService.insert(tag);
        else
            t= iTagService.update(tag);
        if (t == null ) {
            model.addAttribute("message", "操作失败");
        } else {
            model.addAttribute("message", "操作成功");
        }
        return "redirect:/admin/blog/tags";
    }

    @GetMapping("/tag/delete/{id}")
    public String deleteTag(@PathVariable Integer id,RedirectAttributes attributes) {
        iTagService.deleteById(id);
        attributes.addFlashAttribute("message", "删除成功");
        return "redirect:/admin/blog/tags";
    }


}
