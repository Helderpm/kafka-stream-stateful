package com.javatechie.validation.validators;

import com.javatechie.events.Transaction;
import com.javatechie.validation.TransactionValidator;
import com.javatechie.validation.ValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotNullValidator implements TransactionValidator {
    
    @Override
    public ValidationResult validate(Transaction transaction) {
        if (transaction == null) {
            log.error("Transaction is null");
            return ValidationResult.invalid("Transaction is null");
        }
        return ValidationResult.valid();
    }
}
