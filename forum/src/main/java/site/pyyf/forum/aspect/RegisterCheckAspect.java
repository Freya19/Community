package site.pyyf.forum.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import site.pyyf.commons.utils.CommunityUtil;
import site.pyyf.forum.service.ISiteSettingService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Aspect
public class RegisterCheckAspect {

    @Autowired
    ISiteSettingService iSiteSettingService;

    @Pointcut("execution(* site.pyyf.forum.controller.LoginController.register(..))")
    public void pointcut() {

    }

    @Before("pointcut()")
    public void before(JoinPoint joinPoint) throws IOException {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();
        String xRequestedWith = request.getHeader("x-requested-with");

        // XML。。异步请求（POST）
        if ("XMLHttpRequest".equals(xRequestedWith)) {
            assert response != null;
            response.setContentType("application/plain;charset=utf-8");
            if (iSiteSettingService.allowRegister() == 0) {
                PrintWriter writer = response.getWriter();
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("usernameMsg", "本网站由于安全性暂关闭注册，敬请谅解");
                writer.write(CommunityUtil.getJSONString(1, "注册失败", resultMap));
            }
        } else {
            // 同步请求
            assert response != null;
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }
}
