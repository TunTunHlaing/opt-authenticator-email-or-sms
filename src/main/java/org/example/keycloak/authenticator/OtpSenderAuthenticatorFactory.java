package org.example.keycloak.authenticator;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.List;

public class OtpSenderAuthenticatorFactory implements AuthenticatorFactory {

    @Override
    public String getDisplayType() {
        return "OTP Authenticator";
    }

    @Override
    public String getReferenceCategory() {
        return "";
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return new AuthenticationExecutionModel.Requirement[] {
                AuthenticationExecutionModel.Requirement.REQUIRED,
                AuthenticationExecutionModel.Requirement.ALTERNATIVE,
                AuthenticationExecutionModel.Requirement.DISABLED
        };
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public String getHelpText() {
        return "";
    }
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {

        return List.of(
                constructOtpProperty("twilio_account_sid", "Twilio Account SID", ProviderConfigProperty.STRING_TYPE, "Twilio SID for sending SMS"),
                constructOtpProperty("twilio_auth_token", "Twilio Auth Token", ProviderConfigProperty.STRING_TYPE, "Twilio Auth Token"),
                constructOtpProperty("twilio_phone_number", "Twilio Phone Number", ProviderConfigProperty.STRING_TYPE, "Sender phone number for Twilio"),

                constructOtpProperty("email_host", "SMTP Host", ProviderConfigProperty.STRING_TYPE, "SMTP server host"),
                constructOtpProperty("email_port", "SMTP Port", ProviderConfigProperty.STRING_TYPE, "SMTP server port"),
                constructOtpProperty("email_username", "SMTP Username", ProviderConfigProperty.STRING_TYPE, "SMTP username"),
                constructOtpProperty("email_password", "SMTP Password", ProviderConfigProperty.STRING_TYPE, "SMTP password"),
                constructOtpProperty("email_from", "From Email", ProviderConfigProperty.STRING_TYPE, "Sender email address")
        );
    }


    private ProviderConfigProperty constructOtpProperty(String name, String label, String type, String helpText) {
        ProviderConfigProperty otpProperty = new ProviderConfigProperty();
        otpProperty.setName(name);
        otpProperty.setLabel(label);
        otpProperty.setType(type);
        otpProperty.setHelpText(helpText);
        return otpProperty;
    }

    @Override
    public Authenticator create(KeycloakSession keycloakSession) {

        return new OtpSenderAuthenticator();
    }

    @Override
    public void init(Config.Scope scope) {

    }

    @Override
    public void postInit(KeycloakSessionFactory keycloakSessionFactory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return "otp-authenticator";
    }
}
