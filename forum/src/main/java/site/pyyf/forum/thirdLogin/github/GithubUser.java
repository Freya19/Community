package site.pyyf.forum.thirdLogin.github;

import lombok.Data;

/**
 * Created by codedrinker on 2019/4/24.
 */
@Data
public class GithubUser {
    private String login;
    private Long id;
    private String bio;
    private String avatarUrl;
}
