package site.pyyf.blog.controller;

import site.pyyf.blog.service.IBlogService;
import site.pyyf.blog.service.IBlog_TagService;
import site.pyyf.forum.entity.User;
import site.pyyf.commons.utils.HostHolder;
import site.pyyf.forum.service.*;
import site.pyyf.blog.service.ITagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import site.pyyf.forum.service.*;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @ClassName: BaseController
 * @Description: 控制器的基类，所有控制器必须继承此类
 * @author: xw
 * @date 2020/2/25 18:19
 * @Version: 1.0
 **/
public class BaseController {

    @Autowired
    protected IDiscussPostService iDiscussPostService;

    @Autowired
    protected ICommentService iCommentService;

    @Autowired
    protected IBlog_TagService iBlog_tagService;

    @Autowired
    protected IBlogService iBlogService;

    @Autowired
    protected IUserService iUserService;

    @Autowired
    protected ITagService iTagService;

    @Autowired
    protected ICategoryService iCategoryService;

    @Autowired
    protected IAliyunOssService iAliyunOssService;

    @Autowired
    protected ICodePreviewService iCodePreviewService;

    @Autowired
    protected HostHolder hostHolder;

    @Autowired
    protected IFeedService iFeedService;

    /**
     * 在每个子类方法调用之前先调用
     * 设置request,response,session这三个对象
     *
     * @param request
     * @param response
     */
    @ModelAttribute
    public void setReqAndRes(Model model,HttpServletRequest request, HttpServletResponse response) {
        if(hostHolder.getUser()!=null)
            model.addAttribute("hasLogin","1");
        else
            model.addAttribute("hasLogin","0");
    }
}
