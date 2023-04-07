package com.octopus.cart.controller;

import com.octopus.cart.interceptor.CartInterceptor;
import com.octopus.cart.vo.UserInfoTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

/**
 * @author octopus
 * @date 2023/4/7 17:41
 */
@RestController
public class CartController {

    /**
     * 浏览器里面有一个cookie user-key，一个月过期
     * 如果第一次使用，会给一个临时的用户身份
     * @return
     */
    @GetMapping("/cart.html")
    private UserInfoTo cartListPage() {
        UserInfoTo userInfoTo = CartInterceptor.THREAD_LOCAL.get();
        return userInfoTo;
    }
}
