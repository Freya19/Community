package site.pyyf.community.controller;


import site.pyyf.community.annotation.LoginRequired;
import site.pyyf.community.entity.Page;
import site.pyyf.commons.utils.CookieUtil;
import site.pyyf.commons.utils.MultipartSerializable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import site.pyyf.community.entity.File;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class CloudPanCookieController extends BaseController  {

    private static final Logger logger = LoggerFactory.getLogger(CloudPanCookieController.class);

    @RequestMapping(path = "/shareFilesSetting/back")
    public String getNextTopPage( HttpServletRequest request, HttpServletResponse response) {

        String currentCloudPath = CookieUtil.getValue(request, "currentcloudpath");

        if (!currentCloudPath.equals("index")) {

            //dir1*dir2
            int endIndex = currentCloudPath.lastIndexOf('*');
            //dir1
            if (endIndex == -1){
                CookieUtil.moldifyValue(request, response,"currentcloudpath","index");
                return "redirect:/shareFiles/show";

            }
            else{
                CookieUtil.moldifyValue(request,response, "currentcloudpath",currentCloudPath.substring(0, endIndex));
                return "redirect:/shareFiles/show";
            }

        }
        return "redirect:/shareFiles/show";
    }


    @RequestMapping(path = "/shareFilesSetting/mkdir")
    public String mkdir(@RequestParam(value = "dirname") String dirname
            , Page page, Model model,HttpServletRequest request, HttpServletResponse response) {
        if(dirname.equals("")){
            return "redirect:/shareFiles/show";
        }

        String currentCloudPath = CookieUtil.getValue(request, "currentcloudpath");

        // 分页信息
        page.setLimit(14);
        //  absolute_path:   dir2*file  absolute_path:   index
        String realPath = currentCloudPath.replace("*", "/");

        if (realPath.equals("index")) {
            page.setPath("/shareFiles/show");
            cloudPanService.insertFileSystem(dirname, currentCloudPath + "*" + dirname);

        } else {
            page.setPath("/shareFiles/show" );
            cloudPanService.insertFileSystem(realPath + "/" + dirname, currentCloudPath + "*" + dirname);
        }


        int size = cloudPanService.fileSize(realPath);
        page.setRows(size);

        // files = {"dir1*dir2*dir3"}
        List<site.pyyf.community.entity.File> files = cloudPanService.list(page.getCurrent(), page.getLimit(), realPath);

        model.addAttribute("files", files);

        return "site/shareFiles";
    }


    // /shareFiles/index   /shareFiles/dir1*dir2  /shareFiles/dir2*file
    @RequestMapping(path = "/shareFiles/show")
    public String getPage(Model model, Page page,@RequestParam(value="dirname",defaultValue = "") String dirname,
                          HttpServletRequest request, HttpServletResponse response) {

        String currentCloudPath = CookieUtil.getValue(request, "currentcloudpath");

        //第一次访问页面
        if (currentCloudPath == null) {
            Cookie cookie = new Cookie("currentcloudpath", "index");
            cookie.setMaxAge(24*3600);
            cookie.setPath("/");
            response.addCookie(cookie);
            currentCloudPath="index";
        }
        // 点击一个文件夹
        if(!dirname.equals("")){
            if(currentCloudPath.equals("index")){
                CookieUtil.moldifyValue(request,response,"currentcloudpath",dirname);
                currentCloudPath=dirname;
            }
            else{
                CookieUtil.moldifyValue(request,response,"currentcloudpath",currentCloudPath+"*"+dirname);
                currentCloudPath=currentCloudPath+"*"+dirname;
            }
        }

        // path:  index   dir1*dir2 file
        String realPath = currentCloudPath.replace("*", "/");
        String[] strings = realPath.split("/");
        String fileName = strings[strings.length - 1];
        //表明是文件
        if (!currentCloudPath.equals("index") && (fileName.split(".").length != 0))
            //path: file
            return "redirect:/shareFiles/file/" + currentCloudPath;

        // index dir1/dir2
        int size = cloudPanService.fileSize(realPath);
        page.setRows(size);

        // 分页信息
        page.setLimit(14);
        // /shareFiles/index  /shareFiles/dir1*dir2
        page.setPath("/shareFiles/show");

        List<File> files = cloudPanService.list(page.getCurrent(), page.getLimit(), realPath);

        model.addAttribute("files", files);
        return "site/shareFiles";
    }


    @LoginRequired
    @RequestMapping(path = "/files/upload", method = RequestMethod.POST)
    public String uploadFile(@RequestParam(value = "file") MultipartFile url,
                             Model model, Page page,HttpServletRequest request) {
        String currentCloudPath = CookieUtil.getValue(request, "currentcloudpath");
        // path: ""   dir1*dir2
        if (url == null) {
            model.addAttribute("error", "您还没有选择文件!");
            return "site/shareFiles";
        }
        String fileName = url.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件的格式不正确!");
            return "site/shareFiles";
        }

        final String fastdfsName = cloudPanService.uploadFile(MultipartSerializable.file2json(url));

        String absolutePath = null;
        if (currentCloudPath.equals("index")) {
            absolutePath = fileName;

        } else {
            absolutePath = currentCloudPath.replace("*", "/") + "/" + fileName;
        }

        // 分页信息
        page.setLimit(14);
        page.setPath("/shareFiles/show" );


        cloudPanService.insertFileSystem(absolutePath, fastdfsName);


        int size = cloudPanService.fileSize(currentCloudPath.replace("*", "/"));
        page.setRows(size);

        // files = {"dir1*dir2*dir3"}
        List<File> files = cloudPanService.list(page.getCurrent(), page.getLimit(), currentCloudPath.replace("*", "/"));

        model.addAttribute("files", files);
        return "redirect:/shareFiles/show";

    }


    // shareFiles/show/file
    @RequestMapping(path = "/shareFiles/file/{absolute_path}", method = RequestMethod.GET)
    public String getFile(@PathVariable("absolute_path") String absolute_path,
                          @RequestParam("fastdfs_name") String fastdfs_name,
                          HttpServletResponse response, Model model) {



        //path:  file
        Map<String, String> supMDLang = new HashMap<>();
        supMDLang.put("cpp", "cpp");
        supMDLang.put("java", "java");
        supMDLang.put("html", "html");
        supMDLang.put("py", "python");
        supMDLang.put("md", "");


        final String suffix = absolute_path.substring(absolute_path.lastIndexOf(".") + 1);
        if (supMDLang.containsKey(suffix)) {
            StringBuilder code = new StringBuilder();
            try (
                    InputStream is = new URL(fastdfs_name).openStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
            ) {
                String buffer = null;
                while ((buffer = br.readLine()) != null) {
                    buffer += '\n';
                    code.append(buffer);
                }
            } catch (Exception e) {
                logger.error("读取java文件失败: " + e.getMessage());
            }

            // java 启动在线编译器
            if (suffix.equals("java")) {
                model.addAttribute("javaFileContent", code);
                return "site/onlineExecutor";
            } else if (suffix.equals("md")) {
                model.addAttribute("code", code);
                return "site/show-code";
            } else {
                String language = supMDLang.get(suffix);
                StringBuilder suffixAndCode = new StringBuilder();
                suffixAndCode.append("```").append(language).append("\n").append(code).append("```");
                // 其他语言 启动mardown显示
                model.addAttribute("code", suffixAndCode);
                return "site/show-code";
            }

        } else {

            // 响应文件
            response.setContentType("application/x-download");
            try (
                    InputStream is = new URL(fastdfs_name).openStream();
                    OutputStream os = response.getOutputStream();
            ) {
                byte[] buffer = new byte[1024];
                int b = 0;
                while ((b = is.read(buffer)) != -1) {
                    os.write(buffer, 0, b);
                }
            } catch (IOException e) {
                logger.error("读取其他文件失败: " + e.getMessage());
            }
        }
        return "site/shareFiles";
    }


    @RequestMapping(path = "/shareFilesSetting/delete", method = RequestMethod.GET)
    public String deleteFileAndDir(HttpServletRequest request,@RequestParam("fileName") String fileName) {
        String currentCloudPath = CookieUtil.getValue(request, "currentcloudpath");
        if(!fileName.equals("")){
            if(currentCloudPath.equals("index"))
                cloudPanService.deleteFileSystem(fileName);
            else
                cloudPanService.deleteFileSystem(currentCloudPath.replace("*","/")+"/"+fileName);
        }
        return "redirect:/shareFiles/show";
    }

    @RequestMapping(path = "/shareFilesSetting/modify", method = RequestMethod.GET)
    public String modifyFileAndDir(HttpServletRequest request,@RequestParam("oldFileName") String oldFileName,
                                   @RequestParam("newFileName") String newFileName) {
        String currentCloudPath = CookieUtil.getValue(request, "currentcloudpath");
        if(!oldFileName.equals("") && !newFileName.equals("")) {
            if(currentCloudPath.equals("index"))
                cloudPanService.modifyFileSystem(oldFileName,newFileName);
            else
                cloudPanService.modifyFileSystem(currentCloudPath.replace("*","/")+"/"+oldFileName,
                        currentCloudPath.replace("*","/")+"/"+newFileName );
        }
        return "redirect:/shareFiles/show";
    }

    @RequestMapping(path = "/shareFilesSetting/clean", method = RequestMethod.GET)
    public String cleanDeletedFiles() {
        List<File> files = cloudPanService.selectDeletedFiles();

        files.stream().forEach(f -> {
            cloudPanService.cleanFileSystem(f.getFastdfsName());
        });
        return "redirect:/shareFiles/show";
    }
}
