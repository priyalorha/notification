package com.adyogi.notification.services;

import com.adyogi.notification.dto.emails.EmailNotificationDTO;
import com.adyogi.notification.exceptions.ServiceException;
import com.adyogi.notification.utils.FailureHandler;
import com.adyogi.notification.utils.logging.LogUtil;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

import static com.adyogi.notification.utils.constants.ConfigConstants.*;
import static com.adyogi.notification.utils.constants.ErrorConstants.INVALID_EMAIL_DTO;
import static com.adyogi.notification.utils.constants.ErrorConstants.SENDGRID_FAILURE;

@Service
public class EmailSubscriber {

    @Value(SENDGRID_API_KEY)
    private String sendGridApiKey;

    @Autowired
    FailureHandler failureHandler;

    Logger logger = LogUtil.getInstance();


    public void sendEmail(EmailNotificationDTO emailNotificationDTO) throws IOException {
        if (emailNotificationDTO.getFromEmail() == null ||
                emailNotificationDTO.getRecipients() == null ||
                emailNotificationDTO.getRecipients().isEmpty()) {
            throw new IOException(INVALID_EMAIL_DTO);
        }
        Email from = new Email(emailNotificationDTO.getFromEmail());
        List<String> recipients = emailNotificationDTO.getRecipients();
        String sanitizedHtml = Jsoup.clean(emailNotificationDTO.getBody(), Safelist.relaxed());
        Content emailContent = new Content(SENDGRID_EMAIL_FORMAT, sanitizedHtml);
        Mail mail = new Mail();
        mail.setFrom(from);
        mail.setSubject(emailNotificationDTO.getSubject());
        mail.addContent(emailContent);
        Personalization personalization = new Personalization();
        recipients.forEach(email -> personalization.addTo(new Email(email)));
        mail.addPersonalization(personalization);
        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint(SENDGRID_ENDPOINT);
            request.setBody(mail.build());
            Response response = sg.api(request);
            if (response.getStatusCode() != 202) {
                logger.error(SENDGRID_FAILURE + response.getStatusCode());
                throw new ServiceException(SENDGRID_FAILURE + response.getStatusCode());
            }
        } catch (IOException e) {
            failureHandler.handleFailure(INVALID_EMAIL_DTO, e);
        } catch (ServiceException e) {
            failureHandler.handleFailure(SENDGRID_FAILURE, e);
        }
    }


}
