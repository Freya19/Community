package site.pyyf.commons.utils;

public class RedisKeyUtil implements CommunityConstant{

    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOW = "follow";
    private static final String PREFIX_FANS = "fans";
    private static final String PREFIX_KAPTCHA = "kaptcha";
    private static final String PREFIX_TICKET = "ticket";
    private static final String PREFIX_USER = "user";
    private static final String PREFIX_UV = "uv";
    private static final String PREFIX_DAU = "dau";
    private static final String PREFIX_POST = "post";
    private static final String PREFIX_TIMELINE_LATEST = "timeline:latest";
    private static final String PREFIX_TIMELINE_PERSISTENCE = "timeline:persistence";
    private static final String PREFIX_TAG_LATEST = "tag:latest";
    private static final String PREFIX_TAG_PERSISTENCE = "tag:persistence";

    // 某个实体的赞
    // like:entity:entityType:entityId -> set(userId)
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    // 某个用户的赞
    // like:user:userId -> int
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    // 某个用户关注的实体
    // follow:userId:entityType -> zset(entityId,now)
    public static String getFollowKey(int userId, int entityType) {
        return PREFIX_FOLLOW + SPLIT + userId + SPLIT + entityType;
    }

    // 某个实体拥有的粉丝
    // fans:entityType:entityId -> zset(userId,now)
    public static String getFansKey(int entityType, int entityId) {
        return PREFIX_FANS + SPLIT + entityType + SPLIT + entityId;
    }

    // 登录验证码
    public static String getKaptchaKey(String owner) {
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    // 登录的凭证
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    // 用户
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }

    // 单日UV
    public static String getUVKey(String date) {
        return PREFIX_UV + SPLIT + date;
    }

    // 区间UV
    public static String getUVKey(String startDate, String endDate) {
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    // 单日活跃用户
    public static String getDAUKey(String date) {
        return PREFIX_DAU + SPLIT + date;
    }

    // 区间活跃用户
    public static String getDAUKey(String startDate, String endDate) {
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

    // 帖子分数
    public static String getPostScoreKey() {
        return PREFIX_POST + SPLIT + "score";
    }

    //获得最新的的timeline
    public static String getLatestTimelineKey(int userId) {
        return PREFIX_TIMELINE_LATEST + SPLIT + String.valueOf(userId) ;
    }

    //获得持久化的timeline
    public static String getPersistenceTimelineKey(int userId) {
        return PREFIX_TIMELINE_PERSISTENCE + SPLIT + String.valueOf(userId) ;
    }

    //获得最新的的timeline
    public static String getLatestViewTagsKey(int userId) {
        return PREFIX_TAG_LATEST + SPLIT + String.valueOf(userId) ;
    }

    //获得持久化的timeline
    public static String getPersistenceViewTagsKey(int userId) {
        return PREFIX_TAG_PERSISTENCE + SPLIT + String.valueOf(userId) ;
    }
}
