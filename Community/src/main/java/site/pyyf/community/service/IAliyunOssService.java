package site.pyyf.community.service;

import site.pyyf.community.entity.UploadResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.List;

public interface IAliyunOssService {
    UploadResult upload(String suffix,File file) ;

    UploadResult upload(InputStream inputStream, String suffix, String fileName) ;

    UploadResult upload(String suffix,MultipartFile uploadFile) ;

    void download(String absolutePath, OutputStream out) ;

    UploadResult transfer(String srcPath, String dstSuffix) ;

    boolean delete(String srcPath);

    void insertDatabase(String fileName, String url);

    List<String> queryAll(String suffix);
}
