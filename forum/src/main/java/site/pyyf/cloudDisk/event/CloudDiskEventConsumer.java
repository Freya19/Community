package site.pyyf.cloudDisk.event;

import com.alibaba.fastjson.JSONObject;
import site.pyyf.cloudDisk.controller.CloudDiskBaseController;
import site.pyyf.cloudDisk.entity.Event;
import site.pyyf.cloudDisk.entity.MyFile;
import site.pyyf.cloudDisk.utils.CloudDiskConstant;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Component
public class CloudDiskEventConsumer extends CloudDiskBaseController implements CloudDiskConstant {

    private static final Logger logger = LoggerFactory.getLogger(CloudDiskEventConsumer.class);

    @KafkaListener(topics = {TOPIC_FILES_DELETE})
    public void handleDeleteFilesOrFolders(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("消息的内容为空!");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式错误!");
            return;
        }

        int entityType= event.getEntityType();
        if(entityType==0){
            //删除文件
            MyFile deletingFile = JSONObject.parseObject(event.getEntityInfo(),MyFile.class);
            iFileStoreService.deleteFile(deletingFile);
            logger.debug("我kafka把文件 "+deletingFile.getMyFileName()+" 删除啦");

            if (StringUtils.substringAfterLast(deletingFile.getMyFileName(), ".").equals("md")) {
                logger.debug("我kafka把markdown文件 "+deletingFile.getMyFileName()+" 的content删除啦");
                iEbookService.deleteById(deletingFile.getId());
                iEbookContentService.deleteById(deletingFile.getId());
            }
        }



    }


}
