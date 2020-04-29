package site.pyyf.community.entity;

import lombok.*;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
@Builder
public class Event {

    private String topic;

    private int userId;

    private int entityType;

    private int entityId;

    private int entityUserId;

    //附加信息
    private Map<String, Object> data = new HashMap<>();

    public Event setData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

}
