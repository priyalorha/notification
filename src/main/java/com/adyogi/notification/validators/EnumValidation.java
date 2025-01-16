package com.adyogi.notification.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.adyogi.notification.utils.constants.ErrorConstants.INVALID_ENUM_VALUES;

@Constraint(validatedBy = EnumValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumValidation {
    Class<? extends Enum<?>> enumClass();
    String message() default INVALID_ENUM_VALUES;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

