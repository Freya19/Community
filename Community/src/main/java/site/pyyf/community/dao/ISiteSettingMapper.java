package site.pyyf.community.dao;


import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ISiteSettingMapper {
    int getSiteSetting(String description);

}
