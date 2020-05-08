package site.pyyf.forum.entity;

import java.util.Date;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * (Comment)实体类
 *
 * @author makejava
 * @since 2020-03-26 11:56:00
 */
@Accessors(chain = true)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment implements Serializable {
    private static final long serialVersionUID = -23596352330359536L;
    
    private Integer id;
    /**
    * 评论人ID
    */
    private Integer userId;
    /**
    * 实体类型: 1-帖子；2-帖子下面的评论；3-用户；4-博客; 5-博客下面的评论
    */
    private Integer entityType;
    /**
    * 实体ID
    */
    private Integer entityId;
    /**
    * 回复的目标ID
    */
    private Integer targetId;
    /**
    * 内容
    */
    private String content;
    /**
    * 0表示无效，1表示有效
    */
    private Integer status;
    /**
    * 创建时间
    */
    private Date createTime;


}