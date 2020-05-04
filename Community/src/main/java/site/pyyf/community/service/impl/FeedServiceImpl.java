package site.pyyf.community.service.impl;

import site.pyyf.commons.utils.CommunityConstant;
import site.pyyf.commons.utils.RedisKeyUtil;
import site.pyyf.community.controller.CommunityBaseController;
import site.pyyf.community.dao.IFeedMapper;
import site.pyyf.community.entity.Feed;
import site.pyyf.community.entity.User;
import site.pyyf.community.service.IFeedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by Gepeng18 on 2020/2/1.
 */
@Service
public class FeedServiceImpl extends CommunityBaseController implements IFeedService, CommunityConstant {
    @Autowired
    IFeedMapper iFeedMapper;

    public List<Feed> getUserFeeds(List<Integer> userIds, int entityType, int count) {
        return iFeedMapper.selectUserFeeds(userIds, entityType, count);
    }

    public boolean addFeed(Feed feed) {
        iFeedMapper.addFeed(feed);
        return feed.getId() > 0;
    }

    public Feed getById(int id) {
        return iFeedMapper.getFeedById(id);
    }


    public List<Feed> getFeeds() {
        List<Feed> feeds = null;
        if ((int) ((new Date().getTime() - hostHolder.getUser().getLoginTime().getTime()) / (1000 * 3600 * 24)) < 5) {
            String timelinePersistenceKey = RedisKeyUtil.getPersistenceTimelineKey(hostHolder.getUser().getId());
            // entityId userName
            feeds = redisTemplate.opsForList().range(timelinePersistenceKey, 0, -1);
        } else {

            final List<Integer> followerIds = iFollowService.findFollowers(hostHolder.getUser().getId(), 0, Integer.MAX_VALUE).stream().map(m -> {
                return ((User) m.get("user")).getId();
            }).collect(Collectors.toList());

            feeds = getUserFeeds(followerIds, ENTITY_TYPE_POST, FEEDTIMELINECOUNT);
        }
        return feeds;
    }

    public String getFeedContentByPostId(Integer postId, Set<Feed> published, Set<Feed> liked,Set<Feed> commented){
        StringBuilder feedContent = new StringBuilder();
        boolean publishFlag = false;
        boolean likeFlag = false;
        boolean commentFlag = false;
        //这里需要改进
        //向feedContent 加入 Gepeng18 发布此贴 ， 如果搜不到 则"发布此贴"也不加
        for (Feed feed : published) {
            if (feed.getEntityId() == postId) {
                publishFlag = true;
                feedContent.append(feed.getUserName()).append("、");
            }
        }
        if (publishFlag)
            feedContent.append("发布此贴,");


        //向feedContent 加入 Gepeng18 觉得很赞 ， 如果搜不到 则"觉得很赞"也不加
        for (Feed feed : liked) {
            if (feed.getEntityId() == postId) {
                likeFlag = true;
                feedContent.append(feed.getUserName()).append("、");
            }
        }
        if (likeFlag)
            feedContent.append("觉得很赞,");

        //向feedContent 加入 Gepeng18 评论了此贴 ， 如果搜不到 则"评论了此贴"也不加
        for (Feed feed : commented) {
            if (feed.getEntityId() == postId) {
                commentFlag = true;
                feedContent.append(feed.getUserName()).append("、");
            }
        }
        if (commentFlag)
            feedContent.append("评论了此贴,");
        String feed = feedContent.toString();
        //去掉最后的逗号
        if(feed.length()>0)
            feed = feed.substring(0, feed.length()-1);
        return  feed;
    }

    /**
     * @Description 通过ID查询单条数据
     * @author "Freya19"
     * @date 2020-04-19 20:19:22
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public Feed queryById(Integer id) {
        return iFeedMapper.queryById(id);
    }

    /**
     * @Description 查询多条数据
     * @author "Freya19"
     * @date 2020-04-19 20:19:22
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    @Override
    public List<Feed> queryAllByLimit(int offset, int limit) {
        return iFeedMapper.queryAllByLimit(offset, limit);
    }

    /**
     * @Description 查询全部数据
     * @author "Freya19"
     * @date 2020-04-19 20:19:22
     * @return 对象列表
     */
    @Override
    public List<Feed> queryAll() {
        return iFeedMapper.queryAll();
    }

    /**
     * @Description 实体作为筛选条件查询数据
     * @author "Freya19"
     * @date 2020-04-19 20:19:22
     * @param feed 实例对象
     * @return 对象列表
     */
    @Override
    public List<Feed> queryAll(Feed feed) {
        return iFeedMapper.queryAll(feed);
    }

    /**
     * @Description 查询实体数量
     * @author "Freya19"
     * @date 2020-04-19 20:19:22
     * @return 数量
     */
    @Override
    public int queryCount() {
        return iFeedMapper.queryCount();
    }

    /**
     * @Description 实体作为筛选条件查询数量
     * @author "Freya19"
     * @date 2020-04-19 20:19:22
     * @param feed 实例对象
     * @return 数量
     */
    @Override
    public int queryCount(Feed feed) {
        return iFeedMapper.queryCount(feed);
    }

    /**
     * @Description 实体作为筛选条件查询指定行
     * @author "Freya19"
     * @date 2020-04-19 20:19:22
     * @param feed 实例对象
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    @Override
    public List<Feed> queryAllByLimit(Feed feed,int offset, int limit) {
        return iFeedMapper.querySomeByLimit(feed,offset, limit);
    }

    /**
     * @Description 新增数据
     * @author "Freya19"
     * @date 2020-04-19 20:19:22
     * @param feed 实例对象
     * @return 实例对象
     */
    @Override
    public Feed insert(Feed feed) {
        this.iFeedMapper.insert(feed);
        return feed;
    }

    /**
     * @Description 修改数据
     * @author "Freya19"
     * @date 2020-04-19 20:19:22
     * @param feed 实例对象
     * @return 实例对象
     */
    @Override
    public Feed update(Feed feed) {
        this.iFeedMapper.update(feed);
        return queryById(feed.getId());
    }

    /**
     * @Description 通过主键删除数据
     * @author "Freya19"
     * @date 2020-04-19 20:19:22
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(Integer id) {
        return iFeedMapper.deleteById(id) > 0;
    }
}
