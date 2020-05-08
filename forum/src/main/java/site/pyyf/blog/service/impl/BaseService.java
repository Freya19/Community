package site.pyyf.blog.service.impl;

import site.pyyf.blog.dao.IBlogMapper;
import site.pyyf.blog.dao.IBlog_TagMapper;
import site.pyyf.blog.service.IBlogService;
import site.pyyf.blog.service.IBlog_TagService;
import site.pyyf.blog.service.ITagService;
import site.pyyf.forum.dao.ITagMapper;
import site.pyyf.forum.service.ICategoryService;
import site.pyyf.forum.service.ICodePreviewService;
import org.springframework.beans.factory.annotation.Autowired;

public class BaseService {

    @Autowired
    protected IBlog_TagMapper iBlog_tagMapper;

    @Autowired
    protected IBlogMapper iBlogMapper;

    @Autowired
    protected ITagMapper iTagMapper;

    @Autowired
    protected ICodePreviewService iCodeService;

    @Autowired
    protected IBlog_TagService iBlog_tagService;

    @Autowired
    protected IBlogService iBlogService;

    @Autowired
    protected ICategoryService iCategoryService;

    @Autowired
    protected ITagService iTagService;

}
