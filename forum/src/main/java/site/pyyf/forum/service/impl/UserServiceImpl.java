package site.pyyf.forum.service.impl;

import site.pyyf.forum.config.AliyunConfig;
import site.pyyf.forum.service.IUserService;
import site.pyyf.forum.entity.User;
import site.pyyf.commons.utils.CommunityConstant;
import site.pyyf.commons.utils.CommunityUtil;
import site.pyyf.commons.utils.RedisKeyUtil;
import site.pyyf.forum.entity.LoginTicket;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Description (User)表服务实现类
 *
 * @author "Gepeng18"
 * @since 2020-03-25 21:36:12
 */
@Service
public class UserServiceImpl extends BaseService implements IUserService, CommunityConstant {

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private AliyunConfig aliyunConfig;

    /**
     * @Description 通过ID查询单条数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public User queryById(Integer id) {
        User user = getCache(id);
        if (user == null) {
            user = initCache(id);
        }
        return user;
    }

    /**
     * @Description 查询多条数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    @Override
    public List<User> queryAllByLimit(int offset, int limit) {
        return iUserMapper.queryAllByLimit(User.builder().build(),offset, limit);
    }
    
    /**
     * @Description 查询全部数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @return 对象列表
     */
    @Override
    public List<User> queryAll() {
        return iUserMapper.queryAll();
    }
    
     /**
     * @Description 实体作为筛选条件查询数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @param user 实例对象
     * @return 对象列表
     */
    @Override
    public List<User> queryAll(User user) {
        return iUserMapper.queryAll(user);
    }
    
    /**
     * @Description 查询实体数量
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @return 数量
     */
    @Override
    public int queryCount() {
        return iUserMapper.queryCount();
    }
    
    /**
     * @Description 实体作为筛选条件查询数量
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @param user 实例对象
     * @return 数量
     */
    @Override
    public int queryCount(User user) {
        return iUserMapper.queryCount(user);
    }

    /**
     * @Description 实体作为筛选条件查询指定行
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @param user 实例对象
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    @Override
    public List<User> queryAllByLimit(User user,int offset, int limit) {
        return iUserMapper.queryAllByLimit(user,offset, limit);
    }
    
    /**
     * @Description 新增数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @param user 实例对象
     * @return 实例对象
     */
    @Override
    public User insert(User user) {
        if(user.getHeaderUrl()==null){
            List<String> heads = iAliyunOssService.queryAll("community/header");
            int index = new Random().nextInt(heads.size());
            user.setHeaderUrl(aliyunConfig.getUrlPrefix()+heads.get(index));
        }
        this.iUserMapper.insert(user);
        return user;
    }

    /**
     * @Description 修改数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @param user 实例对象
     * @return 实例对象
     */
    @Override
    public User update(User user) {
        this.iUserMapper.update(user);
        return queryById(user.getId());
    }

    /**
     * @Description 通过主键删除数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(Integer id) {
        return iUserMapper.deleteById(id) > 0;
    }

    /**
     * 注册功能
     * @param user
     * @return
     */
    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }

        // 验证账号
        int count = iUserMapper.queryCount(User.builder().username(user.getUsername()).build());
        if (count>0) {
            map.put("usernameMsg", "该账号已存在!");
            return map;
        }

        // 验证邮箱
        count = iUserMapper.queryCount(User.builder().email(user.getEmail()).build());
        if (count >0) {
            map.put("emailMsg", "该邮箱已被注册!");
            return map;
        }

        // 注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setUserType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setLoginTime(new Date());

        //用户头像
        List<String> heads = iAliyunOssService.queryAll("community/header");
        int index = new Random().nextInt(heads.size());
        user.setHeaderUrl(aliyunConfig.getUrlPrefix()+heads.get(index));
        user.setCreateTime(new Date());

        //往数据库里插入该用户字段
        iUserMapper.insert(user);

        // 激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // 例如http://localhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);


        return map;
    }

    public int activation(int userId, String code) {
        User user = iUserMapper.queryById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            user.setStatus(1);
            iUserMapper.update(user);
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    @Override
    public Map<String, Object> login(String username, String password, long expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }

        // 验证账号(此为普通账号验证，所以registerType为0)
        List<User> users = iUserMapper.queryAll(User.builder().username(username).registerType(0).build());

        if (users.size()>1) {
            map.put("usernameMsg", "系统出现异常!");
            return map;
        }

        if (users.size()==0) {
            map.put("usernameMsg", "该账号不存在!");
            return map;
        }
        User user = users.get(0);
        // 验证状态
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账号未激活!");
            return map;
        }

        // 验证密码
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码不正确!");
            return map;
        }

        // 生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(1);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));

        //通过 ticket 拼成 Redis的key
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey, loginTicket);
        iUserService.update(User.builder().id(user.getId()).loginTime(new Date()).build());
        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket) {
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(0);
        redisTemplate.opsForValue().set(redisKey, loginTicket);
    }

    public LoginTicket findLoginTicket(String ticket) {
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    public int updateHeader(int userId, String headerUrl) {
        final User user = queryById(userId);
        user.setHeaderUrl(headerUrl);
        int rows = iUserMapper.update(user);
        clearCache(userId);
        return rows;
    }

    public User findUserByName(String username) {
        return iUserMapper.queryAll(User.builder().username(username).build()).get(0);
    }


    /**
     * 1.优先从缓存中取值
     * @param userId
     * @return
     */
    private User getCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    /**
     *  2.取不到时初始化缓存数据
     * @param userId
     * @return
     */
    private User initCache(int userId) {
        User user = iUserMapper.queryById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    /**
     * /3.数据变更时清除缓存数据
     * @param userId
     */
    private void clearCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = this.queryById(userId);

        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {

            @Override
            public String getAuthority() {
                switch (user.getUserType()) {
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }
    
}