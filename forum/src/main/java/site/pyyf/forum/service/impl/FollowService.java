package site.pyyf.forum.service.impl;

import site.pyyf.forum.entity.User;
import site.pyyf.forum.service.IFollowService;
import site.pyyf.commons.utils.CommunityConstant;
import site.pyyf.commons.utils.RedisKeyUtil;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowService extends BaseService implements IFollowService,CommunityConstant{


    /**
     * 关注和取关模块都是操作 Redis（因为快，操作频繁）
     * 存储两次：关注的目标、粉丝。所以一项业务有两次存储，所以要保证事务
     * @param userId “我 ”
     * @param entityType  关注的目标可以是 用户、帖子、题目等  ———————— 统一定义为 Entity
     * @param entityId  实体的id
     */
    @Override
    public void follow(int userId, int entityType, int entityId) {
        //添加事务
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followKey = RedisKeyUtil.getFollowKey(userId, entityType);
                String fansKey = RedisKeyUtil.getFansKey(entityType, entityId);

                //启动事务
                operations.multi();

                operations.opsForZSet().add(followKey, entityId, System.currentTimeMillis());
                operations.opsForZSet().add(fansKey, userId, System.currentTimeMillis());

                //提交事务
                return operations.exec();
            }
        });
    }

    @Override
    public void unfollow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followKey = RedisKeyUtil.getFollowKey(userId, entityType);
                String fansKey = RedisKeyUtil.getFansKey(entityType, entityId);

                operations.multi();

                operations.opsForZSet().remove(followKey, entityId);
                operations.opsForZSet().remove(fansKey, userId);

                return operations.exec();
            }
        });
    }

    // 查询关注的实体的数量
    @Override
    public Long findFollowCount(int userId, int entityType) {
        String followKey = RedisKeyUtil.getFollowKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followKey);
    }

    // 查询实体的粉丝的数量
    @Override
    public Long findFansCount(int entityType, int entityId) {
        String fansKey = RedisKeyUtil.getFansKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(fansKey);
    }

    // 查询当前用户是否已关注该实体
    @Override
    public boolean hasFollowed(int userId, int entityType, int entityId) {
        String followKey = RedisKeyUtil.getFollowKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followKey, entityId) != null;
    }

    // 查询某用户关注的人
    @Override
    public List<Map<String, Object>> findFollow(int userId, int offset, int limit) {
        String followKey = RedisKeyUtil.getFollowKey(userId, ENTITY_TYPE_USER);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followKey, offset, offset + limit - 1);

        if (targetIds == null) {
            return null;
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer targetId : targetIds) {
            Map<String, Object> map = new HashMap<>();
            User user = iUserService.queryById(targetId);
            map.put("user", user);
            Double score = redisTemplate.opsForZSet().score(followKey, targetId);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }

        return list;
    }

    // 查询某用户的粉丝
    @Override
    public List<Map<String, Object>> findFans(int userId, int offset, int limit) {
        String fansKey = RedisKeyUtil.getFansKey(ENTITY_TYPE_USER, userId);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(fansKey, offset, offset + limit - 1);

        if (targetIds == null) {
            return null;
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer targetId : targetIds) {
            Map<String, Object> map = new HashMap<>();
            User user = iUserService.queryById(targetId);
            map.put("user", user);
            Double score = redisTemplate.opsForZSet().score(fansKey, targetId);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }

        return list;
    }

}
