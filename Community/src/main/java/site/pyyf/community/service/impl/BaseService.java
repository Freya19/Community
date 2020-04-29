package site.pyyf.community.service.impl;

import site.pyyf.commons.event.EventProducer;
import site.pyyf.commons.utils.MailClient;
import site.pyyf.commons.utils.SensitiveFilter;
import site.pyyf.community.dao.*;
import site.pyyf.community.dao.*;
import site.pyyf.community.dao.elasticsearch.DiscussPostRepository;
import site.pyyf.community.service.IAliyunOssService;
import site.pyyf.community.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.thymeleaf.TemplateEngine;

import javax.annotation.Resource;

public class BaseService {

    @Autowired
    protected ISiteSettingMapper iSiteSettingMapper;

    @Autowired
    protected OSSStorageMapper ossStorageMapper;

    @Autowired
    protected ICategoryMapper iCategoryMapper;

    @Autowired
    protected SensitiveFilter sensitiveFilter;

    @Autowired
    protected DiscussPostService discussPostService;

    @Autowired
    protected EventProducer eventProducer;

    @Resource
    protected ICommentMapper iCommentMapper;

    @Autowired
    protected IDiscussPostMapper iDiscussPostMapper;


    @Autowired
    protected RedisTemplate redisTemplate;


    @Autowired
    protected DiscussPostRepository discussRepository;

    @Autowired
    protected ElasticsearchTemplate elasticTemplate;

    @Autowired
    protected IUserService iUserService;

    @Resource
    protected IUserMapper iUserMapper;

    @Autowired
    protected MailClient mailClient;

    @Autowired
    protected TemplateEngine templateEngine;

    @Autowired
    protected IMessageMapper iMessageMapper;

    @Autowired
    protected IAliyunOssService iAliyunOssService;
}
