package site.pyyf.community.dao;


import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface OSSStorageMapper {


    void insertData(String fileName, String url);
}
