package com.adyogi.notification.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

public class EnsureListValidator implements ConstraintValidator<EnsureList, Object> {
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        // Check if the value is null (nullable fields can be allowed)
        if (value == null) {
            return true;
        }
        // Validate if the object is an instance of List
        return value instanceof List;
    }
}

