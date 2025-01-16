package com.adyogi.notification.dto.emails;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailNotificationDTO {
    private List<String> recipients;
    private String subject;
    private String body;
    private String fromEmail;
}
