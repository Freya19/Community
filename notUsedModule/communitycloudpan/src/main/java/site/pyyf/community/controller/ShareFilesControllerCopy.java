//package site.pyyf.community.controller;
//
//
//import site.pyyf.community.annotation.LoginRequired;
//import site.pyyf.commons.entity.Page;
//import site.pyyf.community.service.ShareFilesService;
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.multipart.MultipartFile;
//
//import javax.servlet.http.HttpServletResponse;
//import java.io.*;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
////@Controller
//
//public class ShareFilesControllerCopy {
//
//    private static final Logger logger = LoggerFactory.getLogger(ShareFilesControllerCopy.class);
//
//    @Value("${community.path.upload}")
//    private String uploadPath;
//
//    @Autowired
//    public ShareFilesService shareFilesService;
//
//
//    @RequestMapping(path = "/shareFiles_back/{path}")
//    public String getNextTopPage(@PathVariable(value = "path") String path) {
//        if (!path.equals("index")) {
//
//            //dir1*dir2
//            int endIndex = path.lastIndexOf('*');
//            //dir1
//            if (endIndex == -1)
//                return "redirect:/shareFiles/index";
//
//            return "redirect:/shareFiles/" + path.substring(0, endIndex);
//        }
//        return "redirect:/shareFiles/index";
//    }
//
//
//    // /shareFiles/index
//    @RequestMapping(path = "/shareFiles/{path}")
//    public String getPage(@PathVariable("path") String path, Model model, Page page) {
//
//        File shareDir = new File(uploadPath + "/shareFiles/");
//        if (!shareDir.exists())
//            shareDir.mkdirs();
//
//        String realPath = path.replace("*", "/");
//        // 分页信息
//        page.setLimit(14);
//        // /shareFiles/index
//        page.setPath("/shareFiles/" + path);
//
//        int size = shareFilesService.fileSize(realPath);
//        page.setRows(size);
//
//        // files = java
//        List<String> files = shareFilesService.list(page.getCurrent(), page.getLimit(), realPath);
//
//        model.addAttribute("files", files);
//        model.addAttribute("path", path);
//        return "site/shareFiles";
//    }
//
//
//    @LoginRequired
//    @RequestMapping(path = "/files/upload", method = RequestMethod.POST)
//    public String uploadFile(@RequestParam(value = "file") MultipartFile url,
//                             @RequestParam(value = "path") String path, Model model, Page page) {
//        if (url == null) {
//            model.addAttribute("error", "您还没有选择文件!");
//            return "site/shareFiles";
//        }
//
//        String fileName = url.getOriginalFilename();
//        String suffix = fileName.substring(fileName.lastIndexOf("."));
//        if (StringUtils.isBlank(suffix)) {
//            model.addAttribute("error", "文件的格式不正确!");
//            return "site/shareFiles";
//        }
//        File dest = null;
//        if ((page.getPath() == null)||(page.getPath().equals("index"))) {
//            dest = new File(uploadPath + "/ShareFiles/" + fileName);
//
//        } else {
//            dest = new File(uploadPath + "/ShareFiles/" + path.replace("*", "/") + "/" + fileName);
//        }
//        // 确定文件存放的路径
//        String parent = dest.getParent();
//        new File(parent).mkdirs();
//        try {
//            // 存储文件
//            url.transferTo(dest);
//        } catch (IOException e) {
//            logger.error("上传文件失败: " + e.getMessage());
//            throw new RuntimeException("上传文件失败,服务器发生异常!", e);
//        }
//
//        // 分页信息
//        page.setLimit(14);
//        if (page.getPath() == null) {
//            page.setPath("/shareFiles/index");
//        } else {
//            page.setPath("/shareFiles/" + page.getPath());
//        }
//
//        int size = shareFilesService.fileSize(path.replace("*", "/"));
//        page.setRows(size);
//
//        // files = {"dir1*dir2*dir3"}
//        List<String> files = shareFilesService.list(page.getCurrent(), page.getLimit(), path.replace("*", "/"));
//
//        model.addAttribute("files", files);
//        model.addAttribute("path", path);
//        return "site/shareFiles";
//    }
//
//    // /files/dir1*dir2*dir3
//    @RequestMapping(path = "/files/{path}", method = RequestMethod.GET)
//    public String getFile(@PathVariable("path") String path, HttpServletResponse response, Page page, Model model) {
//        // 转换路径，dir1*dir2*dir3 -> dir1/dir2/dir3
//        Map<String,String> supMDLang = new HashMap<>();
//        supMDLang.put("cpp","cpp");
//        supMDLang.put("java","java");
//        supMDLang.put("html","html");
//        supMDLang.put("py","python");
//        supMDLang.put("md","");
//
//        String realPath = path.replace("*", "/");
//        realPath = uploadPath + "/ShareFiles/" + realPath;
//
//        if (new File(realPath).isDirectory()) {
//            path = path.replace("/", "*");
//            model.addAttribute("path", path);
//            return "redirect:/shareFiles/" + path;
//        } else if (supMDLang.containsKey(realPath.substring(realPath.lastIndexOf(".") + 1))) {
//            StringBuilder code = new StringBuilder();
//            try (
//                    BufferedReader br = new BufferedReader(new FileReader(realPath));
//            ) {
//                String buffer = null;
//                while ((buffer = br.readLine()) != null) {
//                    buffer+='\n';
//                    code.append(buffer);
//                }
//            } catch (Exception e) {
//                logger.error("读取java文件失败: " + e.getMessage());
//            }
//
//            String suffix = realPath.substring(realPath.lastIndexOf(".") + 1);
//            // java 启动在线编译器
//            if(suffix.equals("java")){
//                model.addAttribute("javaFileContent", code);
//                return "site/onlineExecutor";
//            } else if(suffix.equals("md")){
//                model.addAttribute("code", code);
//                return "site/show-code";
//            }else{
//                String language = supMDLang.get(suffix);
//                StringBuilder suffixAndCode = new StringBuilder();
//                suffixAndCode.append("```").append(language).append("\n").append(code).append("```");
//                // 其他语言 启动mardown显示
//                model.addAttribute("code", suffixAndCode);
//                return "site/show-code";
//            }
//
//        } else {
//
//            // 响应图片
//            response.setContentType("application/x-download");
//            try (
//                    FileInputStream fis = new FileInputStream(realPath);
//                    OutputStream os = response.getOutputStream();
//            ) {
//                byte[] buffer = new byte[1024];
//                int b = 0;
//                while ((b = fis.read(buffer)) != -1) {
//                    os.write(buffer, 0, b);
//                }
//            } catch (IOException e) {
//                logger.error("读取其他文件失败: " + e.getMessage());
//            }
//        }
//        return "site/shareFiles";
//    }
//
//}
