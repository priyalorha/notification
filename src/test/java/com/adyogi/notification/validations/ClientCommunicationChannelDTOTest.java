//package com.adyogi.notification.validations;
//
//import com.adyogi.notification.entities.ClientCommunicationChannelDTO;
//import org.junit.jupiter.api.Test;
//
//import javax.validation.Validation;
//import javax.validation.Validator;
//import javax.validation.ValidatorFactory;
//import javax.validation.ConstraintViolation;
//import java.util.List;
//import java.util.Set;
//
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//public class ClientCommunicationChannelDTOTest {
//
//    private final Validator validator;
//
//    public ClientCommunicationChannelDTOTest() {
//        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
//        this.validator = factory.getValidator();
//    }
//
//    @Test
//    public void testInvalidCommunicationConfiguration() {
//        ClientCommunicationChannelDTO.CommunicationConfiguration config =
//                new ClientCommunicationChannelDTO.CommunicationConfiguration();
//        config.setFromEmail("invalid-email");
//        config.setToEmail(List.of("valid@example.com", "invalid-email"));
//
//        Set<ConstraintViolation<ClientCommunicationChannelDTO.CommunicationConfiguration>> violations = validator.validate(config);
//        assertFalse(violations.isEmpty(), "Expected validation errors");
//    }
//
//    @Test
//    public void testvalidCommunicationConfiguration() {
//        ClientCommunicationChannelDTO.CommunicationConfiguration config =
//                new ClientCommunicationChannelDTO.CommunicationConfiguration();
//        config.setFromEmail("valid@example.com");
//        config.setToEmail(List.of("valid@example.com"));
//
//        Set<ConstraintViolation<ClientCommunicationChannelDTO.CommunicationConfiguration>> violations = validator.validate(config);
//        assertTrue(violations.isEmpty());
//    }
//}