package com.adyogi.notification.services;

import com.adyogi.notification.dto.emails.EmailNotificationDTO;
import com.adyogi.notification.utils.constants.ConfigConstants;
import com.adyogi.notification.utils.logging.LogUtil;
import com.adyogi.notification.utils.rollbar.RollbarManager;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class PubSubEmailSubscriber {

    EmailNotificationDTO emailNotificationDTO;

    private  String sendGridApiKey = ConfigConstants.SENDGRID_KEY;

    Logger logger= LogUtil.getInstance();


    public void sendEmail(EmailNotificationDTO emailNotificationDTO) throws IOException {
        // Initialize the SendGrid Email API
        Email from = new Email(emailNotificationDTO.getFromEmail()); // Assuming the `fromEmail` is injected properly from the Spring context

        // Iterate over each recipient
        for (String recipient : emailNotificationDTO.getRecipients()) {
            Email to = new Email(recipient);


            // Sanitize the HTML content, prevent <script> tags and other malicious content
            String sanitizedHtml = Jsoup.clean( emailNotificationDTO.getBody(), Safelist.relaxed());
            Content emailContent = new Content("text/html", sanitizedHtml); // Use text/html for HTML content
            // Use text/html for HTML content
            Mail mail = new Mail(from, emailNotificationDTO.getSubject(), to, emailContent);
            // Create a SendGrid API client
            SendGrid sg = new SendGrid(sendGridApiKey);
            // Set up the request for sending the email
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            try {
                // Send the email via the SendGrid API
                Response response = sg.api(request);

                if (response.getStatusCode() != 202) {
                    RollbarManager.sendExceptionOnRollBar("Failed to send email to " + recipient + " with status: " +
                            response.getStatusCode(),
                            new IOException("Failed to send email to " + recipient + " with status: " + response.getStatusCode()));
                    throw new IOException("Failed to send email to " + recipient + " with status: " + response.getStatusCode());
                }
            } catch (IOException ex) {
                logger.error("Failed to send email to " + recipient + " with exception: " + ex.getMessage());
            }
        }
    }
}
