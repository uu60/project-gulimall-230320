package com.octopus.cart.controller;

import com.octopus.cart.interceptor.CartInterceptor;
import com.octopus.cart.service.CartService;
import com.octopus.cart.vo.CartItemVo;
import com.octopus.cart.vo.CartVo;
import com.octopus.cart.vo.UserInfoTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author octopus
 * @date 2023/4/7 17:41
 */
@Controller
public class CartController {

    @Autowired
    CartService cartService;

    /**
     * 浏览器里面有一个cookie user-key，一个月过期
     * 如果第一次使用，会给一个临时的用户身份
     * @return
     */
    @GetMapping("/cart.html")
    public String cartList(Model model) {
        CartVo cartVo = cartService.getCart();
        model.addAttribute("cart", cartVo);
        return "cartList";
    }

    @GetMapping("/addCartItem")
    public String addToCart(Long skuId, Integer num, Model model) {
        CartItemVo cartItemVo = cartService.addToCart(skuId, num);

        model.addAttribute("cartItem", cartItemVo);
        return "redirect:http://cart.gulimall.com/addToCartSuccessPage.html?skuId=" + skuId;
    }

    @GetMapping(value = "/addToCartSuccessPage.html")
    public String addToCartSuccessPage(Long skuId, Model model) {
        CartItemVo cartItemVo = cartService.getCartItem(skuId);

        model.addAttribute("cartItem", cartItemVo);
        return "success";
    }

    /**
     * 更改选中状态
     */
    @GetMapping(value = "/checkItem")
    public String checkItem(@RequestParam(value = "skuId") Long skuId,
                            @RequestParam(value = "checked") Integer checked) {
        cartService.checkItem(skuId,checked);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    /**
     * 改变商品数量
     */
    @GetMapping(value = "/countItem")
    public String countItem(@RequestParam(value = "skuId") Long skuId,
                            @RequestParam(value = "num") Integer num) {
        cartService.changeItemCount(skuId,num);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

    /**
     * 删除商品信息
     */
    @GetMapping(value = "/deleteItem")
    public String deleteItem(@RequestParam("skuId") Integer skuId) {
        cartService.deleteIdCartInfo(skuId);
        return "redirect:http://cart.gulimall.com/cart.html";
    }

}
