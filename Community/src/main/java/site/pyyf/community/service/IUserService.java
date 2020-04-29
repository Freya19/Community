package site.pyyf.community.service;

import site.pyyf.community.entity.User;
import site.pyyf.community.entity.LoginTicket;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @Description (User)表服务接口
 *
 * @author "Gepeng18"
 * @since 2020-03-25 21:36:12
 */
public interface IUserService {

    /**
     * @Description 通过ID查询单条数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @param id 主键
     * @return 实例对象
     */
    User queryById(Integer id);
    
     /**
     * @Description 查询全部数据
     * @author makejava
     * @date 2020-03-25 21:36:12
     * @return 对象列表
     */
    List<User> queryAll();
    
    /**
     * @Description 实体作为筛选条件查询数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @param user 实例对象
     * @return 对象列表
     */
    List<User> queryAll(User user);
   
    /**
     * @Description 查询实体数量
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @return 数量
     */
    int queryCount();
    
    /**
     * @Description 实体作为筛选条件查询数量
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @param user 实例对象
     * @return 数量
     */
    int queryCount(User user);
   
   
    /**
     * @Description 查询多条数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<User> queryAllByLimit(int offset, int limit);

    /**
     * @Description 实体作为筛选条件查询指定行
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @param user 实例对象
     * @param offset 查询起始位置
     * @param limit 查询条数
     * @return 对象列表
     */
    List<User> queryAllByLimit(User user, int offset, int limit);

    
    /**
     * @Description 新增数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @param user 实例对象
     * @return 实例对象
     */
    User insert(User user);

    /**
     * @Description 修改数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @param user 实例对象
     * @return 实例对象
     */
    User update(User user);

    /**
     * @Description 通过主键删除数据
     * @author "Gepeng18"
     * @date 2020-03-25 21:36:12
     * @param id 主键
     * @return 是否成功
     */
    boolean deleteById(Integer id);

    Map<String, Object> register(User user) ;

    int activation(int userId, String code);

    Map<String, Object> login(String username, String password, long expiredSeconds);

    void logout(String ticket);

    LoginTicket findLoginTicket(String ticket);

    Collection<? extends GrantedAuthority> getAuthorities(int userId);

    int updateHeader(int userId, String headerUrl);

    User findUserByName(String username);

}