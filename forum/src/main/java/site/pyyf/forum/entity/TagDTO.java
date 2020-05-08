package site.pyyf.forum.entity;

import lombok.Data;

import java.util.List;

/**
 * Created by codedrinker on 2019/6/5.
 */
@Data
public class TagDTO {
    private String group;
    private List<String> tags;
}
