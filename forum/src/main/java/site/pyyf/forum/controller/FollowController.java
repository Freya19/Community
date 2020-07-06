package site.pyyf.forum.controller;

import site.pyyf.forum.entity.Event;
import site.pyyf.forum.entity.Page;
import site.pyyf.forum.entity.User;
import site.pyyf.commons.utils.CommunityConstant;
import site.pyyf.commons.utils.CommunityUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController extends CommunityBaseController implements CommunityConstant {

    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId) {
        User user = hostHolder.getUser();

        iFollowService.follow(user.getId(), entityType, entityId);

        // 1. 触发关注事件，目前只涉及对 用户 关注
        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0, "已关注!");
    }

    @RequestMapping(path = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType, int entityId) {
        User user = hostHolder.getUser();

        iFollowService.unfollow(user.getId(), entityType, entityId);

        return CommunityUtil.getJSONString(0, "已取消关注!");
    }

    /**
     * 关注者
     */
    @RequestMapping(path = "/followees/{userId}", method = RequestMethod.GET)
    public String getFollow(@PathVariable("userId") int userId, Page page, Model model) {
        User user = iUserService.queryById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user", user);

        page.setLimit(5);
        page.setPath("/followees/" + userId);
        page.setRows((int)(long) iFollowService.findFollowCount(userId, ENTITY_TYPE_USER));

        //查询userId用户关注的人
        List<Map<String, Object>> userList = iFollowService.findFollow(userId, page.getOffset(), page.getLimit());
        if (userList != null) {
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users", userList);

        return "forum/followee";
    }

    /**
     * fans
     */
    @RequestMapping(path = "/followers/{userId}", method = RequestMethod.GET)
    public String getFans(@PathVariable("userId") int userId, Page page, Model model) {
        User user = iUserService.queryById(userId);
        if (user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user", user);

        page.setLimit(5);
        page.setPath("/followers/" + userId);
        page.setRows((int)(long) iFollowService.findFansCount(ENTITY_TYPE_USER, userId));

        // 1. 从Redis中查询userId用户的粉丝
        List<Map<String, Object>> userList = iFollowService.findFans(userId, page.getOffset(), page.getLimit());
        if (userList != null) {
            for (Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                // 2. 记录userId用户的粉丝 userId用户是否关注了其粉丝
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users", userList);

        return "forum/follower";
    }

    private boolean hasFollowed(int userId) {
        if (hostHolder.getUser() == null) {
            return false;
        }

        return iFollowService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
    }

}
