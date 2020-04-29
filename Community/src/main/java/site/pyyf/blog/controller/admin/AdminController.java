package site.pyyf.blog.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/blog")
public class AdminController {

    @RequestMapping()
    public String getAdminPage(){
        return "blog/admin/admin";
    }
}
