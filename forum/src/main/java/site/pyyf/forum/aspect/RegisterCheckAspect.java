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
import site.pyyf.forum.Exception.NotAllowedRegisterException;
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
    public void before(JoinPoint joinPoint) throws IOException, NotAllowedRegisterException {
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
            if (iSiteSettingService.allowRegister() == 0) {
                throw new NotAllowedRegisterException();
            }
        } else {
            // 同步请求
            assert response != null;
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }
}
