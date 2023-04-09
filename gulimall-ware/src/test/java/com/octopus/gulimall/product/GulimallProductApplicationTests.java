package com.octopus.gulimall.product;

import com.octopus.common.utils.R;
import com.octopus.gulimall.ware.GulimallWareApplication;
import com.octopus.gulimall.ware.feign.OrderFeignService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = GulimallWareApplication.class)
class GulimallProductApplicationTests {

	@Autowired
	OrderFeignService orderFeignService;

	@Test
	void contextLoads() {
		R r = orderFeignService.getOrderStatus("202304091849047381645015969648144385");
		System.out.println(r);
	}

}
