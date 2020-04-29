package site.pyyf.community.entity;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * (Tag)实体类
 *
 * @author makejava
 * @since 2020-04-02 09:06:49
 */
@Accessors(chain = true)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tag implements Serializable {
    private static final long serialVersionUID = -66221013929440314L;
    
    private Integer id;
    /**
    * 标签名
    */
    private String name;
    /**
    * 实体类型: 1-帖子；2-帖子下面的评论；3-用户；4-博客; 5-博客下面的评论
    */
    private Integer entityType;
    /**
    * 属于该类别的实体个数 - 只存在于博客中
    */
    private Integer count;


}