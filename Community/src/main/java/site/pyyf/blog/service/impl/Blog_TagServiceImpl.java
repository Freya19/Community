package site.pyyf.blog.service.impl;

import site.pyyf.blog.service.IBlog_TagService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author "Gepeng18"
 * @Description (Tag)表服务实现类
 * @since 2020-03-23 21:31:53
 */
@Service
public class Blog_TagServiceImpl extends BaseService implements IBlog_TagService {

    @Override
    public int queryBlogCountByTagId(Integer tagId) {
        return iBlog_tagMapper.queryBlogCountByTagId(tagId);
    }

    @Override
    public List<Integer> queryBlogIdByTagIdByLimit(Integer tagId, int offset, int limit) {
        return iBlog_tagMapper.queryBlogIdByTagIdByLimit(tagId, offset, limit);
    }

    @Override
    public List<Integer> queryTagIdByBlogId(Integer blogId) {
        return iBlog_tagMapper.queryTagIdByBlogId(blogId);
    }

    @Override
    public void insertBlogAndTagPair(Integer blogId, Integer tagId) {
        iBlog_tagMapper.insertBlogAndTagPair(blogId, tagId);
    }

    @Override
    public void deleteByBlogId(Integer blogId) {
        iBlog_tagMapper.deleteByBlogId(blogId);
    }

}