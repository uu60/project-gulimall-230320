package com.octopus.gulimall.thirdparty;

import com.aliyun.oss.OSS;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@SpringBootTest
class GulimallThirdPartyApplicationTests {

	@Autowired
	private OSS ossClient;

	@Test
	void contextLoads() throws FileNotFoundException {
		InputStream input = new FileInputStream("/Users/dujianzhang/Downloads/accounts.json");
		ossClient.putObject("gulimall-230320", "accounts.json", input);
		ossClient.shutdown();
	}

}
