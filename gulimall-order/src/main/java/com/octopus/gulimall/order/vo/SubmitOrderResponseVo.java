package com.octopus.gulimall.order.vo;

import com.octopus.gulimall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @author octopus
 * @date 2023/4/8 19:32
 */
@Data
public class SubmitOrderResponseVo {
    private OrderEntity order;
    /** 错误状态码 0成功**/
    private Integer code;
}
