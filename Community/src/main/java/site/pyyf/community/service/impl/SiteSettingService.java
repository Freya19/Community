package site.pyyf.community.service.impl;


import site.pyyf.community.service.ISiteSettingService;
import org.springframework.stereotype.Service;

@Service
public class SiteSettingService extends BaseService implements ISiteSettingService {

    public int allowRegister(){
        return iSiteSettingMapper.getSiteSetting("允许注册");
    }

    public int allowKaptchaLogin(){
        return iSiteSettingMapper.getSiteSetting("验证码登录");
    }
    public int allowOnlineExecutor(){
        return iSiteSettingMapper.getSiteSetting("允许在线编译");
    }

}
