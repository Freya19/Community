package site.pyyf.blog.controller.admin;

import site.pyyf.blog.controller.BaseController;
import site.pyyf.community.entity.Category;
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
public class CatsController extends BaseController implements CommunityConstant {

    @GetMapping("/cats")
    public String cats(Page page, Model model) {
        page.setLimit(8);
        page.setPath("/admin/blog/cats");
        page.setRows(iCategoryService.queryCount(Category.builder().entityType(ENTITY_TYPE_BLOG).build()));

        List<Category> cats = iCategoryService.queryAllByLimit(page.getOffset(), page.getLimit());

        model.addAttribute("cats",cats);
        return "blog/admin/cats";
    }

    @GetMapping("/cat")
    public String getInputPage() {
        return "blog/admin/cats-input";
    }

    @GetMapping("/cat/{id}")
    public String getEditPage(@PathVariable Integer id, Model model) {
        model.addAttribute("cat", iCategoryService.queryById(id));
        return "blog/admin/cats-input";
    }

    @PostMapping("/cat")
    public String addCat(Category category, Model model) {
        if(category.getEntityType()==null)
            category.setEntityType(ENTITY_TYPE_POST);
        if (iCategoryService.queryCount(category) >0) {
            //name与Type中的属性相同，表明某个属性校验失败，nameError为自定义，"不能添加重复的分类"为 message
            model.addAttribute("error","不能添加重复的分类");
            return "blog/admin/cats-input";
        }

        Category cat = null;
        if(category.getId()==null)
            cat= iCategoryService.insert(category);
        else
            cat= iCategoryService.update(category);
        if (cat == null ) {
            model.addAttribute("message", "操作失败");
        } else {
            model.addAttribute("message", "操作成功");
        }
        return "redirect:/admin/blog/cats";
    }

    @GetMapping("/cat/delete/{id}")
    public String deleteCat(@PathVariable Integer id,RedirectAttributes attributes) {
        iCategoryService.deleteById(id);
        attributes.addFlashAttribute("message", "删除成功");
        return "redirect:/admin/blog/cats";
    }


}
