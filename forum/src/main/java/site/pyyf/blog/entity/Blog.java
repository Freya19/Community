package site.pyyf.blog.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * (Blog)实体类
 *
 * @author makejava
 * @since 2020-03-25 10:06:39
 */
@Accessors(chain = true)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Blog implements Serializable {
    private static final long serialVersionUID = 805409127400299502L;
    
    private Integer id;
    /**
    * 赞赏
    */
    private Boolean appreciation;
    /**
    * 评论
    */
    private Boolean commentabled;
    /**
    * 推荐
    */
    private Boolean recommend;
    /**
    * 发布
    */
    private Boolean published;
    /**
    * 种类id
    */
    private Integer categoryId;
    /**
    * 标签：原创 转载 翻译
    */
    private String flag;
    /**
    * 描述
    */
    private String description;
    /**
    * 内容
    */
    private String content;
    /**
    * 创建时间
    */
    private Date createTime;
    /**
    * 转载
    */
    private Boolean shareStatement;
    /**
    * 标题
    */
    private String title;
    /**
    * 更新时间
    */
    private Date updateTime;
    /**
    * 预览人数
    */
    private Integer views;
    /**
    * 发布用户ID
    */
    private Integer userId;

    /**
     * 博客对应的标签
     */
    private String tags;
}