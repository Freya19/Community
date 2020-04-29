package site.pyyf.community.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.*;
import site.pyyf.community.config.AliyunConfig;
import site.pyyf.community.entity.UploadResult;
import site.pyyf.community.service.IAliyunOssService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class AliyunOssService extends BaseService implements IAliyunOssService {
    private static final Logger logger = LoggerFactory.getLogger(AliyunOssService.class);
    @Autowired
    private OSS ossClient;

    @Autowired
    private AliyunConfig aliyunConfig;

    // 允许上传的格式
    private static String[] SUP_TYPE = new String[]{
            "md", "java", "css", "cpp", "py", "php", "html",
            "bmp", "jpg", "jpeg", "gif", "png",
            "mp4", "wmv", "flv",
            "mp3", "wma","flac"};

    private String getFilePath( String suffix,String fileName) {
        final String dateSuffix = new SimpleDateFormat("yyyy/MM/dd/HH/mm/ss").format(new Date());
        return suffix +"/"+ dateSuffix + "/" + fileName ;

    }

    public UploadResult upload( String suffix,File file) {
        String fileName = file.getName();
        // 校验文件格式
        boolean isLegal = false;
        for (String type : SUP_TYPE) {
            if (StringUtils.substringAfterLast(fileName, ".").equals(type)) {
                isLegal = true;
                break;
            }
        }
        // 封装Result对象，并且将文件的byte数组放置到result对象中
        UploadResult fileUploadResult = new UploadResult();
        if (!isLegal) {
            fileUploadResult.setMessage("上传OSS时后缀名不匹配");
            fileUploadResult.setSuccess(0);
            logger.error("上传OSS时后缀名不匹配");
            return fileUploadResult;
        }

        String remotePath = getFilePath(suffix, fileName);
        // 上传到阿里云
        try {
            ossClient.putObject(aliyunConfig.getBucketName(), remotePath, new FileInputStream(file));
            logger.info("上传OSS成功");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("上传OSS失败");

            //上传失败
            fileUploadResult.setMessage("服务器发生了错误");
            fileUploadResult.setSuccess(0);
            return fileUploadResult;
        }
        fileUploadResult.setSuccess(1);
        fileUploadResult.setMessage("上传成功");
        fileUploadResult.setUrl(this.aliyunConfig.getUrlPrefix() + remotePath);
        return fileUploadResult;
    }

    public UploadResult upload(InputStream inputStream,  String suffix,String fileName) {
        // 校验文件格式
        boolean isLegal = false;
        for (String type : SUP_TYPE) {
            if (StringUtils.substringAfterLast(fileName, ".").equals(type)) {
                isLegal = true;
                break;
            }
        }
        // 封装Result对象，并且将文件的byte数组放置到result对象中
        UploadResult fileUploadResult = new UploadResult();
        if (!isLegal) {
            logger.error("上传OSS时后缀名不匹配");
            fileUploadResult.setMessage("上传OSS时后缀名不匹配");
            fileUploadResult.setSuccess(0);
            return fileUploadResult;
        }

        String remotePath = getFilePath(suffix, fileName);
        // 上传到阿里云
        try {
            ossClient.putObject(aliyunConfig.getBucketName(), remotePath, inputStream);
            logger.info("上传OSS成功");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("上传OSS失败");
            //上传失败
            fileUploadResult.setMessage("服务器发生了错误");
            fileUploadResult.setSuccess(0);
            return fileUploadResult;
        }
        fileUploadResult.setSuccess(1);
        fileUploadResult.setMessage("上传成功");
        fileUploadResult.setUrl(this.aliyunConfig.getUrlPrefix() + remotePath);
        return fileUploadResult;
    }

    public UploadResult upload(String suffix,MultipartFile uploadFile) {
        String fileName = uploadFile.getOriginalFilename();
        // 校验文件格式
        boolean isLegal = false;
        for (String type : SUP_TYPE) {
            if (StringUtils.substringAfterLast(fileName, ".").equals(type)) {

                isLegal = true;
                break;
            }
        }
        // 封装Result对象，并且将文件的byte数组放置到result对象中
        UploadResult fileUploadResult = new UploadResult();
        if (!isLegal) {
            logger.error("上传OSS时后缀名不匹配");
            fileUploadResult.setMessage("上传OSS时后缀名不匹配");
            logger.error("上传OSS时后缀名不匹配");
            return fileUploadResult;
        }
        // 文件新路径
        String remotePath = getFilePath(suffix,fileName);
        // 上传到阿里云
        try {
            ossClient.putObject(aliyunConfig.getBucketName(), remotePath, new
                    ByteArrayInputStream(uploadFile.getBytes()));
        } catch (Exception e) {
            e.printStackTrace();
            //上传失败
            fileUploadResult.setMessage("服务器发生了错误");
            fileUploadResult.setSuccess(0);
            return fileUploadResult;
        }
        fileUploadResult.setSuccess(1);
        fileUploadResult.setMessage("上传成功");
        fileUploadResult.setUrl(this.aliyunConfig.getUrlPrefix() + remotePath);
        return fileUploadResult;
    }

    public void download(String absolutePath, OutputStream out) {
        final String bucketName = aliyunConfig.getBucketName();
        final OSS oss = aliyunConfig.oSSClient();
        // ossObject包含文件所在的存储空间名称、文件名称、文件元信息以及一个输入流。
        OSSObject ossObject = oss.getObject(bucketName, absolutePath);
        BufferedInputStream in = null;
        try {
            // 读取文件内容。
            in = new BufferedInputStream(ossObject.getObjectContent());
            byte[] buffer = new byte[1024];
            int len = 0;
            int i = 0;
            while ((len = in.read(buffer)) > 0) {
                i = i + len;
                out.write(buffer, 0, len);
            }
            logger.info("下载OSS成功");
            // 数据读取完成后，获取的流必须关闭，否则会造成连接泄漏，导致请求无连接可用，程序无法正常工作。
            in.close();
        } catch (Exception e) {
            logger.info("下载OSS失败");
            e.printStackTrace();
        }

    }

    public UploadResult transfer(String srcPath, String dstSuffix) {
        if (!new File("data/temp").exists())
            new File("data/temp").mkdirs();
        File tmpFile = new File("data/temp/" + UUID.randomUUID().toString() + "." + StringUtils.substringAfterLast(srcPath, "."));

        try {
            download(srcPath, new FileOutputStream(tmpFile));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("OSS转存过程中下载失败");
            return new UploadResult(0,"OSS转存过程中下载失败", "");
        }
        return upload( dstSuffix,tmpFile);


    }


    public boolean delete(String srcPath) {
        String bucketName = aliyunConfig.getBucketName();
        String fileName = StringUtils.substringAfterLast(srcPath, "/");

        if (bucketName == null || fileName == null) {
            logger.error("OSS所删除的文件不存在！");
            return false;
        }
        try {
            GenericRequest request = new DeleteObjectsRequest(bucketName).withKey(srcPath); //srcPath = "cloudDisk/Audio/test.html"
            ossClient.deleteObject(request);
        } catch (Exception oe) {
            oe.printStackTrace();
            return false;
        }
        return true;
    }

    public List<String> queryAll(String suffix) {
        // 构造ListObjectsRequest请求
        ListObjectsRequest listObjectsRequest = new ListObjectsRequest(aliyunConfig.getBucketName());

        // 设置prefix参数来获取fun目录下的所有文件。
        listObjectsRequest.setPrefix(suffix + "/");
        // 列出文件。
        ObjectListing listing = ossClient.listObjects(listObjectsRequest);
        // 遍历所有文件。
        List<String> list = new ArrayList<>();
        for (int i = 0; i < listing.getObjectSummaries().size(); i++) {
            if (i == 0) {
                continue;
            }
            String fileName = listing.getObjectSummaries().get(i).getKey();
            list.add(fileName);
        }

        return list;
    }


    public void insertDatabase(String fileName, String url){
        ossStorageMapper.insertData(fileName,url);
    }


}
