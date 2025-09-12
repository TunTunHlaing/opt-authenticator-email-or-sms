package org.example.keycloak.authenticator;

import jakarta.mail.MessagingException;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.example.keycloak.UsernameValidatorConstant;
import org.example.keycloak.service.OTPService;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OtpSenderAuthenticator implements Authenticator {

    private static final Logger logger = LoggerFactory.getLogger(OtpSenderAuthenticator.class);
    private OTPService otpService;

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        otpService = new OTPService(context.getAuthenticatorConfig());

        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String inputUsername = formData.getFirst("username");
        logger.info("Input Username: " + inputUsername);

        if (inputUsername == null || inputUsername.isEmpty()) {
            logger.warn("No username provided");
            context.failure(AuthenticationFlowError.INVALID_USER);
            return;
        }

        if (!inputUsername.matches(UsernameValidatorConstant.Email.getPattern()) &&
                !inputUsername.matches(UsernameValidatorConstant.Phone.getPattern())) {
            logger.warn("Invalid username format: " + inputUsername);
            context.failure(AuthenticationFlowError.UNKNOWN_USER);
            return;
        }

        UserModel user = context.getSession().users().getUserByUsername(context.getRealm(), inputUsername);
        if (user == null) {
            logger.info("User not found, creating new user: " + inputUsername);
            user = context.getSession().users().addUser(context.getRealm(), inputUsername);
            user.setEnabled(true);
            context.setUser(user);
        } else {
            if (!user.isEnabled()) {
                logger.warn("User is disabled: " + inputUsername);
                user.setEnabled(true);
                logger.info("Enabled user: " + inputUsername);

            }
            context.setUser(user);
        }

        String username = user.getUsername();
        if (extracted(context, username, user)) return;

        logger.info("Creating OTP input form");
        context.challenge(createOTPInputForm(context));
    }

    private boolean extracted(AuthenticationFlowContext context, String username, UserModel user) {
        if (username.matches(UsernameValidatorConstant.Email.getPattern())) {
            logger.info("Username is Email");
            String otp = otpService.generateOTP();
            otpService.storeOTP(otp, context.getSession());
            try {
                otpService.sendEmailOTP(user.getUsername(), otp);
            } catch (MessagingException e) {
                logger.error("Failed to send email OTP", e);
                context.failure(AuthenticationFlowError.INTERNAL_ERROR);
                return true;
            }
        } else if (username.matches(UsernameValidatorConstant.Phone.getPattern())) {
            String otp = otpService.generateOTP();
            otpService.storeOTP(otp, context.getSession());
            otpService.sendSMSOTP(user.getUsername(), otp);
            logger.info("Username is Phone");
        } else {
            logger.warn("Invalid username format after validation: " + username);
            context.failure(AuthenticationFlowError.INVALID_USER);
            return true;
        }
        return false;
    }

    private Response createOTPInputForm(AuthenticationFlowContext context) {
        return context.form()
                .setAttribute("realm", context.getRealm())
                .setAttribute("user", context.getUser())
                .createForm("otp-input.ftl");
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        if (otpService == null) {
            otpService = new OTPService(context.getAuthenticatorConfig());
        }

        String inputOtp = context.getHttpRequest().getDecodedFormParameters().getFirst("otp");
        if (inputOtp == null || inputOtp.isEmpty()) {
            logger.warn("No OTP provided");
            context.failure(AuthenticationFlowError.INVALID_CREDENTIALS);
            return;
        }

        if (otpService.validateOTP(inputOtp, context.getSession())) {
            logger.info("OTP validation successful for user: " + context.getUser().getUsername());
            context.success();
        } else {
            logger.warn("Invalid OTP provided");
            context.failure(AuthenticationFlowError.INVALID_CREDENTIALS);
        }
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
    }

    @Override
    public void close() {
    }
}