package com.octopus.gulimall.order.interceptor;

import com.octopus.common.vo.MemberResponseVo;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author octopus
 * @date 2023/4/8 16:07
 */
public class LoginUserInterceptor implements HandlerInterceptor {

    public final static ThreadLocal<MemberResponseVo> LOGIN_USER = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        boolean match = antPathMatcher.match("/order/order/status/**", uri);
        boolean match1 = antPathMatcher.match("/payed/notify", uri);
        if (match || match1) {
            return true;
        }

        MemberResponseVo loginUser = (MemberResponseVo) request.getSession().getAttribute("loginUser");
        if (loginUser != null) {
            LOGIN_USER.set(loginUser);
            return true;
        }
        // 还没登录去登录
        request.getSession().setAttribute("msg", "请先进行登录");
        response.sendRedirect("http://auth.gulimall.com/login.html");
        return false;
    }
}
