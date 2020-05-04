package site.pyyf.community.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import site.pyyf.commons.utils.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import site.pyyf.community.service.IExecuteStringSourceService;

import java.io.IOException;


@Controller
public class OnlineExecutorController extends CommunityBaseController {
    private Logger logger = LoggerFactory.getLogger(OnlineExecutorController.class);

    @Reference(loadbalance = "random", timeout = 5000, check = false) //dubbo直连
    private IExecuteStringSourceService iExecuteStringSourceService;

    private static final String defaultSource = "public class Run {\n"
            + "    public static void main(String[] args) {\n"
            + "        \n"
            + "    }\n"
            + "}";

    @RequestMapping(path = "/onlineExecutor", method = RequestMethod.GET)
    public String getHtml(Model model) {
        if (iSiteSettingService.allowOnlineExecutor() == 1)
            return "site/onlineExecutor";
        else {
            model.addAttribute("msg", "非常抱歉，该功能正在测试中，请谅解");
            model.addAttribute("target", "/index");
            return "site/info/operate-result";
        }

    }

    @HystrixCommand(fallbackMethod="errorExec")
    @RequestMapping(path = "/onlineExecutor", method = RequestMethod.POST)
    @ResponseBody
    public String PostCode(@RequestParam(value = "input",defaultValue = "") String input,
                           @RequestParam("code") String code) throws IOException {
        String runResult = iExecuteStringSourceService.execute(code, input);
        runResult = runResult.replaceAll(System.lineSeparator(), "   "); // 处理html中换行的问题
        System.out.println(runResult);
        return CommunityUtil.getJSONString(200, runResult);
    }

    public String errorExec(@RequestParam(value = "code") String code,
                            @RequestParam(value = "input",defaultValue = "") String input) {

        String runResult = "服务器发生了错误，请稍后再试";
        return CommunityUtil.getJSONString(200, runResult);
    }

}
