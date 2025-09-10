package org.example.keycloak.authenticator;

import jakarta.mail.MessagingException;
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

    Logger logger = LoggerFactory.getLogger(OtpSenderAuthenticator.class);
    private OTPService otpService;

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        otpService = new OTPService(context.getAuthenticatorConfig());
        UserModel user = context.getUser();

        if (user == null) {
            context.failure(AuthenticationFlowError.UNKNOWN_USER);
            return;
        }

        String username = user.getUsername();

        if (extracted(context, username, user)) return;

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
                throw new RuntimeException(e);
            }
        } else if (username.matches(UsernameValidatorConstant.Phone.getPattern())) {
            String otp = otpService.generateOTP();
            otpService.storeOTP(otp, context.getSession());
            otpService.sendSMSOTP(user.getUsername(), otp);
            logger.info("Username is Sms");
        } else {
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
        String inputOtp = context.getHttpRequest()
                .getDecodedFormParameters().getFirst("otp");

        if (inputOtp == null || inputOtp.isEmpty()) {
            context.failure(AuthenticationFlowError.INVALID_CREDENTIALS);
            return;
        }

        if (otpService.validateOTP(inputOtp, context.getSession())) {
            context.success();
            logger.info("Succcessfully Login!");
        } else {
            context.failure(AuthenticationFlowError.INVALID_CREDENTIALS);
        }
    }


    @Override
    public boolean requiresUser() {
        return true;
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

