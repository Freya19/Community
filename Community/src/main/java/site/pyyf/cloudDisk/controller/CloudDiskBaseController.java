package site.pyyf.cloudDisk.controller;

import org.springframework.ui.Model;
import site.pyyf.cloudDisk.config.CloudDiskConfig;
import site.pyyf.cloudDisk.event.CloudDiskEventProducer;
import site.pyyf.cloudDisk.service.*;
import site.pyyf.commons.utils.HostHolder;
import site.pyyf.community.config.AliyunConfig;
import site.pyyf.community.entity.User;
import site.pyyf.community.service.ICodePreviewService;
import site.pyyf.community.service.IUserService;
import site.pyyf.community.service.impl.AliyunOssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.ModelAttribute;

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
public class CloudDiskBaseController {

    @Autowired
    protected AliyunConfig aliyunConfig;
    @Autowired
    protected CloudDiskConfig cloudDiskConfig;
    @Autowired
    protected IMediaTranfer iMediaTranfer;
    @Autowired
    protected ICodePreviewService iCodeService;
    @Autowired
    protected IFileFolderService iFileFolderService;
    @Autowired
    protected IMyFileService iMyFileService;
    @Autowired
    protected IEbookService iEbookService;
    @Autowired
    protected AliyunOssService aliyunOssService;
    @Autowired
    protected IEbookContentService iEbookContentService;
    @Autowired
    protected IUserService iUserService;
    @Autowired
    protected RedisTemplate redisTemplate;
    @Autowired
    protected CloudDiskEventProducer eventProducer;
    @Autowired
    protected IFileStoreService iFileStoreService;
    @Autowired
    protected HostHolder hostHolder;

    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected HttpSession session;

    /**
     * 在每个子类方法调用之前先调用
     * 设置request,response,session这三个对象
     * @param request
     * @param response
     */
    @ModelAttribute
    public void setReqAndRes(Model model, HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
        this.session = request.getSession(true);

        //用户类型为1和2 即具有管理员权限
        model.addAttribute("isAdmin",
                hostHolder.getUser() != null && (hostHolder.getUser().getUserType() == 1 || hostHolder.getUser().getUserType() == 2) ? 1 : 0);
    }
}
