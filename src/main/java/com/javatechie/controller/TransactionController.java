package com.javatechie.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.*;
import com.javatechie.events.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;

import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

@Tag(name = "Transaction Management", description = "APIs for managing and monitoring financial transactions")
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);
    private final KafkaTemplate<String, Transaction> kafkaTemplate;
    private final ObjectMapper mapper = new ObjectMapper();
    private final List<String> fraudAlerts = new CopyOnWriteArrayList<>();

    public TransactionController(KafkaTemplate<String, Transaction> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

//    @PostMapping
//    public String sendTransaction() throws Exception {
//
//        for (int i = 0; i < 50; i++) {
//
//            String transactionId = "txn-" + System.currentTimeMillis() + "-" + i;
//            double amount = 8000 + new Random().nextDouble() * (11000 - 8000);
//
//            Transaction txn = new Transaction(
//                    transactionId,
//                    "USER_" + i,
//                    amount, LocalDateTime.now().toString());
//
//            //String txnJson = mapper.writeValueAsString(txn);
//
//            kafkaTemplate.send("transactions", transactionId, txn);
//        }
//
//        return "✅ Transaction sent to Kafka!";
//    }

    @Operation(summary = "Publish transactions from file", description = "Reads transaction data from transactions.json file and publishes all transactions to Kafka for fraud detection processing")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transactions published successfully", 
                   content = @Content(mediaType = "application/json", 
                   schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error when reading transactions file")
    })
    @PostMapping("/publish")
    public String publishTransaction() {
        List<Transaction> transactions = readTransactionsFromResource();

        for (Transaction txn : transactions) {
            kafkaTemplate.send("transactions", txn.transactionId(), txn);
        }
        return "✅ Published " + transactions.size() + " transactions to Kafka!";
    }


    private List<Transaction> readTransactionsFromResource() {
        // open the resource stream for /transactions.json from the classpath
        try (InputStream is = getClass().getResourceAsStream("/transactions.json")) {
            return mapper.readValue(is, new TypeReference<List<Transaction>>() {
            });
        } catch (Exception e) {
            // wrap any parsing errors and rethrow as a runtime exception
            throw new RuntimeException("Failed to parse transactions.json", e);
        }
    }

    @KafkaListener(topics = "txn-fraud-alert", groupId = "fraud-alert-consumer")
    public void consumeFraudAlert(String alertMessage) {
        log.info("Received fraud alert: {}", alertMessage);
        fraudAlerts.add(alertMessage);
        log.info("Total fraud alerts stored: {}", fraudAlerts.size());
        // Keep only last 100 alerts to prevent memory issues
        if (fraudAlerts.size() > 100) {
            fraudAlerts.removeFirst();
        }
    }

    @Operation(summary = "Get fraud alerts", description = "Retrieves all fraud alerts detected by the stream processing. Returns empty array if no fraud alerts are found.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of fraud alerts retrieved successfully", 
                   content = @Content(mediaType = "application/json", 
                   array = @ArraySchema(schema = @Schema(implementation = String.class)))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/fraud-alerts")
    public List<String> getFraudAlerts() {
        return new ArrayList<>(fraudAlerts);
    }
}
