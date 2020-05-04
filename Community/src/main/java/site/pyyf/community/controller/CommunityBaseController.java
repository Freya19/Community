package site.pyyf.community.controller;

import com.google.code.kaptcha.Producer;
import site.pyyf.commons.event.EventProducer;
import site.pyyf.commons.utils.HostHolder;
import site.pyyf.commons.utils.HotTagCache;
import site.pyyf.community.entity.User;
import site.pyyf.community.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import site.pyyf.community.service.*;
import site.pyyf.community.service.impl.ElasticsearchService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CommunityBaseController {

    @Autowired
    protected IDiscussPostService iDiscussPostService;

    @Autowired
    protected IUserService iUserService;

    @Autowired
    protected ILikeService iLikeService;

    @Autowired
    protected ElasticsearchService elasticsearchService;

    @Autowired
    protected ICategoryService iCategoryService;

    @Autowired
    protected ICommentService iCommentService;

    @Autowired
    protected HostHolder hostHolder;

    @Autowired
    protected EventProducer eventProducer;

    @Autowired
    protected RedisTemplate redisTemplate;

    @Autowired
    protected IDataSevice iDataSevice;

    @Autowired
    protected IFollowService iFollowService;

    @Autowired
    protected ISiteSettingService iSiteSettingService;

    @Autowired
    protected Producer kaptchaProducer;

    @Autowired
    protected IMessageService iMessageService;

    @Autowired
    protected IAliyunOssService iAliyunOssService;

    @Autowired
    protected IElasticsearchService iElasticsearchService;

    @Autowired
    protected ICodePreviewService iCodePreviewService;

    @Autowired
    protected HotTagCache hotTagCache;

    @Autowired
    IFeedService iFeedService;

    /**
     * 在每个子类方法调用之前先调用
     * hasLogin：1-登录，0-未登录
     * 获取用户是否登录的状态以及用户类型是否是管理员
     * isAdmin：0-非管理员; 1-超级管理员;
     *
     * @param request
     * @param response
     */
    @ModelAttribute
    public void setReqAndRes(Model model, HttpServletRequest request, HttpServletResponse response) {
        User loginUser = hostHolder.getUser();
        if (loginUser != null) {
            model.addAttribute("hasLogin", "1");
        } else {
            model.addAttribute("hasLogin", "0");
        }

        //获取前端当前登录用户
        if(hostHolder.getUser()==null) {
            model.addAttribute("loginUserId","-1");
        } else {
            model.addAttribute("loginUserId",hostHolder.getUser().getId());
        }

        //用户类型为1和2 即具有管理员权限
        model.addAttribute("isAdmin",
                hostHolder.getUser() != null && (hostHolder.getUser().getUserType() == 1 || hostHolder.getUser().getUserType() == 2) ? 1 : 0);
    }
}
