package site.pyyf.cloudDisk.controller;

import site.pyyf.cloudDisk.entity.FileFolder;
import site.pyyf.cloudDisk.entity.MyFile;
import site.pyyf.cloudDisk.service.IResolveHeaderService;
import site.pyyf.cloudDisk.utils.*;
import site.pyyf.cloudDisk.utils.*;
import site.pyyf.forum.config.AliyunConfig;
import site.pyyf.forum.entity.UploadResult;
import site.pyyf.forum.entity.User;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Controller
@Scope("prototype")
public class ShareController extends CloudDiskBaseController implements CloudDiskConstant {

    private static final Logger logger = LoggerFactory.getLogger(ShareController.class);

    @Autowired
    private AliyunConfig aliyunConfig;
    @Autowired
    protected IResolveHeaderService iResolveHeaderService;


    /**
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @Description 获得二维码 和直链下载地址
     * @Author xw
     * @Date 15:20 2020/2/12
     * @Param [id, url]
     **/
    @GetMapping("getQrCode")
    @ResponseBody
    public Map<String, Object> getQrCode(@RequestParam Integer id, @RequestParam String url) {
        Map<String, Object> map = new HashMap<>();
        map.put("imgPath", "https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=2654852821,3851565636&fm=26&gp=0.jpg");
        if (id != null) {
            MyFile file = iMyFileService.queryById(id);
            if (file != null) {
                try {
                    String rootPath = this.getClass().getResource("/").getPath().replaceAll("^\\/", "") + "user_img";
                    url = url + "/file/linearDownload?t=" + UUID.randomUUID().toString().substring(0, 10) + "&f=" + file.getId() + "&p=" + file.getUploadTime().getTime() + "" + file.getSize();
                    File targetFile = new File(rootPath, "");
                    if (!targetFile.exists()) {
                        targetFile.mkdirs();
                    }
                    File f = new File(rootPath, id + ".jpg");
                    if (!f.exists()) {
                        //文件不存在,开始生成二维码并保存文件
                        OutputStream os = new FileOutputStream(f);
                        QRCodeUtil.encode(url, new URL("https://pyyf.oss-cn-hangzhou.aliyuncs.com/community/icons/cloud.png"), os, true);
                        os.close();
                    }
                    UploadResult upload = aliyunOssService.upload("cloudDisk/QrCode",f );

                    map.put("imgPath", upload.getUrl());
                    map.put("url", url);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return map;
    }


    /**
     * @return void
     * @Description 获取直链下载
     * @Author xw
     * @Date 14:23 2020/2/12
     * @Param [fId]
     **/
    @GetMapping("/file/linearDownload")
    public void linearDownload(Integer f, String p, String t) throws IOException {
        //获取文件信息
        MyFile myFile = iMyFileService.queryById(f);
        String pwd = myFile.getUploadTime().getTime() + "" + myFile.getSize();
        if (t == null) {
            return;
        }
        if (!pwd.equals(p)) {
            return;
        }
        if (myFile == null) {
            return;
        }
        String filePath = myFile.getMyFilePath();
        String fileName = myFile.getMyFileName();
        logger.debug("文件位置" + filePath + fileName);

        OutputStream os = null;
        try {
            os = new BufferedOutputStream(response.getOutputStream());
            logger.debug("开始下载");
            response.setCharacterEncoding("utf-8");
            // 设置返回类型
            response.setContentType("multipart/form-data");
            // 文件名转码一下，不然会出现中文乱码
            response.setHeader("Content-Disposition", "attachment;fileName=" + URLEncoder.encode(fileName, "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (filePath.startsWith("http")) {
            //配置是OSS或者是图片则从OSS中下载，因为图片始终存放在OSS中
            try {
                logger.debug("开始下载");
                aliyunOssService.download(filePath.substring(aliyunConfig.getUrlPrefix().length()), os);
                iMyFileService.update(
                        MyFile.builder().id(myFile.getId()).downloadTime(myFile.getDownloadTime() + 1).build());
                logger.debug("文件从OSS下载成功");
            } catch (Exception e) {
                e.printStackTrace();
                logger.debug("文件从OSS下载失败");
                os.close();
                return;
            }
        } else {
            //去FTP上拉取
            logger.debug("开始下载");
            boolean FTPdownLoadRes = FtpUtil.downloadFile("/" + filePath, os);
            if (FTPdownLoadRes){
                logger.debug("文件从FTP下载成功");
                iMyFileService.update(
                        MyFile.builder().id(myFile.getId()).downloadTime(myFile.getDownloadTime() + 1).build());
            }
            else {
                logger.debug("文件从FTP下载失败!" + myFile);
                os.close();
                return;
            }
        }
        os.flush();
        os.close();

    }


    @ResponseBody
    @RequestMapping(value = "/getShareUrl", method = RequestMethod.POST)
    public String getShareUrl(@RequestParam(value = "fId") Integer id,
                              @RequestParam(value = "type") String type) {
        String pwd = UUID.randomUUID().toString().replaceAll("-", "");
        String typeAndId = type + "-" + id;

        // 将验证码存入Redis
        String shareKey = RedisKeyUtil.getShareKey(pwd);
        redisTemplate.opsForValue().set(shareKey, typeAndId, 7, TimeUnit.DAYS);
        return CloudDiskUtil.getJSONString(200, pwd);

    }


    @ResponseBody
    @RequestMapping(value = "/share")
    public String share(@RequestParam(value = "fileFolderId") Integer toFileFolderId,
                        @RequestParam(value = "pwd") String pwd) {
        Map<Integer, String> map = new HashMap();
        map.put(200, "转存完成！");
        map.put(501, "当前文件已存在!上传失败！");
        map.put(502, "分享码已过期！");
        map.put(503, "上传失败!仓库已满！");
        map.put(504, "文件转存失败！");
        map.put(500, "系统错误，请联系管理员！");

        String shareKey = RedisKeyUtil.getShareKey(pwd);
        String typeAndFid = (String) redisTemplate.opsForValue().get(shareKey);
        if (StringUtils.isBlank(typeAndFid))
            return CloudDiskUtil.getJSONString(502, map.get(502));


        String[] split = typeAndFid.split("-");
        if (split[0].equals("folder")) {
            //分享的是文件夹
            FileFolder fileFolder = iFileFolderService.queryById(Integer.valueOf(split[1]));
            int result = transferSaveFolder(fileFolder, toFileFolderId);
            return CloudDiskUtil.getJSONString(result, map.get(result));
        } else if (split[0].equals("file")) {
            MyFile shareFile = iMyFileService.queryById(Integer.valueOf(split[1]));
            int result = transferSaveFile(shareFile, toFileFolderId);
            return CloudDiskUtil.getJSONString(result, map.get(result));

        } else {
            logger.error("传来一个不知名的内容");
            return CloudDiskUtil.getJSONString(500, map.get(500));
        }
    }

    //将文件夹fileFolder放到fileFolderId中
    public int transferSaveFolder(FileFolder fileFolder, Integer toFileFolderId) {
        User user = iUserService.queryById(ROOTUSERID);

        List<MyFile> files = iMyFileService.queryAll(MyFile.builder().userId(ROOTUSERID).parentFolderId(fileFolder.getId()).build());
        //设置文件夹信息
        FileFolder thisFolder = FileFolder.builder()
                .fileFolderName(fileFolder.getFileFolderName())
                .parentFolderId(toFileFolderId)
                .createTime(new Date())
                .userId(user.getId()).build();
        iFileFolderService.insert(thisFolder);
        for (MyFile file : files) {
            transferSaveFile(file, thisFolder.getId());
        }

        List<FileFolder> folders = iFileFolderService.queryAll(FileFolder.builder().userId(ROOTUSERID).parentFolderId(fileFolder.getId()).build());
        for (FileFolder folder : folders) {
            transferSaveFolder(folder, thisFolder.getId());
        }
        return 200;
    }

    //将shareFile放在fileFolderId中
    public int transferSaveFile(MyFile shareFile, Integer toFileFolderId) {
        User user = iUserService.queryById(ROOTUSERID);
        //获取当前目录下的所有文件，用来判断是否已经存在
        List<MyFile> myFiles = iMyFileService.queryAll(MyFile.builder().userId(ROOTUSERID).parentFolderId(toFileFolderId).build());
        for (int i = 0; i < myFiles.size(); i++) {
            if (myFiles.get(i).getMyFileName().equals(shareFile.getMyFileName())) {
                logger.error("当前文件已存在!上传失败...");
                return 501;
            }
        }

        Integer sizeInt = Math.toIntExact(shareFile.getSize() / 1024);
        //是否仓库放不下该文件
        if (user.getCurrentSize() + sizeInt > user.getMaxSize()) {
            logger.error("上传失败!仓库已满。");
            return 503;
        }


        //文件转存成功后则需要插入数据库
        MyFile insertFile = MyFile.builder().parentFolderId(toFileFolderId)
                .myFileName(shareFile.getMyFileName()).userId(ROOTUSERID)
                .downloadTime(0).uploadTime(new Date()).size(shareFile.getSize())
                .type(shareFile.getType()).postfix(shareFile.getPostfix()).build();

        iFileStoreService.transeferFile(shareFile, insertFile);

        if (insertFile == null) {
            logger.error("当前文件上传失败...");
            return 504;
        }
        //向数据库文件表写入数据
        iMyFileService.insert(insertFile);
        //更新仓库表的当前大小
        //更新仓库表的当前大小
        iUserService.update(User.builder()
                .id(user.getId())
                .currentSize(iUserService.queryById(user.getId()).getCurrentSize() + shareFile.getSize())
                .build());
        logger.debug("转存文件插入数据库成功");


        //如果是markdown，则再传一份到library表中
        if (insertFile.getPostfix().equals(".md")) {
            try {
                iResolveHeaderService.readFile(insertFile.getMyFilePath(), insertFile.getMyFileName(), insertFile.getId());
                logger.debug("文件转存过程中markdown转化成功");
                return 200;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("文件转存过程中markdown转化失败");
                return 504;
            }
        }
        return 200;
    }
}

