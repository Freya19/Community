package site.pyyf.commons.event;

import site.pyyf.forum.entity.Event;

import java.util.List;

public interface EventHandler {
    void doHandle(Event event);

    List<String> getSupportEventTypes();
}
