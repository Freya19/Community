package site.pyyf.community.service;

import java.util.*;

public interface IFollowService {
    void follow(int userId, int entityType, int entityId) ;

    void unfollow(int userId, int entityType, int entityId);

    // 查询关注的实体的数量
    Long findFolloweeCount(int userId, int entityType) ;

    // 查询实体的粉丝的数量
    Long findFollowerCount(int entityType, int entityId) ;

    // 查询当前用户是否已关注该实体
    boolean hasFollowed(int userId, int entityType, int entityId) ;
    // 查询某用户关注的人
    List<Map<String, Object>> findFollowees(int userId, int offset, int limit) ;

    // 查询某用户的粉丝
    List<Map<String, Object>> findFollowers(int userId, int offset, int limit) ;

}
