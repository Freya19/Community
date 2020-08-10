package site.pyyf.commons.utils;

import site.pyyf.forum.entity.Tag;
import lombok.Data;
import org.springframework.stereotype.Component;
import sun.misc.PostVMInitHook;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by codedrinker on 2019/8/2.
 */

@Component
@Data
public class TagCache {
    private List<Tag> showTags;

    public void setTags(List<Tag> tags) {

        int max = 100;
        /**
         * 这是使用优先队列，找30个最大的，这30个不分前后

         PriorityQueue<Tag> priorityQueue = new PriorityQueue<>(max);

         tags.forEach((name, Tag) -> {
         if (priorityQueue.size() < max) {
         priorityQueue.add(Tag);
         } else {
         Tag minHot = priorityQueue.peek();
         if (Tag.compareTo(minHot) > 0) {
         priorityQueue.poll();
         priorityQueue.add(Tag);
         }
         }
         });

         List<Tag> sortedTags = new ArrayList<>();

         Tag poll = priorityQueue.poll();
         while (poll != null) {
         sortedTags.add(poll);
         poll = priorityQueue.poll();
         }
         Collections.reverse(sortedTags);
         hots = sortedTags;
         */

        TreeSet<Tag> set = new TreeSet<>();
        tags.forEach(tag -> {
            set.add(tag);
        });

        //这里我们采取每次重新统计一次，即将hots置空后进行赋值
        //使用CopyOnWriteArrayList提高并发访问效率
        showTags = new CopyOnWriteArrayList<>();
        //hots数量加到max为止 或者 加到set中元素没了为止
        while (showTags.size() < max && set.size() > 0) {
            showTags.add(set.pollFirst());
        }
    }


}
