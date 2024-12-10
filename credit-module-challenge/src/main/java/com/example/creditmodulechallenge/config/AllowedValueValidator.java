package com.example.creditmodulechallenge.config;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AllowedValueValidator implements ConstraintValidator<AllowedValue, Integer> {

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null) return true; // Null kontrolü yapılır.
        return value==3 || value == 6 || value == 9 || value == 12 || value == 24;
    }
}
