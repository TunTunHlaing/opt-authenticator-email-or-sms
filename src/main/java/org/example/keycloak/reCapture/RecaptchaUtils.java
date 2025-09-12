package org.example.keycloak.reCapture;

import jakarta.ws.rs.core.MultivaluedMap;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.connections.httpclient.HttpClientProvider;
import org.keycloak.events.Details;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.validation.Validation;
import org.keycloak.util.JsonSerialization;

import java.io.InputStream;
import java.util.*;

public class RecaptchaUtils {

    public static boolean validateRecaptcha(AuthenticationFlowContext context,
                                            boolean success,
                                            String captcha,
                                            String secret,
                                            String USE_RECAPTCHA_NET) {
        HttpClient httpClient = context.getSession().getProvider(HttpClientProvider.class).getHttpClient();
        HttpPost post = new HttpPost("https://www." + getRecaptchaDomain(context.getAuthenticatorConfig(), USE_RECAPTCHA_NET) + "/recaptcha/api/siteverify");
        List<NameValuePair> formparams = new LinkedList<>();
        formparams.add(new BasicNameValuePair("secret", secret));
        formparams.add(new BasicNameValuePair("response", captcha));
        formparams.add(new BasicNameValuePair("remoteip", context.getConnection().getRemoteAddr()));
        try {
            UrlEncodedFormEntity form = new UrlEncodedFormEntity(formparams, "UTF-8");
            post.setEntity(form);
            HttpResponse response = httpClient.execute(post);
            InputStream content = response.getEntity().getContent();
            try {
                Map json = JsonSerialization.readValue(content, Map.class);
                Object val = json.get("success");
                success = Boolean.TRUE.equals(val);
            } finally {
                content.close();
            }
        } catch (Exception e) {
            ServicesLogger.LOGGER.recaptchaFailed(e);
        }
        return success;
    }


    public static String getRecaptchaDomain(AuthenticatorConfigModel config, String USE_RECAPTCHA_NET) {
        Boolean useRecaptcha = Optional.ofNullable(config)
                .map(configModel -> configModel.getConfig())
                .map(cfg -> Boolean.valueOf(cfg.get(USE_RECAPTCHA_NET)))
                .orElse(false);
        if (useRecaptcha) {
            return "recaptcha.net";
        }

        return "google.com";
    }


    public static boolean recaptchaAction (AuthenticationFlowContext context,
                        String SITE_SECRET,
                        String G_RECAPTCHA_RESPONSE,
                                           String USE_RECAPTCHA_NET ) {

        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        boolean success = false;
        context.getEvent().detail(Details.AUTH_METHOD, "auth_method");

        String captcha = formData.getFirst(G_RECAPTCHA_RESPONSE);
        if (!Validation.isBlank(captcha)) {
            AuthenticatorConfigModel captchaConfig = context.getAuthenticatorConfig();
            String secret = captchaConfig.getConfig().get(SITE_SECRET);

            success = validateRecaptcha(context, success, captcha, secret, USE_RECAPTCHA_NET);
        }
       return success;
    }

}
