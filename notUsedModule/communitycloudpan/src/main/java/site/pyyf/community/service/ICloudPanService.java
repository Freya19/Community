package site.pyyf.community.service;


import lombok.Value;
import org.springframework.web.multipart.MultipartFile;
import site.pyyf.community.entity.File;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public interface ICloudPanService {
	public String uploadFile(String file) ;

	public void insertFileSystem(String absoluteName, String fastdfsName) ;

	public int fileSize(String absolutePath) ;

	public List<site.pyyf.community.entity.File> list(int current, int limit, String absolutePath) ;

	public int deleteFileSystem(String absolute_path) ;

	public int modifyFileSystem(String absolute_path, String modified_path) ;


	public int cleanFileSystem(String fastdfsName);

	public List<site.pyyf.community.entity.File> selectDeletedFiles() ;





}





