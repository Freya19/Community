package site.pyyf.blog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AboutShowController {

    @GetMapping("/blog/about")
    public String about() {
        return "blog/about";
    }
}
