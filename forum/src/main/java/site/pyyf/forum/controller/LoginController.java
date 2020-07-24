package site.pyyf.forum.controller;

import site.pyyf.forum.entity.LoginTicket;
import site.pyyf.forum.entity.User;
import site.pyyf.commons.utils.CommunityUtil;
import site.pyyf.commons.utils.CommunityConstant;
import site.pyyf.commons.utils.RedisKeyUtil;
import site.pyyf.forum.thirdLogin.github.GithubAccessTokenDTO;
import site.pyyf.forum.thirdLogin.github.GithubProvider;
import site.pyyf.forum.thirdLogin.github.GithubUser;
import com.qq.connect.QQConnectException;
import com.qq.connect.api.OpenID;
import com.qq.connect.api.qzone.UserInfo;
import com.qq.connect.javabeans.AccessToken;
import com.qq.connect.javabeans.qzone.UserInfoBean;
import com.qq.connect.oauth.Oauth;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController extends CommunityBaseController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);


    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${github.client.id}")
    private String clientId;

    @Value("${github.client.secret}")
    private String clientSecret;

    @Value("${github.redirect.uri}")
    private String redirectUri;

    @Autowired
    private GithubProvider githubProvider;

    @ResponseBody
    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(User user) {
        Map<String, Object> resultMap = new HashMap<>();

        Map<String, Object> map = iUserService.register(user);
        if (map == null || map.isEmpty()) {
            return CommunityUtil.getJSONString(0, "注册成功");
        } else {
            resultMap.put("usernameMsg", map.get("usernameMsg"));
            resultMap.put("passwordMsg", map.get("passwordMsg"));
            resultMap.put("emailMsg", map.get("emailMsg"));
            return CommunityUtil.getJSONString(1, "注册失败", resultMap);
        }
    }

    // http://localhost:8080/community/activation/101/code
    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        int result = iUserService.activation(userId, code);
        if (result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "激活成功,您的账号已经可以正常使用了!");
            model.addAttribute("target", "/index");
        } else if (result == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "无效操作,该账号已经激活过了!");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活失败,您提供的激活码不正确!");
            model.addAttribute("target", "/index");
        }
        return "forum/info/operate-result";
    }

    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/) {
        // 生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        // 将验证码存入session
        // session.setAttribute("kaptcha", text);

        // 验证码的归属
        String kaptchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kaptchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        // 将验证码存入Redis
        String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
        redisTemplate.opsForValue().set(redisKey, text, 60, TimeUnit.SECONDS);

        // 将图片输出给浏览器
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error("响应验证码失败:" + e.getMessage());
        }
    }

    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "forum/login";
    }

    @ResponseBody
    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(String username, String password, String verifycode, boolean rememberme,
                        HttpServletResponse response, HttpServletRequest request,
                        @CookieValue("kaptchaOwner") String kaptchaOwner) {

        Map<String, Object> resultMap = new HashMap<>();

        // 检查验证码
        String kaptcha = null;
        if (StringUtils.isNotBlank(kaptchaOwner)) {
            String redisKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(redisKey);
        }

        if (StringUtils.isBlank(kaptcha) || StringUtils.isBlank(verifycode) || !kaptcha.equalsIgnoreCase(verifycode)) {
            resultMap.put("verifyCodeMsg", "验证码不正确!");
            return CommunityUtil.getJSONString(1, "登录失败", resultMap);
        }

        // 检查账号,密码
        int expiredSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = iUserService.login(username, password, expiredSeconds);
        if (map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);

            return CommunityUtil.getJSONString(0, "登录成功");
        } else {
            resultMap.put("usernameMsg", map.get("usernameMsg"));
            resultMap.put("passwordMsg", map.get("passwordMsg"));
            return CommunityUtil.getJSONString(1, "登录失败", resultMap);
        }
    }

    @GetMapping("/githubCallback")
    public String githubCallback(@RequestParam(name = "code") String code,
                                 @RequestParam(name = "state") String state,
                                 HttpServletResponse response, Model model) {
        GithubAccessTokenDTO GithubAccessTokenDTO = new GithubAccessTokenDTO();
        GithubAccessTokenDTO.setClient_id(clientId);
        GithubAccessTokenDTO.setClient_secret(clientSecret);
        GithubAccessTokenDTO.setCode(code);
        GithubAccessTokenDTO.setRedirect_uri(redirectUri);
        GithubAccessTokenDTO.setState(state);
        String accessToken = githubProvider.getAccessToken(GithubAccessTokenDTO);

        GithubUser githubUser = githubProvider.getUser(accessToken);
        if (githubUser != null && githubUser.getId() != null) {
            //获取到了用户信息，开始判断是否已经注册
            List<User> users = iUserService.queryAll(User.builder().openId(String.valueOf(githubUser.getId())).build());
            if (users.size() > 1) {
                logger.error("根据信息检索出了超过一条用户");
                model.addAttribute("reason", "根据信息检索出了超过一条用户");
                return "error/500";
            }

            /* ------------------- 有则取出，无则注册 ----------------- */
            User user = null;
            if (users.size() == 0) {
                user = User.builder().registerType(1).username(githubUser.getLogin()).openId(String.valueOf(githubUser.getId())).headerUrl(githubUser.getAvatarUrl()).build();
                user = iUserService.insert(user);
                if (user.getId() != null) {
                    logger.info("注册用户成功！当前注册用户" + user);
                } else {
                    logger.error("注册用户失败！");
                    model.addAttribute("reason", "注册用户失败");
                    return "error/500";
                }
            } else {
                user = users.get(0);
                user.setUsername(githubUser.getLogin());
                user.setHeaderUrl(githubUser.getAvatarUrl());
                iUserService.update(user);
            }

            // 生成登录凭证
            LoginTicket loginTicket = new LoginTicket();
            loginTicket.setUserId(user.getId());
            loginTicket.setTicket(CommunityUtil.generateUUID());
            loginTicket.setStatus(1);
            loginTicket.setExpired(new Date(System.currentTimeMillis() + DEFAULT_EXPIRED_SECONDS * 1000));

            String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
            redisTemplate.opsForValue().set(redisKey, loginTicket);

            Cookie cookie = new Cookie("ticket", loginTicket.getTicket());
            cookie.setPath(contextPath);
            cookie.setMaxAge(DEFAULT_EXPIRED_SECONDS);
            response.addCookie(cookie);
            return "redirect:/index";
        } else {
            logger.error("callback get github error,{}", githubUser);
            model.addAttribute("reason", "未获取到github用户信息");
            return "error/500";
        }
    }


    /**
     * @return void
     * @Description 请求QQ登录
     * @Author xw
     * @Date 18:27 2020/2/25
     * @Param []
     **/
    @GetMapping("/tencentqqLogin")
    public void login(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("text/html;charset=utf-8");
        try {
            response.sendRedirect(new Oauth().getAuthorizeURL(request));
            logger.info("请求QQ登录,开始跳转...");
        } catch (QQConnectException | IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * @return java.lang.String
     * @Description QQ登录回调地址
     * @Author xw
     * @Date 18:27 2020/2/25
     * @Param []
     **/
    @GetMapping("/tencentqqCallback")
    public String connection(HttpServletRequest request, HttpServletResponse response, Model model) {
        try {
            AccessToken accessTokenObj = (new Oauth()).getAccessTokenByRequest(request);
            String accessToken = null, openID = null;
            long tokenExpireIn = 0L;
            if ("".equals(accessTokenObj.getAccessToken())) {
                logger.error("登录失败:没有获取到响应参数");
                model.addAttribute("reason", "没有获取到响应参数");
                return "error/500";
            } else {
                accessToken = accessTokenObj.getAccessToken();
                tokenExpireIn = accessTokenObj.getExpireIn();
                logger.info("accessToken" + accessToken);
                request.getSession().setAttribute("demo_access_token", accessToken);
                request.getSession().setAttribute("demo_token_expirein", String.valueOf(tokenExpireIn));
                // 利用获取到的accessToken 去获取当前用的openid -------- start
                OpenID openIDObj = new OpenID(accessToken);
                openID = openIDObj.getUserOpenID();
                UserInfo qzoneUserInfo = new UserInfo(accessToken, openID);
                UserInfoBean userInfoBean = qzoneUserInfo.getUserInfo();
                if (userInfoBean.getRet() == 0) {
                    logger.info("用户的OPEN_ID: " + openID);
                    logger.info("用户的昵称: " + CommunityUtil.removeNonBmpUnicode(userInfoBean.getNickname()));
                    logger.info("用户的头像URI: " + userInfoBean.getAvatar().getAvatarURL100());

                    //设置用户信息
                    List<User> users = iUserService.queryAll(User.builder().openId(openID).build());
                    if (users.size() > 1) {
                        logger.error("根据信息检索出了超过一条用户");
                        model.addAttribute("reason", "根据信息检索出了超过一条用户");
                        return "error/500";
                    }
                    User user = null;
                    if (users.size() == 0) {
                        user = User.builder()
                                .openId(openID).username(CommunityUtil.removeNonBmpUnicode(userInfoBean.getNickname()))
                                .headerUrl(userInfoBean.getAvatar().getAvatarURL100()).
                                        createTime(new Date()).registerType(2).build();
                        User insert = iUserService.insert(user);
                        if (insert.getId() != null) {
                            logger.info("注册用户成功！当前注册用户" + user);
//                            User store = User.builder().id(insert.getId()).build();
//                            if (iUserService.addUser(store) == 1){
//                                user.setUserId(store.getUserId());
//                                iUserService.update(user);
//                                logger.info("注册仓库成功！当前注册仓库" + store);
//                              }
                        } else {
                            logger.error("注册用户失败");
                            model.addAttribute("reason", "注册用户失败");
                            return "error/500";
                        }
                    } else {
                        user = users.get(0);
                        user.setUsername(CommunityUtil.removeNonBmpUnicode(userInfoBean.getNickname()));
                        user.setHeaderUrl(userInfoBean.getAvatar().getAvatarURL100());
                        iUserService.update(user);
                    }
                    logger.info("QQ用户登录成功！" + user);

                    // 生成登录凭证
                    LoginTicket loginTicket = new LoginTicket();
                    loginTicket.setUserId(user.getId());
                    loginTicket.setTicket(CommunityUtil.generateUUID());
                    loginTicket.setStatus(1);
                    loginTicket.setExpired(new Date(System.currentTimeMillis() + DEFAULT_EXPIRED_SECONDS * 1000));

                    String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
                    redisTemplate.opsForValue().set(redisKey, loginTicket);

                    Cookie cookie = new Cookie("ticket", loginTicket.getTicket());
                    cookie.setPath(contextPath);
                    cookie.setMaxAge(DEFAULT_EXPIRED_SECONDS);
                    response.addCookie(cookie);
                    return "redirect:/index";
                } else {
                    logger.error("未获取到用户信息");
                    model.addAttribute("reason", "未获取到用户信息");
                    return "error/500";
                }
            }
        } catch (QQConnectException e) {
            e.printStackTrace();
            model.addAttribute("reason", "服务器发生了故障");
            return "error/500";
        }
    }

    @ResponseBody
    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {

        iUserService.logout(ticket);
        SecurityContextHolder.clearContext();
        return CommunityUtil.getJSONString(0, "注销成功");
    }



    /*
    @Deprecated
    @RequestMapping(path = "/registerSuccess")
    public String registerSuccess(Model model) {
        model.addAttribute("msg", "注册成功,我们已经向您的邮箱发送了一封激活邮件,请尽快激活!");
        model.addAttribute("target", "/index");
        return "forum/info/operate-result";
    }
     */

}
