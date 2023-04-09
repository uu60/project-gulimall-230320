package com.octopus.gulimall.controller;

import com.alibaba.fastjson.TypeReference;
import com.octopus.common.utils.R;
import com.octopus.common.vo.MemberResponseVo;
import com.octopus.gulimall.feign.MemberFeignService;
import com.octopus.gulimall.vo.GithubSocialUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpSession;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * @author octopus
 * @date 2023/4/6 01:00
 */
@Controller
public class OAuthController {

    @Autowired
    RestTemplate restTemplate;
    @Autowired
    MemberFeignService memberFeignService;

    @GetMapping("/oauth2/github/success")
    public String githubLogin(String code, HttpSession session) {
        Map<String, String> params = new HashMap<>();
        params.put("client_id", "cd06ba87a7d6c955611e");
        params.put("client_secret", "d3babe79ed05e5b0f52d4788e47bb5c8832178a9");
        params.put("code", code);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity("https://github" +
                ".com/login/oauth/access_token", params, String.class);
        String body = responseEntity.getBody();
        if (body.contains("access_token=")) {
            String accessToken = body.split("access_token=")[1].split("&scope=")[0];
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add("Authorization", "Bearer " + accessToken);
            try {
                ResponseEntity<GithubSocialUser> resp = restTemplate.exchange(new RequestEntity<>(httpHeaders,
                        HttpMethod.GET, new URI("https://api.github.com/user")), GithubSocialUser.class);
                GithubSocialUser githubSocialUser = resp.getBody();
                githubSocialUser.setAccessToken(accessToken);
                R r = memberFeignService.oauthLogin(githubSocialUser);
                if (r.getCode() == 0) {
                    MemberResponseVo data = r.getData("data", new TypeReference<MemberResponseVo>() {});
                    session.setAttribute("loginUser", data);
                    return "redirect:http://gulimall.com";
                }
            } catch (Exception e) {
                return "redirect:http://auth.gulimall.com/login.html";
            }
        }
        return "redirect:http://auth.gulimall.com/login.html";
    }
}
