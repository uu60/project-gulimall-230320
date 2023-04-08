package com.octopus.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**
 * @author octopus
 * @date 2023/4/8 20:44
 */
@Data
public class WareSkuLockVo {
    private String orderSn;

    /** 需要锁住的所有库存信息 **/
    private List<OrderItemVo> locks;
}
