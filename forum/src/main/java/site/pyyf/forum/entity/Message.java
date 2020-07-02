package site.pyyf.forum.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Message {

    //
    private int id;

    /**
     * 发送方的id
     */
    private int fromId;

    /**
     * 接收方的id
     */
    private int toId;

    /**
     * 会话id （fromID_toId or toId_from_Id)
     */
    private String conversationId;

    /**
     * 发送的内容
     */
    private String content;

    /**
     *
     */
    private int status;

    private Date createTime;

}
