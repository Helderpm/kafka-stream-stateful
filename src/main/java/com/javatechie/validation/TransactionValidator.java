package com.javatechie.validation;

import com.javatechie.events.Transaction;

@FunctionalInterface
public interface TransactionValidator {
    
    ValidationResult validate(Transaction transaction);
    
    default TransactionValidator and(TransactionValidator other) {
        return (transaction) -> {
            ValidationResult firstResult = this.validate(transaction);
            if (!firstResult.isValid()) {
                return firstResult;
            }
            return other.validate(transaction);
        };
    }
    
    default TransactionValidator or(TransactionValidator other) {
        return (transaction) -> {
            ValidationResult firstResult = this.validate(transaction);
            if (firstResult.isValid()) {
                return firstResult;
            }
            return other.validate(transaction);
        };
    }
}
