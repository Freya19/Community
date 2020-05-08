package site.pyyf.blog.dao;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IBlog_TagMapper {
    int queryBlogCountByTagId(Integer tagId);

    List<Integer> queryBlogIdByTagIdByLimit(Integer tagId, int offset, int limit);

    List<Integer> queryTagIdByBlogId(Integer blogId);

    void insertBlogAndTagPair(Integer blogId, Integer tagId);

    int queryAll(Integer blogId, Integer tagId);

    void deleteByBlogId(Integer blogId);


}
