# 🔐 Keycloak OTP Authenticator SPI

This project provides a **custom OTP (One-Time Password) authenticator** for **Keycloak**, allowing authentication via **email** or **SMS** based on user preference. This OTP authentication is integrated into Keycloak’s authentication flows.

---

## 📚 Table of Contents

1. [Prerequisites](#prerequisites)
2. [Step 1: Build the Project](#step-1-build-the-project)
3. [Step 2: Deploy the JAR to Keycloak](#step-2-deploy-the-jar-to-keycloak)
4. [Step 3: Configure the OTP Flow in Keycloak](#step-3-configure-the-otp-flow-in-keycloak)
5. [Step 4: Testing the OTP Flow](#step-4-testing-the-otp-flow)
6. [Troubleshooting](#troubleshooting)
7. [License](#license)
8. [Additional Notes](#additional-notes)

---

## ✅ Prerequisites

Before you begin, ensure you have:

- Keycloak installed and running ([Download here](https://www.keycloak.org/downloads))
- Java Development Kit (JDK) 11 or later
- Maven installed ([Install here](https://maven.apache.org/install.html))
- SMTP server (e.g., Gmail, Mailgun) for email OTP
- Twilio account for SMS OTP ([Sign up](https://www.twilio.com))

---

## 🏗️ Step 1: Build the Project

### Clone the Repository
```bash
git clone https://github.com/yourusername/keycloak-otp-authenticator.git
cd keycloak-otp-authenticator
```
### Install Dependencies
```bash
mvn clean install
```

After building, the JAR will be located at: target/otp_sender_authenticator-1.0-SNAPSHOT.jar

## 📦 Step 2: Deploy the JAR to Keycloak

