package site.pyyf.community.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * (User)实体类
 *
 * @author makejava
 * @since 2020-03-28 19:18:27
 */
@Accessors(chain = true)
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    private static final long serialVersionUID = 669838070366072637L;
    
    private Integer id;
    /**
    * 用户名
    */
    private String username;
    /**
    * 密码
    */
    private String password;
    /**
    * 用户的openid
    */
    private String openId;
    /**
    * MD5盐
    */
    private String salt;
    /**
    * 电子邮箱
    */
    private String email;
    /**
    * 0-普通用户; 1-超级管理员; 2-版主;
    */
    private Integer userType;
    /**
    * 0-未激活; 1-已激活;
    */
    private Integer status;
    /**
    * 激活码
    */
    private String activationCode;
    /**
    * 头像
    */
    private String headerUrl;
    /**
    * 创建时间
    */
    private Date createTime;
    /**
    * 当前网盘容量（单位KB)
    */
    private Integer currentSize;
    /**
    * 最大容量（单位KB)
    */
    private Integer maxSize;

    /**
     * 0是网站注册，1是github注册，2是qq注册
     */
    private Integer registerType;

    /**
     * 最近一次登录时间
     */
    private Date loginTime;

    /**
     * 根文件夹
     */
    private Integer rootFolder;
}