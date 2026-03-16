package com.javatechie.validation.validators;

import com.javatechie.events.Transaction;
import com.javatechie.validation.TransactionValidator;
import com.javatechie.validation.ValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
public class TransactionTypeValidator implements TransactionValidator {
    
    private static final Set<String> VALID_TYPES = Set.of("debit", "credit");
    
    @Override
    public ValidationResult validate(Transaction transaction) {
        if (transaction.type() == null || !VALID_TYPES.contains(transaction.type().toLowerCase())) {
            log.error("Invalid transaction type: {} for transaction: {}", transaction.type(), transaction);
            return ValidationResult.invalid("Invalid transaction type: " + transaction.type());
        }
        return ValidationResult.valid();
    }
}
