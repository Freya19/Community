package site.pyyf.cloudDisk.service.impl;

import site.pyyf.cloudDisk.config.CloudDiskConfig;
import site.pyyf.cloudDisk.mapper.IEbookContentMapper;
import site.pyyf.cloudDisk.mapper.IEbookMapper;
import site.pyyf.cloudDisk.mapper.IFileFolderMapper;
import site.pyyf.cloudDisk.mapper.IMyFileMapper;
import site.pyyf.cloudDisk.service.IEbookContentService;
import site.pyyf.cloudDisk.service.IMediaTranfer;
import site.pyyf.forum.config.AliyunConfig;
import site.pyyf.forum.dao.IUserMapper;
import site.pyyf.forum.service.impl.AliyunOssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @ClassName: BaseService
 * @author: xw
 * @date 2020/2/25 17:19
 * @Version: 1.0
 **/
public class CloudDiskBaseController {
    @Autowired
    protected AliyunConfig aliyunConfig;
    @Autowired
    protected CloudDiskConfig cloudDiskConfig;

    @Autowired
    protected IMediaTranfer iMediaTranfer;
    @Autowired
    protected AliyunOssService aliyunOssService;
    @Autowired
    protected IEbookContentService iEbookContentService;
    @Autowired
    protected RedisTemplate redisTemplate;

    @Autowired
    protected IUserMapper userMapper;

    @Autowired
    protected IMyFileMapper myFileMapper;
    @Autowired
    protected IFileFolderMapper fileFolderMapper;

    @Autowired
    protected IEbookContentMapper iebookContentMapper;

    @Autowired
    protected IEbookMapper ilibraryMapper;
}
