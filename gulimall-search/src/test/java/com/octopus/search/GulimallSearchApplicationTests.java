package com.octopus.search;


import com.alibaba.fastjson.JSON;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
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

	@Test
	void searchData() throws IOException {
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.indices("bank");
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		// 构造检索条件
		/*
			{
				"query": {
					"match": {
						"address": "mill"
					}
				}
			}
		 */
		searchSourceBuilder.query(QueryBuilders.matchQuery("address", "mill"))
				/*
                    {
                        "query": {
                            "match": {
                                "address": "mill"
                            }
                        },
                        "aggs": {
                            "ageAgg": {
                                "terms": {
                                    "field": "age",
                                    "size": 10
                                }
                            }
                        }
                    }
                 */
				.aggregation(AggregationBuilders.terms("ageAgg").field("age").size(10))
				/*
                    {
                        "query": {
                            "match": {
                                "address": "mill"
                            }
                        },
                        "aggs": {
                            "ageAgg": {
                                "terms": {
                                    "field": "age",
                                    "size": 10
                                }
                            },
                            "balanceAvg": {
                            	"avg": {
                            		"field": "balance"
                            	}
                            }
                        }
                    }
                 */
				.aggregation(AggregationBuilders.avg("balanceAvg").field("balance"))
				/*
                    {
                        "query": {
                            "match": {
                                "address": "mill"
                            }
                        },
                        "aggs": {
                            "ageAgg": {
                                "terms": {
                                    "field": "age",
                                    "size": 10
                                }
                            },
                            "balanceAvg": {
                            	"avg": {
                            		"field": "balance"
                            	}
                            }
                        },
                        "size": 0
                    }
                 */
				.size(2);

		searchRequest.source(searchSourceBuilder);
		System.out.println(searchSourceBuilder);
		// 执行操作
		SearchResponse response = esTemplate.search(searchRequest, COMMON_OPTIONS);

		/*
		"hits" : {
			"total" : {
			  "value" : 4,
			  "relation" : "eq"
			},
			"max_score" : 5.4032025,
			"hits" : [{
				"_source": {
					"account_number": ..,
					"address": "mill .."
				}
			}, ..]
		},
		"aggregations": {
			"ageAgg": {
				..,
				"buckets": [
					{
						"key": 38,
						"doc_count": 2
					},
					..
				]
			},
			"balanceAvg": {
				"value": 25108.0
			}
		}

		 */
		SearchHit[] hits = response.getHits().getHits();
		for (SearchHit hit : hits) {
			String str = hit.getSourceAsString();
			System.out.println(str);
		}
		Terms ageAgg = response.getAggregations().get("ageAgg");
		for (Terms.Bucket bucket : ageAgg.getBuckets()) {
			System.out.println(bucket.getKey() + ": " + bucket.getDocCount());
		}
	}

	class User {
		private String userName;
		private String gender;
		private Integer age;
	}

}
