package com.javatechie.validation;

import com.javatechie.events.Transaction;
import com.javatechie.validation.validators.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TransactionValidationService {
    
    private final TransactionValidator transactionValidator;
    
    public TransactionValidationService(NotNullValidator notNullValidator,
                                      TransactionIdValidator transactionIdValidator,
                                      UserIdValidator userIdValidator,
                                      AmountValidator amountValidator,
                                      TransactionTypeValidator transactionTypeValidator) {
        
        this.transactionValidator = notNullValidator
                .and(transactionIdValidator)
                .and(userIdValidator)
                .and(amountValidator)
                .and(transactionTypeValidator);
    }
    
    public boolean isValid(Transaction transaction) {
        try {
            ValidationResult result = transactionValidator.validate(transaction);
            if (!result.isValid()) {
                log.error("Transaction validation failed: {} - Transaction: {}", 
                         result.errorMessage(), transaction);
            }
            return result.isValid();
        } catch (Exception e) {
            log.error("Error during transaction validation: {}", transaction, e);
            return false;
        }
    }
}
