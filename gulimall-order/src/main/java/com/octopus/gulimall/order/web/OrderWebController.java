package com.octopus.gulimall.order.web;

import com.alibaba.fastjson.JSON;
import com.octopus.gulimall.order.service.OrderService;
import com.octopus.gulimall.order.vo.OrderConfirmVo;
import com.octopus.gulimall.order.vo.OrderSubmitVo;
import com.octopus.gulimall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

/**
 * @author octopus
 * @date 2023/4/8 16:05
 */
@Controller
public class OrderWebController {
    @Autowired
    OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model) {
        OrderConfirmVo orderConfirmVo = orderService.confirmOrder();
        model.addAttribute("confirmOrderData", orderConfirmVo);

        return "confirm";
    }

    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo vo, Model model, RedirectAttributes redirectAttributes) {
        SubmitOrderResponseVo responseVo = orderService.submitOrder(vo);

        // 下单：创建订单，验令牌，验价格，锁库存

        // 下单成功去支付页
        if (responseVo.getCode() == 0) {
            model.addAttribute("order", responseVo.getOrder());
            return "pay";
        } else {
            String msg = "下单失败；";
            switch (responseVo.getCode()) {
                case 1: msg += "订单信息过期，请刷新再次提交。"; break;
                case 2: msg += "订单商品价格发生变化，请确认后再次提交。"; break;
                case 3: msg += "库存锁定失败，商品库存不足。"; break;
            }
            redirectAttributes.addFlashAttribute("msg", msg);
            return "redirect:http://order.gulimall.com/toTrade";
        }
    }
}
