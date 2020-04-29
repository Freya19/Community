package site.pyyf.community.service;

import site.pyyf.community.entity.DiscussPost;
import org.springframework.data.domain.Page;

public interface IElasticsearchService {
    void saveDiscussPost(DiscussPost post);

    void deleteDiscussPost(int id);

    Page<DiscussPost> searchDiscussPost(String keyword, int current, int limit) ;

}
