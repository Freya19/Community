package site.pyyf.commons.event.Handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import site.pyyf.blog.controller.BaseController;
import site.pyyf.commons.event.EventHandler;
import site.pyyf.commons.utils.CommunityConstant;
import site.pyyf.forum.entity.DiscussPost;
import site.pyyf.forum.entity.Event;
import site.pyyf.forum.service.impl.DiscussPostService;
import site.pyyf.forum.service.impl.ElasticsearchService;

import java.util.Arrays;
import java.util.List;


@Component
public class ESUpdateHandler extends BaseController implements EventHandler, CommunityConstant {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private DiscussPostService discussPostService;


    @Override
    public void doHandle(Event event) {
        if (event.getEntityType() == ENTITY_TYPE_POST){
            DiscussPost post = discussPostService.findDiscussPostById(event.getEntityId());
            elasticsearchService.saveDiscussPost(post);
        }

    }

    @Override
    public List<String> getSupportEventTypes() {
        return Arrays.asList(TOPIC_COMMENT,TOPIC_FOLLOW,TOPIC_LIKE,TOPIC_PUBLISH,TOPIC_WONDERFUL,TOPIC_TOP);
    }
}
