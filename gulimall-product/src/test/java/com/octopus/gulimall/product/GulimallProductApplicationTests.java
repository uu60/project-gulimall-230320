package com.octopus.gulimall.product;

import com.octopus.gulimall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GulimallProductApplicationTests {

	@Autowired
	BrandService brandService;

	@Test
	void contextLoads() {
//		BrandEntity brandEntity = new BrandEntity();
//
//		brandEntity.setName("中文测试");
//		brandService.save(brandEntity);
//		System.out.println("successful");

	}

}
