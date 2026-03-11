package com.javatechie;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, 
               brokerProperties = { "listeners=PLAINTEXT://localhost:9092", 
                                   "port=9092" },
               topics = { "transactions", "user-txn-counts" })
@DirtiesContext
class FraudDetectionSystemApplicationTests {

	@Test
	void contextLoads() {
	}

}
