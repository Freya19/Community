package site.pyyf.forum.controller;

import site.pyyf.forum.entity.DiscussPost;
import site.pyyf.forum.entity.Feed;
import site.pyyf.forum.entity.Page;
import site.pyyf.forum.entity.User;
import site.pyyf.commons.utils.CommunityConstant;
import site.pyyf.commons.utils.CommunityUtil;
import site.pyyf.forum.annotation.LoginRequired;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user")
public class UserController extends CommunityBaseController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${quniu.bucket.header.url}")
    private String headerBucketUrl;

    @LoginRequired
    @RequestMapping(method = RequestMethod.GET)
    public String getSettingPage(Model model) {
        // 上传文件名称
        String fileName = CommunityUtil.generateUUID();
        // 设置响应信息
        StringMap policy = new StringMap();
        policy.put("returnBody", CommunityUtil.getJSONString(0));
        // 生成上传凭证
        Auth auth = Auth.create(accessKey, secretKey);
        String uploadToken = auth.uploadToken(headerBucketName, fileName, 3600, policy);

        model.addAttribute("uploadToken", uploadToken);
        model.addAttribute("fileName", fileName);

        return "forum/setting";
    }

    // 更新头像路径
    @RequestMapping(path = "/header/url", method = RequestMethod.POST)
    @ResponseBody
    public String updateHeaderUrl(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return CommunityUtil.getJSONString(1, "文件名不能为空!");
        }

        String url = headerBucketUrl + "/" + fileName;
        iUserService.updateHeader(hostHolder.getUser().getId(), url);

        return CommunityUtil.getJSONString(0);
    }

    // 废弃
    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "您还没有选择图片!");
            return "forum/setting";
        }

        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件的格式不正确!");
            return "forum/setting";
        }

        // 生成随机文件名
        fileName = CommunityUtil.generateUUID() + suffix;
        // 确定文件存放的路径
        File dest = new File(uploadPath + "/" + fileName);
        try {
            // 存储文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败: " + e.getMessage());
            throw new RuntimeException("上传文件失败,服务器发生异常!", e);
        }

        // 更新当前用户的头像的路径(web访问路径)
        // http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        iUserService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }

    // 废弃
    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 服务器存放路径
        fileName = uploadPath + "/" + fileName;
        // 文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 响应图片
        response.setContentType("image/" + suffix);
        try (
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败: " + e.getMessage());
        }
    }

    // 个人主页
    @RequestMapping(path = "/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = iUserService.queryById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }

        // 用户
        model.addAttribute("user", user);
        // 点赞数量
        int likeCount = iLikeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        // 关注数量
        Long followeeCount = iFollowService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量
        Long followerCount = iFollowService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // 是否已关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = iFollowService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);

        return "forum/profile";
    }


    // 个人主页
    @RequestMapping(path = "/forget", method = RequestMethod.GET)
    public String getForgetPage() {
        return "forum/forget";
    }


    /**
     * 个性化定制 之 我的帖子
     * @param userId 用户id
     * @param model 后端往前端传参的媒介
     * @param page  分页
     * @return 页面
     */
    @RequestMapping(path ={"/posts/{userId}"},method = {RequestMethod.GET,RequestMethod.POST})
    public String getUserPosts(@PathVariable(value = "userId") int userId, Model model, Page page){
        DiscussPost query = DiscussPost.builder().userId(userId).build();
        page.setRows(iDiscussPostService.queryCount(query));
        page.setPath("/user/posts/"+userId);

        model.addAttribute("user",iUserService.queryById(userId));

        List<Feed> feeds = null;
        Set<Feed> published = null;
        Set<Feed> liked = null;
        Set<Feed> commented = null;
        if(hostHolder.getUser()!=null){
            feeds = iFeedService.getFeeds();
            published = feeds.stream().filter(feed -> {
                return feed.getFeedType() == FEED_PUBLISH;
            }).collect(Collectors.toSet());
            liked = feeds.stream().filter(feed -> {
                return feed.getFeedType() == FEED_LIKE;
            }).collect(Collectors.toSet());
            commented = feeds.stream().filter(feed -> {
                return feed.getFeedType() == FEED_COMMENT;
            }).collect(Collectors.toSet());
        }

        //ordermode 0是按照时间排序 1是按照热度排序
        List<DiscussPost> posts = iDiscussPostService.queryAllByLimit(1,page.getOffset(),page.getLimit());

        List<Map<String, Object>> discussPostVOS = new ArrayList<>();
        if (posts.size()!= 0) {
            for (DiscussPost post : posts) {
                Map<String, Object> discussPostVO = new HashMap<>();
                if(hostHolder.getUser()!=null) {
                    String feedContent = iFeedService.getFeedContentByPostId(post.getId(), published, liked, commented);
                    if (feedContent.length() > 0) {
                        discussPostVO.put("feedContent", feedContent);
                    } else {
                        //简单粗暴，这样前端就好写了
                        discussPostVO.put("feedContent", "");
                    }
                }
                discussPostVO.put("post", post);
                User user = iUserService.queryById(post.getUserId());
                discussPostVO.put("user", user);

                Long likeCount = iLikeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                discussPostVO.put("likeCount", (long) likeCount);

                discussPostVOS.add(discussPostVO);
            }
        }
        model.addAttribute("discussPosts", discussPostVOS);

        return "forum/posts";

    }

}
