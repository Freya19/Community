package site.pyyf.commons.utils;

import site.pyyf.community.entity.HotTag;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by codedrinker on 2019/8/2.
 */

@Component
@Data
public class HotTagCache {
    private List<HotTag> hots;

    public void setTags(Map<String, HotTag> tags) {
        int max = 30;
        /**
         * 这是使用优先队列，找30个最大的，这30个不分前后

        PriorityQueue<HotTag> priorityQueue = new PriorityQueue<>(max);

        tags.forEach((name, hotTag) -> {
            if (priorityQueue.size() < max) {
                priorityQueue.add(hotTag);
            } else {
                HotTag minHot = priorityQueue.peek();
                if (hotTag.compareTo(minHot) > 0) {
                    priorityQueue.poll();
                    priorityQueue.add(hotTag);
                }
            }
        });


        List<HotTag> sortedTags = new ArrayList<>();

        HotTag poll = priorityQueue.poll();
        while (poll != null) {
            sortedTags.add(poll);
            poll = priorityQueue.poll();
        }
        Collections.reverse(sortedTags);
        hots = sortedTags;
         */
        //这里我们采取每次重新统计一次，即将hots置空后进行赋值
        hots = new ArrayList<>();
        TreeSet<HotTag> set = new TreeSet<>();
        tags.forEach((name, hotTag) -> {
            set.add(hotTag);
        });

        //hots数量加到max为止 或者 加到set中元素没了为止
        while (hots.size()<max&&set.size()>0){
            hots.add(set.pollFirst());
        }
    }
}
