package site.pyyf.cloudDisk.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import site.pyyf.cloudDisk.entity.MyFile;
import site.pyyf.cloudDisk.service.IFileStoreService;
import site.pyyf.cloudDisk.utils.FtpUtil;
import site.pyyf.forum.entity.UploadResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import org.springframework.web.multipart.MultipartFile;


import javax.annotation.PostConstruct;
import java.io.*;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author 鹏圆
 * @ClassName UserServiceImpl
 * @Description (User)表服务实现类
 * @date 2020-02-25 17:19:31
 * @Version 1.0
 **/
@Service
public class FileStoreServiceImpl extends CloudDiskBaseController implements IFileStoreService {
    private static final Logger logger = LoggerFactory.getLogger(FileStoreServiceImpl.class);

    // 代码缓存
    private Cache<String, Object> previewCodeCache;

    @Value("${caffeine.code.max-size}")
    private int maxSize;

    @Value("${caffeine.code.expire-hours}")
    private int expireHours;

    @PostConstruct
    public void init() {
        // 初始化帖子列表缓存
        previewCodeCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireHours, TimeUnit.HOURS)
                .build();
    }

    /**
     * @return int 1:文本类型   2:图像类型  3:视频类型  4:音乐类型  5:其他类型
     * @Description 根据文件的后缀名获得对应的类型
     * @Author xw
     * @Date 23:20 2020/2/10
     * @Param [type]
     **/
    public int getType(String type) {


        if ("html".equals(type) || "css".equals(type) || "js".equals(type) || "c".equals(type) || "md".equals(type)
                || "java".equals(type) || "php".equals(type) || "py".equals(type) || "cpp".equals(type)) {
            return 1;
        } else if ("bmp".equals(type) || "gif".equals(type) || "jpg".equals(type)
                || "pic".equals(type) || "png".equals(type) || "jepg".equals(type) || "webp".equals(type)
                || "svg".equals(type)) {
            return 2;
        } else if ("avi".equals(type) || "mov".equals(type) || "qt".equals(type)
                || "asf".equals(type) || "rm".equals(type) || "navi".equals(type) || "wav".equals(type)
                || "mp4".equals(type) || "flv".equals(type)) {
            return 3;
        } else if ("mp3".equals(type) || "wma".equals(type) || "wav".equals(type) || "flac".equals(type)) {
            return 4;
        } else {
            return 5;
        }
    }

    @Override
    public StringBuilder getFileContentByMyFile(MyFile file) {
        //这里引入caffeine在内存中存储文件内容
        String caffeineKey = "code:"+file.getId();
        // 根据key查询一个缓存，如果没有返回NULL
        String cacheCode = (String)previewCodeCache.getIfPresent(caffeineKey);
        if(cacheCode!=null){
            logger.debug("查询"+file.getMyFileName()+"文件内容时,caffeine缓存击中");
            return new StringBuilder(cacheCode);
        }

        StringBuilder code = new StringBuilder();
        //获取文件信息
        String remotePath = file.getMyFilePath();

        try {
            File temp = new File("data/temp");
            if (!temp.exists()) {
                temp.mkdirs();
            }
            String tempStr = "data/temp/" + UUID.randomUUID().toString();
            //去FTP上拉取
            OutputStream tmpFileStream = new FileOutputStream(new File(tempStr));
            boolean flag = FtpUtil.downloadFile("/" + remotePath, tmpFileStream);
            if (flag) {
                //获得服务器本地的文件，并使用IO流写出到浏览器
                InputStream is = new BufferedInputStream(new FileInputStream(tempStr));
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String buffer = null;
                while ((buffer = br.readLine()) != null) {
                    buffer += '\n';
                    code.append(buffer);
                }
                is.close();
                tmpFileStream.close();
                logger.debug("文件下载成功!" + file.getMyFileName());

                new File(tempStr).delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.debug("查询"+file.getMyFileName()+"文件内容时,caffeine缓存未击中，从数据库中读取文件");
        previewCodeCache.put(caffeineKey, code.toString());
        return code;
    }


    @Override
    public void deleteFile(MyFile myFile) {
        String remotePath = myFile.getMyFilePath();
        String showPath = myFile.getShowPath();
        if (myFile.getMyFilePath().startsWith("http")) {

            boolean OSSdeleteRes = aliyunOssService.delete(remotePath.substring(aliyunConfig.getUrlPrefix().length()));
            if (OSSdeleteRes)
                logger.debug("remote文件从OSS删除成功");
            else {
                logger.debug("remote文件从OSS删除失败!" + myFile);
            }
            if (!remotePath.equals(showPath)) {
                //从OSS文件服务器上删除文件
                OSSdeleteRes = aliyunOssService.delete(showPath.substring(aliyunConfig.getUrlPrefix().length()));
                if (OSSdeleteRes)
                    logger.debug("show文件从OSS删除成功");
                else {
                    logger.debug("show文件从OSS删除失败!" + myFile);
                }
            }

        } else {
            //从FTP文件服务器上删除文件
            boolean FTPdeleteRes = FtpUtil.deleteFile("/" + remotePath);
            if (FTPdeleteRes)
                logger.debug("remote文件从FTP删除成功");
            else {
                logger.debug("remote文件从FTP删除失败!" + myFile);
            }

            if (!remotePath.equals(showPath)) {
                //从FTP文件服务器上删除文件
                FTPdeleteRes = FtpUtil.deleteFile("/" + showPath);
                if (FTPdeleteRes)
                    logger.debug("show文件从FTP删除成功");
                else {
                    logger.debug("show文件从FTP删除失败!" + myFile);
                }
            }
        }
    }

    @Override
    public void uploadTAudioFile(MultipartFile originalFile, MyFile fileItem) throws Exception {
        String insertRemotePath = null;
        String insertShowPath = null;


        InputStream uploadStream = originalFile.getInputStream();
        String rootPath = ClassUtils.getDefaultClassLoader().getResource("").getPath();
        final File tmpFolder = new File(rootPath + "data/audio");
        if (!tmpFolder.exists())
            tmpFolder.mkdirs();

        //保存
        File srcFile = new File(tmpFolder.getAbsolutePath() + "/" + UUID.randomUUID().toString() + "." + fileItem.getPostfix());
        FileOutputStream fos = new FileOutputStream(srcFile);
        byte[] buf = new byte[1024];
        int length;
        while ((length = uploadStream.read(buf)) > 0) {
            fos.write(buf, 0, length);
        }
        fos.close();
        logger.debug("非htmlSupAudio音乐文件存储成功");

        //非htmlSupAudio音乐转码为mp3音乐
        File dstFile = new File(tmpFolder.getAbsolutePath() + "/" + UUID.randomUUID().toString() + ".mp3");
        boolean tranferSuccess = iMediaTranfer.tranferAudio(srcFile, dstFile);
        if (tranferSuccess)
            logger.debug("非htmlSupAudio音乐文件转码成功");
        else {
            logger.error("非htmlSupAudio音乐转码失败");
            fileItem = null;
            return;
        }

        if (cloudDiskConfig.getType().equals("OSS")) {
            UploadResult OSSsrcUploadResult = aliyunOssService.upload("cloudDisk/audio",srcFile );
            UploadResult OSSdstUploadResult = aliyunOssService.upload("cloudDisk/audio",dstFile);

            if ((OSSsrcUploadResult.getSuccess()==1) && (OSSdstUploadResult.getSuccess()==1)) {

                logger.debug("非htmlSupAudio音乐源文件和转码文件上传到OSS完毕");
                insertRemotePath = OSSsrcUploadResult.getUrl();
                insertShowPath = OSSdstUploadResult.getUrl();
            } else {
                logger.error("非htmlSupAudio音乐源文件或目标文件上传到OSS失败");
            }
        } else {
            String remoteFilePath = fileItem.getUserId() + "/" + fileItem.getParentFolderId() + "/" + UUID.randomUUID().toString() + "." + fileItem.getPostfix();
            String showFilePath = fileItem.getUserId() + "/" + fileItem.getParentFolderId() + "/" + UUID.randomUUID().toString() + ".";

            FileInputStream srcStream = new FileInputStream(srcFile);
            final boolean FTPsrcUploadresult = FtpUtil.uploadFile("/" + remoteFilePath, srcStream);
            srcStream.close();

            FileInputStream dstStream = new FileInputStream(dstFile);
            final boolean FTPdstUploadresult = FtpUtil.uploadFile("/" + showFilePath + "mp3", dstStream);
            dstStream.close();
            if (FTPsrcUploadresult && FTPdstUploadresult) {
                logger.debug("非htmlSupAudio音乐源文件和转码文件上传到FTP完毕");
                insertRemotePath = remoteFilePath;
                insertShowPath = showFilePath + "mp3";
            } else {
                logger.error("非htmlSupAudio音乐源文件或目标文件上传到FTP失败");
            }

        }

        if ((insertRemotePath == null) || (insertShowPath == null)) {
            logger.error("当前文件上传失败...");
            fileItem = null;
            return;
        }
        if (dstFile != null) {
            if (dstFile.exists())
                dstFile.delete();
        }

        if (srcFile != null) {
            if (srcFile.exists())
                srcFile.delete();
        }
        fileItem.setMyFilePath(insertRemotePath);
        fileItem.setShowPath(insertShowPath);
    }

    @Override
    public void uploadTVideoFile(MultipartFile originalFile, MyFile fileItem) throws Exception {

        String insertRemotePath = null;
        String insertShowPath = null;

        InputStream uploadStream = originalFile.getInputStream();
        //视频文件，小于10MB，且不是htmlSupVideo则转码后存储
        String rootPath = ClassUtils.getDefaultClassLoader().getResource("").getPath();
        final File tmpFolder = new File(rootPath + "data/video");
        if (!tmpFolder.exists())
            tmpFolder.mkdirs();

        File srcFile = new File(tmpFolder.getAbsolutePath() + "/" + UUID.randomUUID().toString() + "." + fileItem.getPostfix());
        FileOutputStream fos = new FileOutputStream(srcFile);
        byte[] buf = new byte[1024];
        int length;
        while ((length = uploadStream.read(buf)) > 0) {
            fos.write(buf, 0, length);
        }
        fos.close();
        logger.debug("非htmlSupVideo视频文件存储成功");

        //非Mp4音乐转码
        File dstFile = new File(tmpFolder.getAbsolutePath() + "/" + UUID.randomUUID().toString() + ".mp4");
        boolean tranferSuccess = iMediaTranfer.tranferVideo(srcFile, dstFile);
        if (tranferSuccess)
            logger.debug("非htmlSupVideo音乐文件转码成功");
        else {
            logger.error("非htmlSupVideo音乐转码失败");
            fileItem = null;
            return;
        }

        if (cloudDiskConfig.getType().equals("OSS")) {
            UploadResult OSSsrcUploadResult = aliyunOssService.upload("cloudDisk/video",srcFile);
            UploadResult OSSdstUploadResult = aliyunOssService.upload("cloudDisk/video",dstFile );

            if ((OSSsrcUploadResult.getSuccess()==1) && (OSSdstUploadResult.getSuccess()==1)) {
                logger.debug("非htmlSupVideo音乐源文件和转码文件上传到OSS完毕");
                insertRemotePath = OSSsrcUploadResult.getUrl();
                insertShowPath = OSSdstUploadResult.getUrl();
            } else {
                logger.error("非htmlSupVideo音乐源文件或目标文件上传失败");
            }
        } else {
            String remoteFilePath = fileItem.getUserId() + "/" + fileItem.getParentFolderId() + "/" + UUID.randomUUID().toString() + "." + fileItem.getPostfix();
            String showFilePath = fileItem.getUserId() + "/" + fileItem.getParentFolderId() + "/" + UUID.randomUUID().toString() + ".";

            FileInputStream srcStream = new FileInputStream(srcFile);
            final boolean FTPsrcUploadresult = FtpUtil.uploadFile("/" + remoteFilePath, srcStream);
            srcStream.close();

            FileInputStream dstStream = new FileInputStream(dstFile);
            final boolean FTPdstUploadresult = FtpUtil.uploadFile("/" + showFilePath + "mp4", dstStream);
            dstStream.close();
            if (FTPsrcUploadresult && FTPdstUploadresult) {
                logger.debug("非htmlSupVideo音乐源文件和转码文件上传到FTP完毕");
                insertRemotePath = remoteFilePath;
                insertShowPath = showFilePath + "mp4";
            } else
                logger.error("非htmlSupVideo音乐源文件或目标文件上传失败");
        }

        if ((insertRemotePath == null) || (insertShowPath == null)) {
            logger.error("当前文件上传失败...");
            fileItem = null;
            return;
        }
        if (dstFile != null) {
            if (dstFile.exists())
                dstFile.delete();
        }

        if (srcFile != null) {
            if (srcFile.exists())
                srcFile.delete();
        }
        fileItem.setMyFilePath(insertRemotePath);
        fileItem.setShowPath(insertShowPath);

    }

    @Override
    public void uploadImgFile(MultipartFile originalFile, MyFile fileItem) throws Exception {
        String insertRemotePath = null;
        String insertShowPath = null;

        final UploadResult OSSimgUploadRes = aliyunOssService.upload(originalFile.getInputStream(), "cloudDisk/imgs",fileItem.getMyFileName());

        if (OSSimgUploadRes.getSuccess()==1) {
            logger.debug("图片文件上传到OSS完毕");
            insertRemotePath = OSSimgUploadRes.getUrl();
            insertShowPath = OSSimgUploadRes.getUrl();
        } else {
            logger.error("图片文件上传到OSS失败");
        }

        if ((insertRemotePath == null) || (insertShowPath == null)) {
            logger.error("当前文件上传失败...");
            fileItem = null;
            return;
        }

        fileItem.setMyFilePath(insertRemotePath);
        fileItem.setShowPath(insertShowPath);

    }

    @Override
    public void uploadAudioAndVideoFile(MultipartFile originalFile, MyFile fileItem) throws Exception {
        String insertRemotePath = null;
        String insertShowPath = null;

        //htmlSupAudio insertPostfix
        if (cloudDiskConfig.getType().equals("OSS")) {
            final UploadResult OSSfileUploadRes = aliyunOssService.upload(originalFile.getInputStream(), "cloudDisk/audio", fileItem.getMyFileName());

            if (OSSfileUploadRes.getSuccess()==1) {
                logger.debug("htmlSupAudio或者htmlSupVideo文件上传到OSS完毕");
                insertRemotePath = OSSfileUploadRes.getUrl();
                insertShowPath = OSSfileUploadRes.getUrl();
            } else {
                logger.error("htmlSupAudio或者htmlSupVideo文件上传到OSS失败");
            }
        } else {
            String remoteFilePath = fileItem.getUserId() + "/" + fileItem.getParentFolderId() + "/" + UUID.randomUUID().toString() + "." + fileItem.getPostfix();

            boolean FTPfileUploadRes = FtpUtil.uploadFile("/" + remoteFilePath, originalFile.getInputStream());

            if (FTPfileUploadRes) {
                logger.debug("htmlSupAudio或者htmlSupVideo文件上传到FTP完毕");
                insertRemotePath = remoteFilePath;
                insertShowPath = remoteFilePath;
            } else
                logger.error("htmlSupAudio或者htmlSupVideo文件上传到FTP失败");
        }

        if ((insertRemotePath == null) || (insertShowPath == null)) {
            logger.error("当前文件上传失败...");
            fileItem = null;
            return;
        }

        fileItem.setMyFilePath(insertRemotePath);
        fileItem.setShowPath(insertShowPath);
    }

    @Override
    public void uploadOtherFile(MultipartFile originalFile, MyFile fileItem) throws Exception {
        String insertRemotePath = null;
        String insertShowPath = null;

        String remoteFilePath = fileItem.getUserId() + "/" + fileItem.getParentFolderId() + "/" + UUID.randomUUID().toString() + "." + fileItem.getPostfix();
        //提交到FTP服务器
        boolean FTPfilesUploadResult = FtpUtil.uploadFile("/" + remoteFilePath, originalFile.getInputStream());
        if (FTPfilesUploadResult) {
            logger.debug("普通文件上传到FTP完毕");
            insertRemotePath = remoteFilePath;
            insertShowPath = remoteFilePath;
        } else {
            logger.error("普通文件上传到FTP失败");
        }


        if ((insertRemotePath == null) || (insertShowPath == null)) {
            logger.error("当前文件上传失败...");
            fileItem = null;
            return;
        }

        fileItem.setMyFilePath(insertRemotePath);
        fileItem.setShowPath(insertShowPath);
    }


    public void transeferFile(MyFile shareFile, MyFile fileItem) {
        String insertRemotePath = null;
        String insertShowPath = null;
        if (shareFile.getMyFilePath().startsWith("http")) {
            //提交到OSS服务器
            UploadResult OSStransferRes = aliyunOssService.transfer(shareFile.getMyFilePath().substring(aliyunConfig.getUrlPrefix().length()),
                    StringUtils.substringBeforeLast(shareFile.getMyFilePath().substring(aliyunConfig.getUrlPrefix().length()), "/"));
            if (OSStransferRes.getSuccess()==1) {
                logger.debug("OSS中remote文件转存完成");
                insertRemotePath = OSStransferRes.getUrl();
                insertShowPath = OSStransferRes.getUrl();
            } else {
                logger.error("OSS中remote文件转存失败");
            }

            if (!shareFile.getShowPath().equals(shareFile.getMyFilePath())) {

                OSStransferRes = aliyunOssService.transfer(shareFile.getShowPath().substring(aliyunConfig.getUrlPrefix().length()),
                        org.apache.commons.lang3.StringUtils.substringBeforeLast(shareFile.getShowPath().substring(aliyunConfig.getUrlPrefix().length()), "/"));
                if (OSStransferRes.getSuccess()==1) {
                    logger.debug("OSS中show文件转存完成");
                    insertShowPath = OSStransferRes.getUrl();
                } else {
                    logger.error("OSS中show文件转存失败");
                }
            }

        } else {
            String remoteFilePath = shareFile.getUserId() + "/" + fileItem.getUploadTime() + "/" + shareFile.getParentFolderId() + "/" + UUID.randomUUID().toString() + "." + shareFile.getPostfix();
            String showFilePath = shareFile.getUserId() + "/" + fileItem.getUploadTime() + "/" + shareFile.getParentFolderId() + "/" + UUID.randomUUID().toString() + "." + StringUtils.substringAfterLast(shareFile.getShowPath(), ".");

            try {
                //提交到FTP服务器
                FtpUtil.transferFile("/" + shareFile.getMyFilePath(), "/" + remoteFilePath);
                logger.debug("FTP中remote文件转存完成");
                insertRemotePath = remoteFilePath;
                insertShowPath = remoteFilePath;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("FTP中remote文件转存失败");
            }

            if (!shareFile.getShowPath().equals(shareFile.getMyFilePath())) {
                try {
                    FtpUtil.transferFile("/" + shareFile.getShowPath(), "/" + showFilePath);
                    logger.error("FTP中showfile转存成功");
                    insertShowPath = showFilePath;
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("FTP中showfile转存失败");
                }

            }

        }
        if ((insertRemotePath == null) || (insertShowPath == null)) {
            logger.error("当前文件上传失败...");
            fileItem = null;
            return;
        }
        fileItem.setMyFilePath(insertRemotePath);
        fileItem.setShowPath(insertShowPath);
    }
}