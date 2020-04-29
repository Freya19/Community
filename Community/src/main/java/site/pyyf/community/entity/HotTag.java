package site.pyyf.community.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Created by codedrinker on 2019/8/2.
 */
@Data
@Builder
@AllArgsConstructor
public class HotTag implements Comparable {
    private String name;
    private Double score;
    private Integer count;
    //从高到低排
    @Override
    public int compareTo(Object o) {
        return (int)( ((HotTag) o).getScore() - this.getScore() );
    }
}
