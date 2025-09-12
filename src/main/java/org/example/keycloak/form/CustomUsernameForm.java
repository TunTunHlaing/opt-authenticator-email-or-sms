package org.example.keycloak.form;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.keycloak.authentication.AbstractFormAuthenticator;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

public class CustomUsernameForm extends AbstractFormAuthenticator {

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        Response form = createUsernameForm(context);
        context.challenge(form);
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String username = formData.getFirst("username");

        if (username == null || username.trim().isEmpty()) {
            context.getEvent().error("invalid_username");
            Response form = createUsernameForm(context);
            context.failureChallenge(AuthenticationFlowError.INVALID_USER, form);
            return;
        }

        context.success();
    }

    private Response createUsernameForm(AuthenticationFlowContext context) {
        LoginFormsProvider form = context.form()
                .setAttribute("username", context.getHttpRequest().getDecodedFormParameters().getFirst("username"));
        return form.createForm("custom-username-form.ftl");
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
}