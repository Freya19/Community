package site.pyyf.blog.controller;

import site.pyyf.blog.entity.Blog;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ArchiveShowController extends  BaseController{

    @GetMapping("/blog/archives")
    public String archives(Model model) {

        List<String> months = iBlogService.queryGroupByMonth();
        List<Map<String,Object>> blogsVOS = new ArrayList<>();
        for(String month:months){
            Map<String, Object> blogsVO = new HashMap<>();
            blogsVO.put("month",month.substring(0,4)+"年"+month.substring(4)+"月");
            List<Blog> blogsInAMonth = iBlogService.queryByMonth(month);
            blogsVO.put("blogs",blogsInAMonth);
            blogsVOS.add(blogsVO);
        }
        model.addAttribute("blogsVOS", blogsVOS);
        model.addAttribute("blogCount", iBlogService.queryCount());
        return "blog/archives";
    }
}
