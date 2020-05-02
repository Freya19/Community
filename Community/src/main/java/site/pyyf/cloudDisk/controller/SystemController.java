package site.pyyf.cloudDisk.controller;

import site.pyyf.cloudDisk.entity.FileFolder;
import site.pyyf.cloudDisk.entity.MyFile;
import site.pyyf.cloudDisk.entity.UserStatistics;
import site.pyyf.cloudDisk.utils.CloudDiskConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.tools.ToolProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: SystemController
 * @Description: 系统页面跳转控制器
 * @author: xw
 * @date 2020/2/25 22:02
 * @Version: 1.0
 **/
@Controller
public class SystemController extends CloudDiskBaseController implements CloudDiskConstant {

    private static final Logger logger = LoggerFactory.getLogger(SystemController.class);

    /**
     * @return java.lang.String
     * @Description 前往我的云盘
     * @Author xw
     * @Date 23:28 2020/2/10
     * @Param [fId, fName, error, map]
     **/
    @GetMapping("/files")
    public String toUserPage(@RequestParam(value = "fId", defaultValue = "0") Integer fId,
                             Integer error, Map<String, Object> map) {
        if (fId == 0)
            fId = iUserService.queryById(ROOTUSERID).getRootFolder();

        if (!verifySecret(fId))
            return "redirect:/files?fId=0";

        //分页不好分，就不分了
//        //连带着加密文件也搜出来了
//        int folderCount = iFileFolderService.queryCount(FileFolder.builder().parentFolderId(fId).build());;
//        int fileCount = iMyFileService.queryCount(MyFile.builder().parentFolderId(fId).build());
//        if(hostHolder.getUser().getId()!=8&&hostHolder.getUser().getId()!=14){
//            folderCount = fileCount-1;
//        }
//
//        page.setRows(folderCount + fileCount);
//        page.setPath("/files?fId="+fId);


        //判断是否包含错误信息
        if (error != null) {
            if (error == 1) {
                map.put("error", "添加失败！当前已存在同名文件夹");
            }
            if (error == 2) {
                map.put("error", "重命名失败！文件夹已存在");
            }
        }
        //包含的子文件夹
        List<FileFolder> folders = null;
        //包含的文件
        List<MyFile> files = null;
        //当前文件夹信息
        FileFolder nowFolder = null;
        //当前文件夹的相对路径
        List<FileFolder> location = new ArrayList<>();


        folders = iFileFolderService.queryAll(FileFolder.builder().parentFolderId(fId).build());
        files = iMyFileService.queryAll(MyFile.builder().parentFolderId(fId).build());
        nowFolder = iFileFolderService.queryById(fId);

        //遍历查询当前目录
        FileFolder temp = nowFolder;
        while (temp.getParentFolderId() != 0) {
            temp = iFileFolderService.queryById(temp.getParentFolderId());
            location.add(temp);
        }

        Collections.reverse(location);
        if (location.size() > 0)
            location.remove(0);
        //获得统计信息
        UserStatistics statistics = iMyFileService.getCountStatistics(ROOTUSERID);
        map.put("statistics", statistics);
        map.put("folders", folders);
        map.put("files", files);
        map.put("nowFolder", nowFolder);
        map.put("location", location);

        map.put("loginUserId", hostHolder.getUser() == null ? 0 : hostHolder.getUser().getId());
        
        logger.info("云盘页面域中的数据显示成功");
        return "cloudDisk/clouddisk/files";
    }

    public boolean verifySecret(Integer fId) {

        //遍历查询当前目录
        FileFolder current = iFileFolderService.queryById(fId);
        //因为根文件夹不可能加密，所以正好退出循环
        while (current.getParentFolderId()!= 0) {

            //访问加密文件夹但未登录
            if (current.getId() == 3 && (hostHolder.getUser() == null))
                return false;
            //访问加密文件夹但是不是8也不是14
            if (current.getId() == 3 && (hostHolder.getUser().getId() != 8 && hostHolder.getUser().getId() != 14))
                return false;

            current = iFileFolderService.queryById(current.getParentFolderId());
        }

        return true;
    }
}
