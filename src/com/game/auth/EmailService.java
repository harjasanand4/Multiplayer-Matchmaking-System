package com.game.auth;
import com.sendgrid.*;
import java.io.IOException;
/**
 * This class acts as email delivery system with using sendGrid API.
 * This class will be used to deliver the email to teh customer for various purpose.
 * @author: Maneet Singh,
 * @email: maneet.singh1@ucalgary.ca,
 */
public class EmailService {
    private static final String sendgrid_api = "YOUR_SENDGRID_KEY";
    private static SendGrid sendGridClient;
    // email address for our platform, it can receive the emails as well
    static Email from = new Email("omggamingplatform@outlook.com");
    static{
        sendGridClient =  new SendGrid(sendgrid_api);
    }
    /**
     * This method will send the email that contain one time password
     * portion of code for this method is taken from https://app.sendgrid.com/
     *
     * @param toEmail One time password receiver email address
     * @param otp One time password that needs to be sent to the user
     */
    public static void sendOtpEmail(String toEmail, String otp) {
        // Using a template set in sendGrid specifically set up for OTP emails
        String templateId = "d-e7ea350ed75d461eb4d653c7ab983c60";
        Mail mail = new Mail();
        mail.setFrom(from);
        mail.setTemplateId(templateId);

        Personalization personalization = new Personalization();
        personalization.addTo(new Email(toEmail));
        personalization.addDynamicTemplateData("OTP", otp);
        mail.addPersonalization(personalization);

        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sendGridClient.api(request);
            System.out.println("OTP email sent: " + response.getStatusCode());
        } catch (IOException ex) {
            System.out.println("Issue in delivering email to the user");
        }
    }

    /**
     * This method will send the welcome email to user after completing the registration process
     * @param toEmail to send welcome email
     */
    public static void welcomeEmail(String toEmail) {
        // Using a template specifically set up for welcome emails

        String templateId = "d-b68a4629e80543af9d275d05f60edb5d";
        Mail mail = new Mail();
        mail.setFrom(from);
        mail.setTemplateId(templateId);

        Personalization personalization = new Personalization();
        personalization.addTo(new Email(toEmail));
        mail.addPersonalization(personalization);

        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sendGridClient.api(request);
            System.out.println("Welcome email sent to "+ toEmail +": " + response.getStatusCode());
        } catch (IOException ex) {
            System.out.println("Issue in delivering email to the user");
        }
    }
}

