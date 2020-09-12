package site.pyyf.commons.event.Handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import site.pyyf.blog.controller.BaseController;
import site.pyyf.commons.event.EventHandler;
import site.pyyf.commons.utils.CommunityConstant;
import site.pyyf.commons.utils.MailClient;
import site.pyyf.forum.entity.Event;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


@Component
public class EmailSendHandler  extends BaseController implements EventHandler, CommunityConstant {

    @Autowired
    protected MailClient mailClient;

    @Autowired
    protected TemplateEngine templateEngine;

    @Override
    public void doHandle(Event event) {
        Map<String, Object> emailSetting = event.getData();
        Context context = new Context();
        context.setVariable("username", (String) emailSetting.get("username"));
        context.setVariable("url", (String) emailSetting.get("url"));
        context.setVariable("title", (String) emailSetting.get("title"));
        context.setVariable("content", (String) emailSetting.get("content"));
        String content = null;
        if ((int) emailSetting.get("emailType") == EMAIL_TYPE_BLOG_USER)
            content = templateEngine.process("mail/notifyBlogUser", context);
        else
            content = templateEngine.process("mail/notifyCommentUser", context);
        mailClient.sendMail((String) emailSetting.get("email"), "【鹏圆云方博客】", content);
    }

    @Override
    public List<String> getSupportEventTypes() {
        return Arrays.asList(TOPIC_EMAIL);
    }
}
