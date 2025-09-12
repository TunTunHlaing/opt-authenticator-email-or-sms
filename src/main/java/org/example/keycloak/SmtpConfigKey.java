package org.example.keycloak;

public enum SmtpConfigKey {

    HOST("host"),
    PASSWORD("password"),
    PORT("port"),
    FROM("from");

    private String keyName;

    public String getKeyName() {
        return keyName;
    }

    SmtpConfigKey(String keyName) {
        this.keyName = keyName;
    }
}
