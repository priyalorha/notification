package com.adyogi.notification.exceptions;

import com.adyogi.notification.utils.logging.LogUtil;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.apache.logging.log4j.Logger;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.transaction.RollbackException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Arrays;

import static com.adyogi.notification.utils.constants.ErrorConstants.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    Logger logger = LogUtil.getInstance();

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<Map<String, String>> handleServiceException(ServiceException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);  // Or other appropriate status
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, List<String>>> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors()
                .stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.toList());
        return new ResponseEntity<>(getErrorsMap(errors), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ClientValidationException.class)
    public ResponseEntity<Map<String, List<String>>> handleClientValidationException(ClientValidationException ex) {
        List<String> errors = Collections.singletonList(ex.getMessage());
        return new ResponseEntity<>(getErrorsMap(errors), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, List<String>>> handleNotFoundException(NotFoundException ex) {
        List<String> errors = Collections.singletonList(ex.getMessage());
        return new ResponseEntity<>(getErrorsMap(errors), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidFormatException(InvalidFormatException ex) {
        Map<String, Object> response = new HashMap<>();

        // Extracting the error details
        List<Map<String, String>> errors = ex.getPath().stream().map(ref -> {
            Map<String, String> errorDetails = new HashMap<>();
            String fieldName = ref.getFieldName(); // The field where the error occurred
            errorDetails.put("field", fieldName);

            if (ex.getTargetType().isEnum()) {
                // If the target type is an Enum, list all possible values
                Object[] enumConstants = ex.getTargetType().getEnumConstants();
                List<String> allowedValues = Arrays.stream(enumConstants)
                        .map(Object::toString)
                        .collect(Collectors.toList());
                errorDetails.put("message", String.format(INVALID_ENUM_VALUE, ex.getValue(), fieldName, allowedValues));
            } else {
                // Generic error for non-enum types
                errorDetails.put("message", String.format(INVALID_VALUE_FORMAT, ex.getValue(), fieldName, ex.getTargetType().getSimpleName()));
            }
            return errorDetails;
        }).collect(Collectors.toList());

        // Add the detailed errors to the response
        response.put("errors", errors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause(); // Get the underlying cause of the exception

        if (cause instanceof InvalidFormatException) {
            // Handle InvalidFormatException specifically
            return handleInvalidFormatException((InvalidFormatException) cause);
        }

        // Handle other cases of HttpMessageNotReadableException
        Map<String, Object> response = new HashMap<>();
        response.put("errors", Collections.singletonList(ex.getCause().getMessage()));
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<Map<String, List<String>>> handleGeneralExceptions(Exception ex) {
        List<String> errors = Collections.singletonList(ex.getMessage());
        logger.error(ex.getStackTrace());
        logger.error(ex.getMessage());
//        return new ResponseEntity<>(ex.getStackTrace(), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(getErrorsMap(errors), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(javax.persistence.RollbackException.class)

    public final ResponseEntity<Map<String, List<String>>> handleRollbarException(Exception ex) {
        List<String> errors = Collections.singletonList(ex.getMessage());
        logger.error(ex.getStackTrace());
        logger.error(ex.getMessage());
        logger.error(ex.getCause());
//        return new ResponseEntity<>(ex.getStackTrace(), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(getErrorsMap(errors), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Map<String, List<String>> getErrorsMap(List<String> errors) {
        Map<String, List<String>> errorResponse = new HashMap<>();
        errorResponse.put("errors", errors);
        return errorResponse;
    }
}
