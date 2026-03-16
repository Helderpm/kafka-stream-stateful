package com.javatechie.validation.validators;

import com.javatechie.events.Transaction;
import com.javatechie.validation.TransactionValidator;
import com.javatechie.validation.ValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AmountValidator implements TransactionValidator {
    
    @Override
    public ValidationResult validate(Transaction transaction) {
        if (transaction.amount() <= 0) {
            log.error("Invalid amount: {} for transaction: {}", transaction.amount(), transaction);
            return ValidationResult.invalid("Invalid amount: " + transaction.amount());
        }
        return ValidationResult.valid();
    }
}
