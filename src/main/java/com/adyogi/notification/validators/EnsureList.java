package com.adyogi.notification.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.adyogi.notification.utils.constants.ValidationConstants.MUST_BE_LIST;

@Constraint(validatedBy = EnsureListValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnsureList {
    String message() default MUST_BE_LIST;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
