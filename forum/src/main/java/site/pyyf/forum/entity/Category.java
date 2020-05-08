package site.pyyf.forum.entity;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * (Category)实体类
 *
 * @author makejava
 * @since 2020-04-02 09:06:50
 */
@Accessors(chain = true)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category implements Serializable {
    private static final long serialVersionUID = 237583810766210431L;
    
    private Integer id;
    /**
    * 种类名
    */
    private String name;
    /**
    * 实体类型: 1-帖子；2-帖子下面的评论；3-用户；4-博客; 5-博客下面的评论
    */
    private Integer entityType;
    /**
    * 类别所属用户ID - 只存在于博客中
    */
    private Integer userId;
    /**
    * 属于该类别的实体个数 - 只存在于博客中
    */
    private Integer count;


}