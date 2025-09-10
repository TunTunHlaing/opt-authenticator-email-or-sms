package org.example.keycloak.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.mail.MessagingException;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OTPService {

    private final Logger logger = LoggerFactory.getLogger(OTPService.class);

    private String twilioAccountSid;
    private String twilioAuthToken;
    private String twilioPhoneNumber;

    private SimpleMailService simpleMailService;

    public OTPService(AuthenticatorConfigModel config) {
        this.twilioAccountSid = config.getConfig().get("twilio_account_sid");
        this.twilioAuthToken = config.getConfig().get("twilio_auth_token");
        this.twilioPhoneNumber = config.getConfig().get("twilio_phone_number");

        String smtpHost = config.getConfig().get("email_host");
        int smtpPort = Integer.parseInt(config.getConfig().get("email_port"));
        String smtpUsername = config.getConfig().get("email_username");
        String smtpPassword = config.getConfig().get("email_password");
        String fromEmail = config.getConfig().get("email_from");

        this.simpleMailService = new SimpleMailService(smtpHost, smtpPort, smtpUsername, smtpPassword, fromEmail);
    }

    public String generateOTP() {
        return String.valueOf((int) (Math.random() * 1000000)); }

    public void sendEmailOTP(String email, String otp) throws MessagingException {
        simpleMailService.send(email, otp);
    }


    public void sendSMSOTP(String phoneNumber, String otp) {
        try {
            Twilio.init(twilioAccountSid, twilioAuthToken);
            Message.creator(
                            new PhoneNumber(phoneNumber),
                            new PhoneNumber(twilioPhoneNumber),
                            "Your OTP is: " + otp)
                    .create();

            logger.info("SMS OTP sent successfully to {}" , phoneNumber);
        } catch (Exception e) {
            logger.error("Failed to send SMS OTP to " + phoneNumber + ": " + e.getMessage());
            throw new RuntimeException("SMS sending failed", e);
        }
    }

    public void storeOTP(String otp, KeycloakSession session) {
        session.getContext().getRealm().setAttribute("otp_" + otp, otp);
        session.getContext().getRealm().setAttribute("otp_expiry_" + otp, System.currentTimeMillis() + 300000);
    }

    public boolean validateOTP(String otp, KeycloakSession session) {
        String storedOtp = session.getContext().getRealm().getAttribute("otp_" + otp);
        Long expiryTime = Long.valueOf(session.getContext().getRealm().getAttribute("otp_expiry_" + otp));
        return storedOtp != null && System.currentTimeMillis() < expiryTime;
    }
}
