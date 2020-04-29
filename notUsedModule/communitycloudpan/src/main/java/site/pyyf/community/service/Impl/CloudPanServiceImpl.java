package site.pyyf.community.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import site.pyyf.community.config.FastdfsConfig;
import site.pyyf.community.dao.FastdfsMapper;
import site.pyyf.community.entity.File;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import site.pyyf.community.util.MultipartSerializable;

import java.io.ByteArrayInputStream;
import java.util.*;

@Component
@Service
@EnableConfigurationProperties(FastdfsConfig.class)
public class CloudPanServiceImpl implements ICloudPanService {
    private Log log = LogFactory.getLog(CloudPanServiceImpl.class);

    @Autowired
    private FastFileStorageClient storageClient;

    @Autowired
    private FastdfsConfig fastdfsConfig;

    @Autowired
    private FastdfsMapper fastdfsMapper;


    public String uploadFile(String json) {

        try {
            // 3、上传到FastDFS
            // 3.1、获取扩展名
            String extension = StringUtils.substringAfterLast(MultipartSerializable.json2file(json,"fileName").toString() ,".");
            // 3.2、上传
            StorePath storePath = storageClient.uploadFile(new ByteArrayInputStream((byte[]) MultipartSerializable.json2file(json,"fileByte")), ((byte[]) MultipartSerializable.json2file(json,"fileByte")).length, extension, null);
            System.out.println("长度为"+((byte[]) MultipartSerializable.json2file(json,"fileByte")).length);
            // 返回路径
            return fastdfsConfig.getBaseUrl() + storePath.getFullPath();
        } catch (Exception e) {
            log.error("【文件上传】上传文件失败！....{}", e);
            throw new RuntimeException("【文件上传】上传文件失败！" + e.getMessage());
        }
    }

    public void insertFileSystem(String absoluteName, String fastdfsName) {
        fastdfsMapper.insertFile(absoluteName, fastdfsName, 1);
    }

    public int fileSize(String absolutePath) {
        List<File> files = fastdfsMapper.selectFiles();
        Set<String> paths = new HashSet<>();
        List<File> resultsFiles = new ArrayList<>();


        if (absolutePath.equals("index")) {
            for (File file : files) {
                String head = file.getAbsolutePath().split("/")[0];
                if (!paths.contains(head)) {
                    paths.add(head);
                    file.setAbsolutePath(head);
                    resultsFiles.add(file);
                }
            }

        } else {
            for (File file : files) {
                if (file.getAbsolutePath().startsWith(absolutePath + "/")) {
                    String remainingPath = file.getAbsolutePath().substring(absolutePath.length() + 1);

                    String head = remainingPath.split("/")[0];
                    if (!paths.contains(head)) {
                        paths.add(head);
                        file.setAbsolutePath(head);
                        resultsFiles.add(file);
                    }
                }


            }
        }

        return resultsFiles.size();
    }

    public List<File> list(int current, int limit, String absolutePath) {
        List<File> files = fastdfsMapper.selectFiles();
        Set<String> paths = new HashSet<>();
        List<File> resultsFiles = new ArrayList<>();


        if (absolutePath.equals("index")) {

            for (int i = 0; i < files.size(); i++) {
                if (resultsFiles.size() / limit == current - 1) {
                    String head = files.get(i).getAbsolutePath().split("/")[0];
                    if (!paths.contains(head)) {
                        paths.add(head);
                        files.get(i).setAbsolutePath(head);
                        resultsFiles.add(files.get(i));
                    }
                }

            }


        } else {
            for (int i = 0; i < files.size(); i++) {
                if (resultsFiles.size() / limit == current - 1) {
                    if (files.get(i).getAbsolutePath().startsWith(absolutePath + "/")) {
                        String remainingPath = files.get(i).getAbsolutePath().substring(absolutePath.length() + 1);

                        String head = remainingPath.split("/")[0];
                        if (!paths.contains(head)) {
                            paths.add(head);
                            files.get(i).setAbsolutePath(head);
                            resultsFiles.add(files.get(i));
                        }
                    }
                }

            }
        }
        resultsFiles.sort((o1,o2)->{
            int i = o1.getAbsolutePath().substring(o1.getAbsolutePath().lastIndexOf("/") + 1).lastIndexOf("."); //4
            int j = o2.getAbsolutePath().substring(o1.getAbsolutePath().lastIndexOf("/") + 1).lastIndexOf("."); // -1
            return i-j;
        });

        return resultsFiles;

    }

    public int deleteFileSystem(String absolute_path) {
        return fastdfsMapper.updateFilesValid(absolute_path);
    }

    public int modifyFileSystem(String absolute_path, String modified_path) {
        List<File> files = fastdfsMapper.selectByFilesName(absolute_path);
        final int result = fastdfsMapper.updateFilesValid(absolute_path);
        for(File file:files){
            file.setAbsolutePath(file.getAbsolutePath().replace(absolute_path,modified_path));
            fastdfsMapper.insertFile(file.getAbsolutePath(),file.getFastdfsName(),1);
        }
        return result;
    }


    public int cleanFileSystem(String fastdfsName){
        if(fastdfsName.startsWith(fastdfsConfig.getBaseUrl()))
            storageClient.deleteFile(fastdfsName.replace(fastdfsConfig.getBaseUrl(),""));
        return 0;
    }

    public List<File> selectDeletedFiles() {
        final List<File> files = fastdfsMapper.selectDeletedFiles();
        fastdfsMapper.deletedRows();
        return files;

    }
}
