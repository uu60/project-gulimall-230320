package com.octopus.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.octopus.common.utils.PageUtils;
import com.octopus.common.utils.Query;
import com.octopus.common.utils.R;
import com.octopus.common.vo.MemberResponseVo;
import com.octopus.gulimall.order.constant.OrderConstant;
import com.octopus.gulimall.order.dao.OrderDao;
import com.octopus.gulimall.order.entity.OrderEntity;
import com.octopus.gulimall.order.entity.OrderItemEntity;
import com.octopus.gulimall.order.feign.CartFeignService;
import com.octopus.gulimall.order.feign.MemberFeignService;
import com.octopus.gulimall.order.feign.ProductFeignService;
import com.octopus.gulimall.order.feign.WareFeignService;
import com.octopus.gulimall.order.interceptor.LoginUserInterceptor;
import com.octopus.gulimall.order.service.OrderItemService;
import com.octopus.gulimall.order.service.OrderService;
import com.octopus.gulimall.order.to.OrderCreateTo;
import com.octopus.gulimall.order.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    MemberFeignService memberFeignService;
    @Autowired
    CartFeignService cartFeignService;
    @Autowired
    WareFeignService wareFeignService;
    @Autowired
    ProductFeignService productFeignService;
    @Autowired
    StringRedisTemplate redisTemplate;
    @Autowired
    OrderItemService orderItemService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(new Query<OrderEntity>().getPage(params), new QueryWrapper<OrderEntity>());

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() {
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        MemberResponseVo responseVo = LoginUserInterceptor.LOGIN_USER.get();

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 1. 远程查询收货地址列表
            R resp = memberFeignService.getAddresses(responseVo.getId());
            orderConfirmVo.setMemberAddressVos(resp.getData("data", new TypeReference<List<MemberAddressVo>>() {
            }));
        });

        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            // 2. 远程查询购物车所有选中的购物项
            List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
            orderConfirmVo.setItems(currentUserCartItems);
        }).thenRunAsync(() -> {
            List<OrderItemVo> items = orderConfirmVo.getItems();
            List<Long> collect = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
            R resp = wareFeignService.getSkusHasStock(collect);
            List<SkuStockVo> data = resp.getData("data", new TypeReference<List<SkuStockVo>>() {
            });
            if (data != null) {
                Map<Long, Boolean> collect1 = data.stream().collect(Collectors.toMap(SkuStockVo::getSkuId,
                        SkuStockVo::getHasStock));
                orderConfirmVo.setStocks(collect1);
            }
        });

        // 3. 查询用户积分
        Integer integration = responseVo.getIntegration();
        orderConfirmVo.setIntegration(integration);

        // 4. 其他数据在模版渲染过程自动调用getter计算

        // 5. 防重令牌
        UUID uuid = UUID.randomUUID();
        String token = uuid.toString();
        redisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + responseVo.getId(), token, 30,
                TimeUnit.MINUTES);

        orderConfirmVo.setOrderToken(token);

        try {
            CompletableFuture.allOf(future, future1).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return orderConfirmVo;
    }

    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        SubmitOrderResponseVo submitOrderResponseVo = new SubmitOrderResponseVo();
        MemberResponseVo responseVo = LoginUserInterceptor.LOGIN_USER.get();
        // 1. 验证令牌
        // 0 代表失败
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return "
                + "0 end";
        String orderToken = vo.getOrderToken();
        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                Arrays.asList(OrderConstant.USER_ORDER_TOKEN_PREFIX + responseVo.getId()), orderToken);
        if (result == 1) { // 令牌验证成功
            // 下单创建订单
            OrderCreateTo order = createOrder();
            // 验价
            BigDecimal payAmount = order.getOrder().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();

            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) < 0.01) {
                // 保存订单
                saveOrder(order);

                //4、库存锁定,只要有异常，回滚订单数据
                //订单号、所有订单项信息(skuId,skuNum,skuName)
                WareSkuLockVo lockVo = new WareSkuLockVo();
                lockVo.setOrderSn(order.getOrder().getOrderSn());

                //获取出要锁定的商品数据信息【order里面存储的是Entity】
                List<OrderItemVo> orderItemVos = order.getOrderItems().stream().map((item) -> {
                    OrderItemVo orderItemVo = new OrderItemVo();
                    orderItemVo.setSkuId(item.getSkuId());
                    orderItemVo.setCount(item.getSkuQuantity());
                    orderItemVo.setTitle(item.getSkuName());
                    return orderItemVo;
                }).collect(Collectors.toList());
                lockVo.setLocks(orderItemVos);
                R resp = wareFeignService.orderLockStock(lockVo);
                if (resp.getCode() == 0) {
                    // 锁成功了
                    submitOrderResponseVo.setCode(0);
                    submitOrderResponseVo.setOrder(order.getOrder());
                    return submitOrderResponseVo;
                } else {
                    // 锁失败了
                    submitOrderResponseVo.setCode(3);
                    return submitOrderResponseVo;
                }
            } else {
                submitOrderResponseVo.setCode(2);
                return submitOrderResponseVo;
            }
        } else {
            // 令牌验证失败
            submitOrderResponseVo.setCode(1);
            return submitOrderResponseVo;
        }
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        OrderEntity orderEntity = baseMapper.selectOne(new QueryWrapper<OrderEntity>().eq("order_sn", orderSn));
        return orderEntity;
    }

    private void saveOrder(OrderCreateTo orderCreateTo) {

        //获取订单信息
        OrderEntity order = orderCreateTo.getOrder();
        order.setModifyTime(new Date());
        order.setCreateTime(new Date());
        // 默认失败用于测试
        order.setStatus(4);
        //保存订单
        this.baseMapper.insert(order);

        //获取订单项信息
        List<OrderItemEntity> orderItems = orderCreateTo.getOrderItems();
        //批量保存订单项数据
        orderItemService.saveBatch(orderItems);
    }

    private OrderCreateTo createOrder() {
        OrderCreateTo orderCreateTo = new OrderCreateTo();
        // 创建订单号
        OrderEntity orderEntity = new OrderEntity();
        orderCreateTo.setOrder(orderEntity);
        String orderSn = IdWorker.getTimeId();
        orderEntity.setOrderSn(orderSn);

        // TODO: 运费地址相关业务
        orderCreateTo.setFare(new BigDecimal("6"));

        // 获取所有的订单项
        List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
        if (currentUserCartItems != null) {
            List<OrderItemEntity> collect = currentUserCartItems.stream().map(cartItem -> {
                OrderItemEntity orderItemEntity = buildOrderItem(cartItem);
                orderItemEntity.setOrderSn(orderSn);
                return orderItemEntity;
            }).collect(Collectors.toList());
            orderCreateTo.setOrderItems(collect);
        }

        computePrice(orderCreateTo);

        return orderCreateTo;
    }

    private void computePrice(OrderCreateTo orderCreateTo) {
        OrderEntity orderEntity = orderCreateTo.getOrder();
        List<OrderItemEntity> orderItemEntities = orderCreateTo.getOrderItems();

        //总价
        BigDecimal total = new BigDecimal("0.0");
        //优惠价
        BigDecimal coupon = new BigDecimal("0.0");
        BigDecimal intergration = new BigDecimal("0.0");
        BigDecimal promotion = new BigDecimal("0.0");

        //积分、成长值
        Integer integrationTotal = 0;
        Integer growthTotal = 0;
        // 1. 订单价格相关
        //订单总额，叠加每一个订单项的总额信息
        for (OrderItemEntity orderItem : orderItemEntities) {
            //优惠价格信息
            coupon = coupon.add(orderItem.getCouponAmount());
            promotion = promotion.add(orderItem.getPromotionAmount());
            intergration = intergration.add(orderItem.getIntegrationAmount());

            //总价
            total = total.add(orderItem.getRealAmount());

            //积分信息和成长值信息
            integrationTotal += orderItem.getGiftIntegration();
            growthTotal += orderItem.getGiftGrowth();

        }
        //1、订单价格相关的
        orderEntity.setTotalAmount(total);
        //设置应付总额(总额+运费)
        orderEntity.setFreightAmount(new BigDecimal("6"));
        orderEntity.setPayAmount(total.add(orderEntity.getFreightAmount()));
        orderEntity.setCouponAmount(coupon);
        orderEntity.setPromotionAmount(promotion);
        orderEntity.setIntegrationAmount(intergration);

        //设置积分成长值信息
        orderEntity.setIntegration(integrationTotal);
        orderEntity.setGrowth(growthTotal);

        //设置删除状态(0-未删除，1-已删除)
        orderEntity.setDeleteStatus(0);

    }

    // 构建每一个订单项数据
    private OrderItemEntity buildOrderItem(OrderItemVo cartItem) {
        OrderItemEntity orderItemEntity = new OrderItemEntity();
        // 商品的spu信息
        R resp = productFeignService.getSpuInfoBySkuId(cartItem.getSkuId());
        SpuInfoEntityVo data = resp.getData("data", new TypeReference<SpuInfoEntityVo>() {
        });
        orderItemEntity.setSpuId(data.getId());
        orderItemEntity.setSpuBrand(data.getBrandId().toString());
        orderItemEntity.setSpuName(data.getSpuName());
        orderItemEntity.setCategoryId(data.getCategoryId());

        // 商品的sku信息
        orderItemEntity.setSkuId(cartItem.getSkuId());
        orderItemEntity.setSkuName(cartItem.getTitle());
        orderItemEntity.setSkuPic(cartItem.getImage());
        orderItemEntity.setSkuPrice(cartItem.getPrice());
        String s = StringUtils.collectionToDelimitedString(cartItem.getSkuAttrValues(), ";");
        orderItemEntity.setSkuAttrsVals(s);
        orderItemEntity.setSkuQuantity(cartItem.getCount());

        // 优惠信息，不做了

        // 积分信息
        orderItemEntity.setGiftGrowth(cartItem.getPrice().intValue());
        orderItemEntity.setGiftIntegration(cartItem.getPrice().intValue());

        // 订单项价格信息
        orderItemEntity.setPromotionAmount(new BigDecimal("0"));
        orderItemEntity.setCouponAmount(new BigDecimal("0"));
        orderItemEntity.setIntegrationAmount(new BigDecimal("0"));
        // 当前订单项的实际金额
        BigDecimal origin =
                orderItemEntity.getSkuPrice().multiply(BigDecimal.valueOf(orderItemEntity.getSkuQuantity()));
        BigDecimal subtract =
                origin.subtract(orderItemEntity.getCouponAmount()).subtract(orderItemEntity.getIntegrationAmount()).subtract(orderItemEntity.getPromotionAmount());
        orderItemEntity.setRealAmount(subtract);

        return orderItemEntity;
    }

}