package site.pyyf.community.entity;

import lombok.*;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UploadResult {

    private int success;
    private String message;
    private String url;

}