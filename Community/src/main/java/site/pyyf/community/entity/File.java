package site.pyyf.community.entity;


import lombok.Data;

import java.io.Serializable;

@Data
public class File implements Serializable {
    public File() {
        super();
    }
    int id;
    String absolutePath;
    String fastdfsName;
    int valid;
}
