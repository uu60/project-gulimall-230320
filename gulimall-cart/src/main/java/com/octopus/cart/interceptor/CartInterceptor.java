package com.octopus.cart.interceptor;

import com.octopus.cart.vo.UserInfoTo;
import com.octopus.common.vo.MemberResponseVo;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * @author octopus
 * @date 2023/4/7 17:50
 */
public class CartInterceptor implements HandlerInterceptor {

    public static final ThreadLocal<UserInfoTo> THREAD_LOCAL = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserInfoTo userInfoTo = new UserInfoTo();
        HttpSession session = request.getSession();
        MemberResponseVo responseVo;
        if ((responseVo = (MemberResponseVo) session.getAttribute("loginUser")) != null) {
            // 已登录
            userInfoTo.setUserId(responseVo.getId());
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                // user-key
                String name = cookie.getName();
                if (name.equals("user-key")) {
                    userInfoTo.setUserKey(cookie.getValue());
                }
            }
        }

        if (StringUtils.isEmpty(userInfoTo.getUserKey())) {
            userInfoTo.setUserKey(String.valueOf(UUID.randomUUID()));
        }

        THREAD_LOCAL.set(userInfoTo);
        Cookie cookie = new Cookie("user-key", userInfoTo.getUserKey());
        cookie.setDomain("gulimall.com");
        cookie.setMaxAge(60 * 60 * 24 * 30);

        response.addCookie(cookie);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
        // 使用@RestController修改了response，已经提前返回给客户端了，到这不能再进行修改
    }
}
