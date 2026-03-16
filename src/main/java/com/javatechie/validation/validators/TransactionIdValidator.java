package com.javatechie.validation.validators;

import com.javatechie.events.Transaction;
import com.javatechie.validation.TransactionValidator;
import com.javatechie.validation.ValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TransactionIdValidator implements TransactionValidator {
    
    @Override
    public ValidationResult validate(Transaction transaction) {
        if (transaction.transactionId() == null || transaction.transactionId().trim().isEmpty()) {
            log.error("Transaction ID is null or empty for transaction: {}", transaction);
            return ValidationResult.invalid("Transaction ID is null or empty");
        }
        return ValidationResult.valid();
    }
}
