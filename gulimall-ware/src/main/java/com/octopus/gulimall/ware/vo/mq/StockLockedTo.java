package com.octopus.gulimall.ware.vo.mq;

import lombok.Data;

import java.util.List;

/**
 * @author octopus
 * @date 2023/4/9 17:12
 */
@Data
public class StockLockedTo {

    private Long id; // 库存工作单id
    private Long detailId; // 每锁一个就发一条
}
