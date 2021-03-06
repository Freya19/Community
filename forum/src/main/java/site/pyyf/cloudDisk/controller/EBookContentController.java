package site.pyyf.cloudDisk.controller;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import site.pyyf.cloudDisk.entity.Ebook;
import site.pyyf.cloudDisk.entity.EbookContent;
import site.pyyf.cloudDisk.entity.Header;
import site.pyyf.cloudDisk.utils.CloudDiskUtil;
import site.pyyf.commons.utils.MarkdownToHtmlUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@Controller
public class EBookContentController extends CloudDiskBaseController {

    @RequestMapping(path = "/ebook/getbook/{fileId}")
    public String getEbook(@PathVariable(value = "fileId") int fileId, Model model) {

        //搜到只会有一个
        Ebook ebook = iEbookService.queryAll(Ebook.builder().fileId(fileId).build()).get(0);
        //Feature.OrderedField表示解析时按照顺序解析，不要打乱List中元素相对顺序
        Header directory = JSONObject.parseObject(ebook.getHeader(), Header.class, Feature.OrderedField);
        model.addAttribute("headers", directory);
        return "cloudDisk/ebook/ebook";
    }


    @ResponseBody
    @RequestMapping(value = "/ebook/getcontent", method = RequestMethod.POST)
    public String getcontent(@RequestParam("contentId") String contentId) {
        //搜到只会有一个
        String content = iEbookContentService.queryAll(EbookContent.builder().contentId(contentId).build()).get(0).getContent();
        String htmlContent = MarkdownToHtmlUtil.markdownToHtmlExtensions(content);
        StringBuilder processedContent = new StringBuilder();
        int i = 0;
        char buf;
        while (i < htmlContent.length()) {
            if ((buf = htmlContent.charAt(i)) == '\n' && i != 0) {
                if (CloudDiskUtil.isChineseNumberAlpha(htmlContent.charAt(i - 1)))
                    processedContent.append("<br>");
            }
            if (htmlContent.charAt(i) == '~')
                processedContent.append("<br>");
            else
                processedContent.append(buf);
            i++;
        }

        StringBuilder newCode = iCodeService.addHtmlShowStyle(iCodeService.addHtmlCompileModule(processedContent, "java"), Arrays.asList("java", "cpp", "python", "html"));
        return CloudDiskUtil.getJSONString(200, newCode.toString());
    }

    @RequestMapping(value = "/ebook/goupdate", method = RequestMethod.POST)
    public String goUpdatePage(@RequestParam("contentId") String contentId,
                               Model model) {
        String content = iEbookContentService.queryAll(EbookContent.builder().contentId(contentId).build()).get(0).getContent();
        model.addAttribute("content", content);
        model.addAttribute("contentId", contentId);
        return "cloudDisk/ebook/update";
    }

    @RequestMapping(value = "/ebook/update", method = RequestMethod.POST)
    public String updateContent(@RequestParam("content") String content,
                                @RequestParam("contentId") String contentId,
                                Model model) {
        iEbookContentService.update(EbookContent.builder().contentId(contentId).content(content).build() );
        int ebookId = iEbookContentService.queryAll(EbookContent.builder().contentId(contentId).build()).get(0).getFileId();
        return "redirect:/ebook/getbook/" + ebookId;
    }


}
