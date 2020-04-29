package site.pyyf.cloudDisk.entity;

import lombok.*;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EbookContent {
    private int id;
    private String contentId;
    private String content;
    private int fileId;
}
