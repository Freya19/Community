package site.pyyf.commons.event.Handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import site.pyyf.blog.controller.BaseController;
import site.pyyf.commons.event.EventHandler;
import site.pyyf.commons.utils.CommunityConstant;
import site.pyyf.forum.entity.Event;
import site.pyyf.forum.service.impl.ElasticsearchService;

import java.util.Arrays;
import java.util.List;

@Component
public class ESDeleteHandler extends BaseController implements EventHandler, CommunityConstant {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Override
    public void doHandle(Event event) {
        elasticsearchService.deleteDiscussPost(event.getEntityId());
    }

    @Override
    public List<String> getSupportEventTypes() {
        return Arrays.asList(TOPIC_DELETE);
    }
}
