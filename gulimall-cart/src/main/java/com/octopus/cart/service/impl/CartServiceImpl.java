package com.octopus.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.octopus.cart.constant.CartConstant;
import com.octopus.cart.feign.ProductFeignService;
import com.octopus.cart.interceptor.CartInterceptor;
import com.octopus.cart.service.CartService;
import com.octopus.cart.vo.CartItemVo;
import com.octopus.cart.vo.CartVo;
import com.octopus.cart.vo.SkuInfoVo;
import com.octopus.cart.vo.UserInfoTo;
import com.octopus.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author octopus
 * @date 2023/4/7 19:42
 */
@Service
public class CartServiceImpl implements CartService {
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    ThreadPoolExecutor pool;

    @Override
    public CartItemVo addToCart(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String res = (String) cartOps.get(skuId.toString());
        // 如果购物车包含该商品
        if (!StringUtils.isEmpty(res)) {
            CartItemVo cartItemVo = JSON.parseObject(res, CartItemVo.class);
            cartItemVo.setCount(cartItemVo.getCount() + num);
            cartOps.put(skuId.toString(), JSON.toJSONString(cartItemVo));
            return cartItemVo;
        } else {
            // 1. 远程查询当前要添加的商品的信息
            CartItemVo cartItemVo = new CartItemVo();
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                R infoR = productFeignService.getInfo(skuId);
                SkuInfoVo data = infoR.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });
                // 2. 添加到购物车
                cartItemVo.setSkuId(skuId);
                cartItemVo.setCheck(true);
                cartItemVo.setCount(num);
                cartItemVo.setTitle(data.getSkuTitle());
                cartItemVo.setImage(data.getSkuDefaultImg());
                cartItemVo.setPrice(data.getPrice());
            }, pool);

            // 远程查询sku的组合信息
            CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
                cartItemVo.setSkuAttrValues(productFeignService.getSkuSaleAttrValues(skuId));
            }, pool);

            try {
                CompletableFuture.allOf(future, future1).get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            String jsonString = JSON.toJSONString(cartItemVo);
            cartOps.put(skuId.toString(), jsonString);

            return cartItemVo;
        }
    }

    @Override
    public CartItemVo getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String res = (String) cartOps.get(skuId.toString());
        CartItemVo cartItemVo = JSON.parseObject(res, CartItemVo.class);
        return cartItemVo;
    }

    @Override
    public CartVo getCart() {
        CartVo cartVo = new CartVo();
        UserInfoTo userInfoTo = CartInterceptor.THREAD_LOCAL.get();
        String cartKey = "";
        if (userInfoTo.getUserId() != null) { // 已登录，需要合并购物车
            cartKey = CartConstant.CART_PREFIX + userInfoTo.getUserId();
            List<CartItemVo> cartItems = getCartItems(CartConstant.CART_PREFIX + userInfoTo.getUserKey());
            if (cartItems != null) {
                for (CartItemVo cartItem : cartItems) {
                    addToCart(cartItem.getSkuId(), cartItem.getCount());
                }
            }
            // 清空临时购物车
            clearCart(CartConstant.CART_PREFIX + userInfoTo.getUserKey());

            // 去redis重新查询
            cartItems = getCartItems(cartKey);
            cartVo.setItems(cartItems);
        } else {
            cartKey = CartConstant.CART_PREFIX + userInfoTo.getUserKey();
            List<CartItemVo> collect = getCartItems(cartKey);
            cartVo.setItems(collect);
        }
        return cartVo;
    }

    @Override
    public void clearCart(String cartKey) {
        redisTemplate.delete(cartKey);
    }

    private List<CartItemVo> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(cartKey);
        List<Object> values = hashOps.values();
        return values.stream().map(obj -> {
            CartItemVo cartItemVo = JSON.parseObject((String) obj, CartItemVo.class);
            return cartItemVo;
        }).collect(Collectors.toList());
    }

    /**
     * 获取我们要操作的购物车
     *
     * @return
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoTo userInfoTo = CartInterceptor.THREAD_LOCAL.get();
        String cartKey = "";
        if (userInfoTo.getUserId() != null) {
            cartKey = CartConstant.CART_PREFIX + userInfoTo.getUserId();
        } else {
            cartKey = CartConstant.CART_PREFIX + userInfoTo.getUserKey();
        }

        BoundHashOperations<String, Object, Object> hashOps = redisTemplate.boundHashOps(cartKey);
        return hashOps;
    }

    @Override
    public void checkItem(Long skuId, Integer check) {

        //查询购物车里面的商品
        CartItemVo cartItem = getCartItem(skuId);
        //修改商品状态
        cartItem.setCheck(check == 1?true:false);

        //序列化存入redis中
        String redisValue = JSON.toJSONString(cartItem);

        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.put(skuId.toString(),redisValue);

    }

    /**
     * 修改购物项数量
     * @param skuId
     * @param num
     */
    @Override
    public void changeItemCount(Long skuId, Integer num) {
        //查询购物车里面的商品
        CartItemVo cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        //序列化存入redis中
        String redisValue = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(),redisValue);
    }

    /**
     * 删除购物项
     * @param skuId
     */
    @Override
    public void deleteIdCartInfo(Integer skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }

    @Override
    public List<CartItemVo> getCurrentUserCartItems() {
        UserInfoTo userInfoTo = CartInterceptor.THREAD_LOCAL.get();
        if (userInfoTo.getUserId() == null) {
            return null;
        }
        String cartKey = CartConstant.CART_PREFIX + userInfoTo.getUserId();
        List<CartItemVo> cartItems = getCartItems(cartKey);
        return cartItems.stream().filter(CartItemVo::getCheck).map(item -> {
            // 更新为最新价格
            item.setPrice(BigDecimal.valueOf((Double) productFeignService.getPrice(item.getSkuId()).get("data")));
            return item;
        }).collect(Collectors.toList());
    }
}
