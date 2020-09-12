package site.pyyf.commons.utils;

public interface CommunityConstant {

    /**
     * 激活成功
     */
    int ACTIVATION_SUCCESS = 0;

    /**
     * 重复激活
     */
    int ACTIVATION_REPEAT = 1;

    /**
     * 激活失败
     */
    int ACTIVATION_FAILURE = 2;

    /**
     * 默认状态的登录凭证的超时时间
     */
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;

    /**
     * 记住状态的登录凭证超时时间
     */
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 100;



    /**
     * 实体类型: 帖子
     */
    int ENTITY_TYPE_POST = 1;

    /**
     * 实体类型: 帖子评论
     */
    int ENTITY_TYPE_POST_COMMENT = 2;

    /**
     * 实体类型: 用户
     */
    int ENTITY_TYPE_USER = 3;

    /**
     * 实体类型: 博客
     */
    int ENTITY_TYPE_BLOG = 4;

    /**
     * 实体类型: 博客评论
     */
    int ENTITY_TYPE_BLOG_COMMENT = 5;


    /**
     * 邮件: 通知博客写作者
     */
    int EMAIL_TYPE_BLOG_USER = 0;

    /**
     * 邮件: 通知评论者
     */
    int EMAIL_TYPE_COMMENT_USER = 1;


    /**
     * 主题: 发送邮件
     */
    String TOPIC_EMAIL = "email";

    /**
     * 主题: 评论
     */
    String TOPIC_COMMENT = "comment";

    /**
     * 主题: 点赞
     */
    String TOPIC_LIKE = "like";

    /**
     * 主题: 关注
     */
    String TOPIC_FOLLOW = "follow";

    /**
     * 主题: 发帖
     */
    String TOPIC_PUBLISH = "publish";

    /**
     * 主题: 删帖
     */
    String TOPIC_DELETE = "delete";

    /**
     * 主题：更新ES
     */
    String TOPIC_UPDATE_ES = "updateEs";

    /**
     * 主题：加精
     */
    String TOPIC_WONDERFUL = "wonderful";

    /**
     * 主题：置顶
     */
    String TOPIC_TOP = "top";

    /**
     * 主题: 分享
     */
    String TOPIC_SHARE = "share";

    /**
     * 主题: 查看帖子
     */
    String TOPIC_VIEW = "view";


    /**
     * 系统用户ID
     */
    int SYSTEM_USER_ID = 1;

    /**
     * 权限: 普通用户
     */
    String AUTHORITY_USER = "user";

    /**
     * 权限: 管理员
     */
    String AUTHORITY_ADMIN = "admin";

    /**
     * 权限: 版主
     */
    String AUTHORITY_MODERATOR = "moderator";

    /**
     * redis的timelinefeed流中存放的元素个数
     */
    int FEEDTIMELINECOUNT = 20;

    /**
     * redis的TAG feed流中存放的元素个数
     */
    int FEEDTAGCOUNT = 5;

    /**
     * Feed流: 评论
     */
    int FEED_COMMENT = 1;

    /**
     * Feed流: 点赞
     */
    int FEED_LIKE = 2;

    /**
     * Feed流: 发帖
     */
    int FEED_PUBLISH = 3;

    /**
     * Feed流: 最近看的标签
     */
    int FEED_TAG = 4;

    int ORDER_TIME = 0;

    int ORDER_TOPANDSCORE = 1;

    int ORDER_PERSONALIZED = 2;

    int ORDER_SCORE = 3;

}
