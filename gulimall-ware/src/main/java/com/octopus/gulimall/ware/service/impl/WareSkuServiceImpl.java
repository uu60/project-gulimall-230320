package com.octopus.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.octopus.common.to.SkuHasStockVo;
import com.octopus.common.utils.PageUtils;
import com.octopus.common.utils.Query;
import com.octopus.common.utils.R;
import com.octopus.gulimall.ware.dao.WareSkuDao;
import com.octopus.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.octopus.gulimall.ware.entity.WareOrderTaskEntity;
import com.octopus.gulimall.ware.entity.WareSkuEntity;
import com.octopus.gulimall.ware.feign.OrderFeignService;
import com.octopus.gulimall.ware.feign.ProductFeignService;
import com.octopus.gulimall.ware.service.WareOrderTaskDetailService;
import com.octopus.gulimall.ware.service.WareOrderTaskService;
import com.octopus.gulimall.ware.service.WareSkuService;
import com.octopus.gulimall.ware.vo.OrderEntityVo;
import com.octopus.gulimall.ware.vo.OrderItemVo;
import com.octopus.gulimall.ware.vo.WareSkuLockVo;
import com.octopus.gulimall.ware.vo.mq.StockLockedTo;
import com.rabbitmq.client.Channel;
import lombok.Data;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("wareSkuService")
@RabbitListener(queues = "stock.release.stock.queue")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private ProductFeignService productFeignService;
    @Autowired
    private WareOrderTaskService wareOrderTaskService;
    @Autowired
    private WareOrderTaskDetailService wareOrderTaskDetailService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private OrderFeignService orderFeignService;

    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo to, Message message, Channel channel) throws IOException {
        Long id = to.getId(); // 库存工作单的id
        Long detailId = to.getDetailId();
        // 解锁
        WareOrderTaskDetailEntity detail = wareOrderTaskDetailService.getById(detailId);
        if (detail != null) {
            // 证明库存锁定成功了
            // 还需要查询订单情况，如果是订单未取消不能解锁
            WareOrderTaskEntity task = wareOrderTaskService.getById(detail.getTaskId());
            String orderSn = task.getOrderSn();
            R resp = orderFeignService.getOrderStatus(orderSn);
            if (resp.getCode() == 0) {
                OrderEntityVo data = resp.getData("data", new TypeReference<OrderEntityVo>() {
                });
                // 取消状态
                if (data.getStatus() == 4) {
                    // 解锁库存
                    unlockStock(detail.getSkuId(), detail.getWareId(), detail.getSkuNum(), detail.getId());
                    channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                } else {
                    // 放回队列继续消费
                    channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
                }
            } else {
                // 放回队列继续消费
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
            }
        } else {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }

    private void unlockStock(Long skuId, Long wareId, Integer num, Long taskDetailId) {
        baseMapper.unlockStock(skuId, wareId, num);
    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareSkuEntity> wrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (StringUtils.hasText(skuId)) {
            wrapper.eq("sku_id", skuId);
        }

        String wareId = (String) params.get("wareId");
        if (StringUtils.hasText(wareId)) {
            wrapper.eq("ware_id", wareId);
        }
        IPage<WareSkuEntity> page = this.page(new Query<WareSkuEntity>().getPage(params), wrapper);

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void addStock(Long skuId, Long wareId, Integer skuNum) {
        // 判断如果还没有这个库存记录新增
        List<WareSkuEntity> list = list(new QueryWrapper<WareSkuEntity>().eq("sku_id", skuId).eq("ware_id", wareId));
        if (list.isEmpty()) {
            WareSkuEntity wareSkuEntity = new WareSkuEntity();
            wareSkuEntity.setSkuId(skuId);
            wareSkuEntity.setWareId(wareId);
            wareSkuEntity.setStock(skuNum);
            wareSkuEntity.setStockLocked(0);
            // 远程查询sku名字
            try {
                R info = productFeignService.info(skuId);
                Map<String, Object> skuInfo = (Map<String, Object>) info.get("skuInfo");
                if (info.getCode() == 0) {
                    wareSkuEntity.setSkuName((String) skuInfo.get("skuName"));
                }
            } catch (Exception e) {
            }
            this.baseMapper.insert(wareSkuEntity);
            return;
        }
        this.baseMapper.addStock(skuId, wareId, skuNum);
    }

    @Override
    public List<SkuHasStockVo> getSkusHasStock(List<Long> skuIds) {
        return skuIds.stream().map(skuId -> {
            SkuHasStockVo skuHasStockVo = new SkuHasStockVo();
            // 查询当前sku的总库存量
            Long count = baseMapper.getSkuStock(skuId);

            skuHasStockVo.setHasStock(count != null && count > 0);
            skuHasStockVo.setSkuId(skuId);
            return skuHasStockVo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean orderLockStock(WareSkuLockVo vo) {
        // 保存工作单详情
        WareOrderTaskEntity taskEntity = new WareOrderTaskEntity();
        taskEntity.setOrderSn(vo.getOrderSn());
        taskEntity.setCreateTime(new Date());
        wareOrderTaskService.save(taskEntity);

        // 1. 按照下单的收货地址，找到一个就近仓库，锁定库存

        // 找到每个商品在哪个仓库都有库存
        List<OrderItemVo> locks = vo.getLocks();

        List<SkuWareHasStock> collect = locks.stream().map(item -> {
            SkuWareHasStock stock = new SkuWareHasStock();
            Long skuId = item.getSkuId();
            stock.setSkuId(skuId);
            stock.setNum(item.getCount());
            // 查询这个商品在哪有库存
            List<Long> wareIds = baseMapper.listWareIdsHasSkuStock(skuId);
            stock.setWareId(wareIds);
            return stock;
        }).collect(Collectors.toList());

        // 2. 锁定库存
        for (SkuWareHasStock hasStock : collect) {
            Boolean skuStocked = false;
            Long skuId = hasStock.getSkuId();
            List<Long> wareIds = hasStock.getWareId();
            if (wareIds == null || wareIds.size() == 0) {
                throw new RuntimeException("no stock for skuID: " + skuId);
            }
            for (Long wareId : wareIds) {
                Long count = baseMapper.lockSkuStock(skuId, wareId, hasStock.getNum());
                // 锁失败了
                if (count == 1) {
                    skuStocked = true;
                    WareOrderTaskDetailEntity detailEntity = new WareOrderTaskDetailEntity();
                    detailEntity.setSkuId(skuId);
                    detailEntity.setSkuName("");
                    detailEntity.setSkuNum(hasStock.num);
                    detailEntity.setTaskId(taskEntity.getId());
                    detailEntity.setWareId(wareId);
                    // 表示锁定
                    detailEntity.setLockStatus(1);
                    wareOrderTaskDetailService.save(detailEntity);

                    // 通知MQ锁定库存成功
                    StockLockedTo stockLockedTo = new StockLockedTo();
                    stockLockedTo.setId(taskEntity.getId());
                    stockLockedTo.setDetailId(detailEntity.getId());
                    // 如果回滚，即使发送了消息，但数据库中不存在对应的id
                    rabbitTemplate.convertAndSend("stock-event-exchange", "stock.locked", stockLockedTo);
                    break;
                } else {
                    // 当前仓库锁失败，重试下一个仓库

                }
            }
            if (!skuStocked) {
                // 当前商品所有仓库都没有锁住
                throw new RuntimeException("no stock for skuID: " + skuId);
            }
        }

        // 到这肯定是都锁定成功了
        return true;
    }

    @Data
    class SkuWareHasStock {
        private Long skuId;
        private Integer num;
        private List<Long> wareId;
    }

}