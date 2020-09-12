package site.pyyf.commons.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import site.pyyf.commons.utils.RedisKeyUtil;
import site.pyyf.forum.entity.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

class MyRunnable implements Runnable{
    Event event;
    Map<String, List<EventHandler>> config;

    public MyRunnable(Event event, Map<String, List<EventHandler>> config) {
        this.event = event;
        this.config = config;
    }

    @Override
    public void run() {
        for (EventHandler handler : config.get(event.getTopic())) {
            handler.doHandle(event);
        }
    }
}

@Service
public class EventConsumer implements InitializingBean, ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);
    private Map<String, List<EventHandler>> config = new HashMap<String, List<EventHandler>>();
    private ApplicationContext applicationContext;

    ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Autowired
    protected RedisTemplate redisTemplate;


    @Override
    public void afterPropertiesSet() throws Exception {

        Map<String, EventHandler> beans = applicationContext.getBeansOfType(EventHandler.class);
        if (beans != null) {
            for (Map.Entry<String, EventHandler> entry : beans.entrySet()) {
                List<String> eventTypes = entry.getValue().getSupportEventTypes();

                for (String type : eventTypes) {
                    if (!config.containsKey(type)) {
                        config.put(type, new ArrayList<EventHandler>());
                    }
                    config.get(type).add(entry.getValue());
                }
            }
        }


        executorService.execute(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    String key = RedisKeyUtil.getTopicKey();
                    Event event = (Event) redisTemplate.opsForList().rightPop(key,5, TimeUnit.DAYS);

                    if (!config.containsKey(event.getTopic())) {
                        logger.error("不能识别的事件");
                        continue;
                    }

                    Runnable runnable = new MyRunnable(event,config);
                    executorService.execute(runnable);

                }
            }
        });
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
