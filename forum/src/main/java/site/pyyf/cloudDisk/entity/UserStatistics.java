package site.pyyf.cloudDisk.entity;

import site.pyyf.forum.entity.User;
import lombok.*;

import java.io.Serializable;

/**
 * @ClassName: UserStatistics
 * @Description: 文件仓库数据统计类
 * @author: xw
 * @date 2020/2/10 21:40
 * @Version: 1.0
 **/
@Builder
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class UserStatistics implements Serializable {
    /**
     * 文件仓库
     */
    private User user;
    /**
     * 文档数
     */
    private int doc;
    /**
     * 音乐数
     */
    private int music;
    /**
     * 视频数
     */
    private int video;
    /**
     * 图像数
     */
    private int image;
    /**
     * 其他
     */
    private int other;
    /**
     * 文件数
     */
    private int fileCount;
    /**
     * 文件夹数
     */
    private int folderCount;

}
