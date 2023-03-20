package com.octopus.search;


import com.alibaba.fastjson.JSON;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

import static com.octopus.search.config.GulimallElasticSearchConfig.COMMON_OPTIONS;

@SpringBootTest
class GulimallSearchApplicationTests {

	@Autowired
	private RestHighLevelClient esTemplate;

	@Test
	void contextLoads() {
		System.out.println(esTemplate);
	}

	@Test
	void indexData() throws IOException {
		IndexRequest indexRequest = new IndexRequest("users");
		indexRequest.id("1").source(
				"userName", "zhangsan",
				"age", 18,
				"gender", "男"
		);

		User user = new User();
		String jsonString = JSON.toJSONString(user);
		indexRequest.source(jsonString, XContentType.JSON);

		IndexResponse response = esTemplate.index(indexRequest, COMMON_OPTIONS);
		System.out.println(response);
	}

	class User {
		private String userName;
		private String gender;
		private Integer age;
	}

}
