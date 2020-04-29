package site.pyyf.community.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class ShareFilesService {

    @Value("${community.path.upload}")
    private String uploadPath;

    private static final Logger logger = LoggerFactory.getLogger(ShareFilesService.class);

    public List<String> list(int PageCurrent, int pageLimit, String path) {
        List partFiles = new ArrayList();
        List allFiles = null;
        try {
            if (path.equals("index")) {
                allFiles = Arrays.asList(new File(uploadPath + "/ShareFiles").list());
                for (int i = 0; i < allFiles.size(); i++) {
                    if (i / pageLimit == PageCurrent - 1)
                        partFiles.add(allFiles.get(i));
                }

            } else {
                allFiles = Arrays.asList(new File(uploadPath + "/ShareFiles/" + path).list());
                for (int i = 0; i < allFiles.size(); i++) {
                    if (i / pageLimit == PageCurrent - 1)
                        partFiles.add(path + "*" + allFiles.get(i));
                }
            }
            partFiles.sort((o1,o2)->{
                int i = o1.toString().substring(o1.toString().lastIndexOf("*") + 1).lastIndexOf("."); //4
                int j = o2.toString().substring(o1.toString().lastIndexOf("*") + 1).lastIndexOf("."); // -1
                return i-j;
            });

        } catch (Exception e) {
            logger.error("自己写的分页错啦");
            throw new RuntimeException("自己写的分页错啦", e);
        }
        return partFiles;
    }

    public int fileSize(String path) {
        List allFiles = null;
        if (path.equals("index")) {
            allFiles = Arrays.asList(new File(uploadPath + "/ShareFiles").list());

        } else {
            allFiles = Arrays.asList(new File(uploadPath + "/ShareFiles/" + path).list());
        }
        return allFiles.size();

    }



}
