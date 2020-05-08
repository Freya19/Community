package site.pyyf.forum.service;

/**
 * @Description (SiteSetting)表服务接口
 *
 * @author "Gepeng18"
 * @since 2020-03-27 08:27:16
 */
public interface ISiteSettingService {
    int allowRegister();

    int allowKaptchaLogin();
    int allowOnlineExecutor();

}