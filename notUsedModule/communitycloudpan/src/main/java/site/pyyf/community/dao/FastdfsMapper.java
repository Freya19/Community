package site.pyyf.community.dao;


import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;
import site.pyyf.community.entity.File;

import java.util.List;

@Mapper
@Component
public interface FastdfsMapper {
    List<File> selectFiles();

    int insertFile(String absolute_path, String fastdfs_name,int valid);

    int updateFilesValid(String absolute_path);

    List<File> selectByFilesName(String absolute_path);

    List<File> selectDeletedFiles();

    int deletedRows();
}


