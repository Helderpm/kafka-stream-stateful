package com.javatechie.validation.validators;

import com.javatechie.events.Transaction;
import com.javatechie.validation.TransactionValidator;
import com.javatechie.validation.ValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserIdValidator implements TransactionValidator {
    
    @Override
    public ValidationResult validate(Transaction transaction) {
        if (transaction.userId() == null || transaction.userId().trim().isEmpty()) {
            log.error("User ID is null or empty for transaction: {}", transaction);
            return ValidationResult.invalid("User ID is null or empty");
        }
        return ValidationResult.valid();
    }
}
