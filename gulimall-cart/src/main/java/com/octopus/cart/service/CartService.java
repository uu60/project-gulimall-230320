package com.octopus.cart.service;

import com.octopus.cart.vo.CartItemVo;
import com.octopus.cart.vo.CartVo;

import java.util.List;

/**
 * @author octopus
 * @date 2023/4/7 19:42
 */
public interface CartService {
    CartItemVo addToCart(Long skuId, Integer num);

    CartItemVo getCartItem(Long skuId);

    CartVo getCart();

    void clearCart(String cartKey);

    void checkItem(Long skuId, Integer checked);

    void changeItemCount(Long skuId, Integer num);

    void deleteIdCartInfo(Integer skuId);

    List<CartItemVo> getCurrentUserCartItems();
}
