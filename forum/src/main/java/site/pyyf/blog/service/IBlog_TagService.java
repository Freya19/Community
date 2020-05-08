package site.pyyf.blog.service;

import java.util.List;

/**
 * @Description (Tag)表服务接口
 *
 * @author "Gepeng18"
 * @since 2020-03-23 21:31:52
 */
public interface IBlog_TagService {

    int queryBlogCountByTagId(Integer id);

    List<Integer> queryBlogIdByTagIdByLimit(Integer tagId, int offset, int limit);


    List<Integer> queryTagIdByBlogId(Integer blogId);

    /**
     * Created by "gepeng" on 2020-03-84 04:57:47.
     * @Description  像blog_tag表中插入数据
     * @param [blogId, tagId]
     * @return void
     */
    void insertBlogAndTagPair(Integer blogId, Integer tagId);

    void deleteByBlogId(Integer blogId);

}