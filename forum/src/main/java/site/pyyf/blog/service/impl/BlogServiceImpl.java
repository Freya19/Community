package site.pyyf.blog.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import site.pyyf.blog.dao.IBlogMapper;
import site.pyyf.blog.service.IBlogService;
import site.pyyf.blog.entity.Blog;
import site.pyyf.commons.utils.CommunityConstant;
import site.pyyf.forum.entity.Category;
import site.pyyf.forum.entity.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author "Gepeng18"
 * @Description (Blog)表服务实现类
 * @since 2020-03-25 20:56:51
 */
@Service
public class BlogServiceImpl extends BaseService implements IBlogService, CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(BlogServiceImpl.class);

    //博客缓存
    private Cache<String, Object> blogCache;

    //caffeine.blogs.max-size=15
    //caffeine.blogs.expire-seconds=180
    @Value("${caffeine.blogs.max-size}")
    private int maxSize;

    @Value("${caffeine.blogs.expire-seconds}")
    private int expireSeconds;

    @PostConstruct
    public void init() {
        // 初始化博客列表缓存
        blogCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build();
    }

    @Autowired
    public IBlogMapper iBlogMapper;

    /**
     * @param id 主键
     * @return 实例对象
     * @Description 通过ID查询单条数据
     * @author "Gepeng18"
     * @date 2020-03-25 20:56:51
     */
    @Override
    public Blog queryById(Integer id) {
        //这里引入caffeine中先查询有无缓存，无再从数据库中查找
        String caffeineKey = "blog:" + id;
        //根据key从caffeine中查找缓存，如果没有返回null
        Blog cacheBlog = (Blog) blogCache.getIfPresent(caffeineKey);
        if (cacheBlog != null) {
            logger.info("查询blog时,caffeine缓存击中");
            return cacheBlog;
        }
        logger.info("查询blog时，caffeine缓存未击中，将从数据库读取");
        cacheBlog = iBlogMapper.queryById(id);
        blogCache.put(caffeineKey, cacheBlog);
        return cacheBlog;
//        return iBlogMapper.queryById(id);
    }

    /**
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     * @Description 查询多条数据
     * @author "Gepeng18"
     * @date 2020-03-25 20:56:51
     */
    @Override
    public List<Blog> queryAllByLimit(int offset, int limit) {
        return iBlogMapper.queryAllByLimit(Blog.builder().build(),offset, limit);

    }

    /**
     * @return 对象列表
     * @Description 查询全部数据
     * @author "Gepeng18"
     * @date 2020-03-25 20:56:51
     */
    @Override
    public List<Blog> queryAll() {
        return iBlogMapper.queryAll();
    }

    /**
     * @param blog 实例对象
     * @return 对象列表
     * @Description 实体作为筛选条件查询数据
     * @author "Gepeng18"
     * @date 2020-03-25 20:56:51
     */
    @Override
    public List<Blog> queryAll(Blog blog) {
        return iBlogMapper.queryAll(blog);
    }

    /**
     * @return 数量
     * @Description 查询实体数量
     * @author "Gepeng18"
     * @date 2020-03-25 20:56:51
     */
    @Override
    public int queryCount() {
        return iBlogMapper.queryCount();
    }

    /**
     * @param blog 实例对象
     * @return 数量
     * @Description 实体作为筛选条件查询数量
     * @author "Gepeng18"
     * @date 2020-03-25 20:56:51
     */
    @Override
    public int queryCount(Blog blog) {
        return iBlogMapper.queryCount(blog);
    }

    /**
     * @param blog   实例对象
     * @param offset 查询起始位置
     * @param limit  查询条数
     * @return 对象列表
     * @Description 实体作为筛选条件查询指定行
     * @author "Gepeng18"
     * @date 2020-03-25 20:56:51
     */
    @Override
    public List<Blog> queryAllByLimit(Blog blog, int offset, int limit) {
        return iBlogMapper.queryAllByLimit(blog, offset, limit);
    }

    /**
     * @param blog 实例对象
     * @return 实例对象
     * @Description 新增数据
     * @author "Gepeng18"
     * @date 2020-03-25 20:56:51
     */
    @Override
    public Blog insert(Blog blog) {
        //更新的类别对应的博客数量加一
        Category nowCategory = iCategoryService.queryById(blog.getCategoryId());
        nowCategory.setCount(nowCategory.getCount() + 1);
        iCategoryService.update(nowCategory);

        //更新的标签对应的博客数量加一
        if (blog.getTags() != null) {
            String[] tagNames = blog.getTags().split(",");
            for (String tagName : tagNames) {
                List<Tag> tags = iTagService.queryAll(Tag.builder().name(tagName).build());
                if (tags.size() == 1) {
                    Tag tag = tags.get(0);
                    tag.setCount(tag.getCount() + 1);
                    iTagService.update(tag);
                } else if (tags.size() == 0) {
                    iTagService.insert(Tag.builder().name(tagName).entityType(ENTITY_TYPE_BLOG).count(1).build());
                }
            }
        }

        //先插入，否则没有id
        this.iBlogMapper.insert(blog);
        return blog;
    }

    /**
     * @param blog 实例对象
     * @return 实例对象
     * @Description 修改数据
     * @author "Gepeng18"
     * @date 2020-03-25 20:56:51
     */
    @Override
    public Blog update(Blog blog) {
        String caffeineKey = "blog:"+blog.getId(); //queryAll(Blog blog)的时候，key也是这个
        Blog previousBlogCache = (Blog) blogCache.getIfPresent(caffeineKey);
        if(previousBlogCache!=null){
            logger.info("更改博客时，将其缓存删除");
            blogCache.invalidate(caffeineKey);
        }

        //原始类别对应的博客数量减一
        Category oriCategory = iCategoryService.queryById(iBlogService.queryById(blog.getId()).getCategoryId());
        oriCategory.setCount(oriCategory.getCount() - 1);
        iCategoryService.update(oriCategory);

        //原始标签对应的博客数量减一
        if (blog.getTags() != null) {
            String[] tagNames = blog.getTags().split(",");
            for (String tagName : tagNames) {
                List<Tag> tags = iTagService.queryAll(Tag.builder().name(tagName).build());
                if (tags.size() == 1) {
                    Tag tag = tags.get(0);
                    tag.setCount(tag.getCount() - 1);
                    iTagService.update(tag);
                } else {
                    //搜出其他数表明有问题，这里不做处理
                }
            }
        }

        //更新的类别对应的博客数量加一
        Category nowCategory = iCategoryService.queryById(blog.getCategoryId());
        nowCategory.setCount(nowCategory.getCount() + 1);
        iCategoryService.update(nowCategory);

        //更新的类别对应的博客数量加一
        if (blog.getTags() != null) {
            String[] tagNames = blog.getTags().split(",");
            for (String tagName : tagNames) {
                List<Tag> tags = iTagService.queryAll(Tag.builder().name(tagName).build());
                if (tags.size() == 1) {
                    Tag tag = tags.get(0);
                    tag.setCount(tag.getCount() + 1);
                    iTagService.update(tag);
                } else if (tags.size() == 0) {
                    iTagService.insert(Tag.builder().name(tagName).entityType(ENTITY_TYPE_BLOG).count(1).build());
                }
            }
        }

        this.iBlogMapper.update(blog);
        return queryById(blog.getId());
    }

    /**
     * @param id 主键
     * @return 是否成功
     * @Description 通过主键删除数据
     * @author "Gepeng18"
     * @date 2020-03-25 20:56:51
     */
    @Override
    public boolean deleteById(Integer id) {

        String caffeineKey = "blog:"+id;
        Blog previousBlogCache = (Blog) blogCache.getIfPresent(caffeineKey);
        if(previousBlogCache!=null){
            logger.info("删除博客时，将其缓存删除");
            blogCache.invalidate(caffeineKey);
        }

        //原始类别对应的博客数量减一
        Category oriCategory = iCategoryService.queryById(iBlogService.queryById(id).getCategoryId());
        oriCategory.setCount(oriCategory.getCount() - 1);
        iCategoryService.update(oriCategory);

        Blog blog = queryById(id);
        //原始标签对应的博客数量减一
        if (blog.getTags() != null) {
            String[] tagNames = blog.getTags().split(",");
            for (String tagName : tagNames) {
                List<Tag> tags = iTagService.queryAll(Tag.builder().name(tagName).build());
                if (tags.size() == 1) {
                    Tag tag = tags.get(0);
                    tag.setCount(tag.getCount() - 1);
                    iTagService.update(tag);
                } else {
                    //搜出其他数表明有问题，这里不做处理
                }
            }
        }
        return iBlogMapper.deleteById(id) > 0;
    }

    @Override
    public List<String> queryGroupByMonth() {
        return iBlogMapper.queryGroupByMonth();
    }

    @Override
    public List<Blog> queryByMonth(String month) {
        return iBlogMapper.queryByMonth(month);
    }


}