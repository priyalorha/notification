package com.adyogi.notification.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;

public class EnumValidator implements ConstraintValidator<EnumValidation, String> {

    private String[] enumValues;

    @Override
    public void initialize(EnumValidation constraintAnnotation) {
        // Extract all enum constants as strings
        enumValues = Arrays.stream(constraintAnnotation.enumClass().getEnumConstants())
                .map(Enum::name)
                .toArray(String[]::new);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Use @NotNull for null checks
        }
        return Arrays.asList(enumValues).contains(value);
    }
}
