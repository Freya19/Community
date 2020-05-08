package site.pyyf.forum.controller;

import site.pyyf.forum.entity.Page;
import site.pyyf.forum.entity.UploadResult;
import site.pyyf.commons.utils.CommunityUtil;
import site.pyyf.forum.annotation.LoginRequired;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;

@CrossOrigin
@Controller
public class OSSController extends CommunityBaseController {

    @LoginRequired
    @ResponseBody
    @RequestMapping(path = "/OSS/upload", method = RequestMethod.POST)
    public String uploadFile(@RequestParam(value = "file") MultipartFile url,
                             Model model, Page page) {
        // path: ""   dir1*dir2
        if (url == null) {
            model.addAttribute("error", "您还没有选择文件!");
            return "forum/shareFiles";
        }
        String fileName = url.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件的格式不正确!");
        }

        final UploadResult result = iAliyunOssService.upload("fileBase",url );
        iAliyunOssService.insertDatabase(fileName, result.getUrl());

        return CommunityUtil.getJSONString(200, result.getUrl());

    }

    @ResponseBody
    @RequestMapping(path = "/img/upload")
    public UploadResult uploadFile(HttpServletRequest request) {

        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultipartFile file = multipartRequest.getFile("editormd-image-file");

        return iAliyunOssService.upload( "post/img",file);


    }
}



