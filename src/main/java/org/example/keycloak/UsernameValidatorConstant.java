package org.example.keycloak;

public enum UsernameValidatorConstant {

    Email("^[\\\\w.-]+@[\\\\w.-]+\\\\.[a-zA-Z]{2,}$"),
    Phone("^\\+?[0-9]{7,15}$");

    private String pattern;

    public String getPattern() {
        return pattern;
    }

    UsernameValidatorConstant(String pattern) {
        this.pattern = pattern;
    }

}
