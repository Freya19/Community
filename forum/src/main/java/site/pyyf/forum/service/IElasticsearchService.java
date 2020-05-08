package site.pyyf.forum.service;

import site.pyyf.forum.entity.DiscussPost;
import org.springframework.data.domain.Page;

public interface IElasticsearchService {
    void saveDiscussPost(DiscussPost post);

    void deleteDiscussPost(int id);

    Page<DiscussPost> searchDiscussPost(String keyword, int current, int limit) ;

}
