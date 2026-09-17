# UPI Micro Payment App

A UPI-based micro payment application developed as a group project using **Kotlin, Spring Boot, and MySQL**.

The application provides separate experiences for **Users, Merchants, and Administrators**, with features such as authentication, UPI transactions, QR-based payments, bank linking, KYC, transaction management, notifications, fraud monitoring, and analytics.

\---

## 🎯 Project Objective

The objective of this project is to develop a digital payment application that allows users and merchants to manage UPI-based transactions through an Android application connected to a Spring Boot REST API.

The project demonstrates the integration of:

* Android mobile application
* RESTful backend services
* MySQL database
* Authentication and authorization
* QR-based payments
* Firebase notifications
* Transaction management
* Fraud detection and monitoring
* Administrative analytics

\---

## ✨ Features

### 👤 User

* User registration and login
* OTP verification
* PIN setup and verification
* User profile management
* KYC management
* Bank account linking
* UPI ID verification
* Personal QR code generation
* QR code scanning
* Send money
* Add money
* Balance management
* Transaction history
* Payment status tracking
* Push notifications

### 🏪 Merchant

* Merchant registration and login
* Merchant profile management
* Merchant KYC
* Bank account linking
* QR code generation
* Payment collection
* Transaction history
* Merchant dashboard
* Revenue analytics
* Transaction analytics

### 🛡️ Admin

* Admin login
* User management
* Merchant management
* Transaction analytics
* User analytics
* Merchant analytics
* Revenue analytics
* QR payment analytics
* Fraud transaction monitoring
* Fraud alerts
* Dashboard KPIs

\---

## 🏗️ System Architecture

```text
                    ┌─────────────────────────┐
                    │     Android App         │
                    │      Kotlin + XML       │
                    └────────────┬────────────┘
                                 │
                                 │ REST APIs
                                 ▼
                    ┌─────────────────────────┐
                    │    Spring Boot API      │
                    │          Kotlin         │
                    ├─────────────────────────┤
                    │ Controllers             │
                    │ Services                │
                    │ Repositories            │
                    │ Spring Security / JWT   │
                    │ Transaction Management  │
                    │ Fraud Detection         │
                    │ Notification Services   │
                    │ Reconciliation          │
                    └────────────┬────────────┘
                                 │
                                 │ JPA / Hibernate
                                 ▼
                    ┌─────────────────────────┐
                    │         MySQL           │
                    │        Database         │
                    └─────────────────────────┘
                                 │
                                 ▼
                    ┌─────────────────────────┐
                    │ Firebase Cloud Messaging │
                    │       Notifications      │
                    └─────────────────────────┘
```

\---

## 🛠️ Technology Stack

|Layer|Technologies|
|-|-|
|Mobile|Kotlin, Android SDK, XML|
|Backend|Kotlin, Spring Boot, Spring Security, JWT|
|API|REST APIs, Retrofit|
|Persistence|Spring Data JPA, Hibernate|
|Database|MySQL, MySQL Workbench|
|Notifications|Firebase Cloud Messaging|
|QR|QR code generation and scanning|
|Build Tools|Gradle, Maven|
|Testing|Postman|
|Version Control|Git, GitHub|
|Development|Android Studio, VS Code|

\---

## 📂 Project Structure

```text
upi-micro-payment-app/
│
├── android-app/
│   ├── app/
│   │   ├── src/
│   │   │   ├── main/
│   │   │   │   ├── java/
│   │   │   │   └── res/
│   │   │   └── test/
│   │   ├── build.gradle.kts
│   │   └── proguard-rules.pro
│   ├── build.gradle.kts
│   ├── gradle/
│   ├── gradle.properties
│   ├── gradlew
│   ├── gradlew.bat
│   └── settings.gradle.kts
│
├── spring-boot-api/
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/
│   │   │   └── resources/
│   │   └── test/
│   ├── pom.xml
│   ├── mvnw
│   └── mvnw.cmd
│
├── .gitignore
└── README.md
```

\---

## 🔐 Security

The application implements security mechanisms for authentication and protected operations.

Security features include:

* JWT-based authentication
* Spring Security
* Role-based access control
* OTP verification
* PIN verification
* Protected REST APIs
* Session management

Sensitive configuration files and credentials are excluded from version control using `.gitignore`.

The public repository excludes:

* Database credentials
* Environment-specific secrets
* Firebase service-account credentials
* Local Firebase configuration

\---

## 💳 Payment \& Transaction Flow

```text
User
 │
 ▼
Android Application
 │
 ▼
Authentication
 │
 ├── Registration
 ├── OTP Verification
 └── PIN Verification
 │
 ▼
User Dashboard
 │
 ├── Send Money
 ├── Scan QR
 ├── Add Money
 ├── Check Balance
 └── Transaction History
 │
 ▼
Spring Boot REST API
 │
 ▼
Business Logic
 │
 ▼
MySQL Database
```

\---

## 🔄 Application Modules

### Authentication Module

Handles:

* Registration
* Login
* OTP verification
* PIN setup
* PIN verification
* JWT authentication

### User Module

Handles:

* User profiles
* KYC
* Bank linking
* UPI verification
* Balance
* QR code
* Transactions

### Merchant Module

Handles:

* Merchant registration
* Merchant profile
* KYC
* Bank linking
* QR payments
* Merchant transactions
* Revenue and transaction analytics

### Transaction Module

Handles:

* Money transfers
* Transaction creation
* Transaction status
* Transaction history
* Transaction analytics

### QR Code Module

Handles:

* QR code generation
* QR code scanning
* QR-based payment verification
* QR payment analytics

### Admin Module

Handles:

* User management
* Merchant management
* Transaction monitoring
* Revenue analytics
* User analytics
* Merchant analytics
* QR analytics
* Fraud monitoring

\---

## 🚨 Fraud Detection

The backend contains a fraud detection module for identifying potentially suspicious transactions.

The implementation includes:

* Fraud Detection Service
* Fraud Rule Engine
* Fraud-related constants
* Fraud transaction monitoring
* Admin fraud alerts

\---

## 🔔 Notifications

Firebase Cloud Messaging is integrated for push notification functionality.

The notification module includes:

* Device token management
* Firebase configuration
* Notification service
* Notification dispatcher
* Notification events
* Notification types

\---

## 🔁 Transaction Reconciliation

The backend contains a reconciliation module for transaction reconciliation activities.

It includes:

* Reconciliation service
* Reconciliation scheduler

The module supports periodic transaction consistency checks.

\---

## 📊 Analytics \& Management

The application includes administrative and merchant analytics.

### Admin Analytics

* User analytics
* Merchant analytics
* Transaction analytics
* Revenue analytics
* QR payment analytics
* Dashboard KPIs
* Fraud monitoring

### Merchant Analytics

* Transaction information
* Revenue information
* Merchant KPIs
* Payment activity

\---

## 🧪 API Testing

The Spring Boot backend exposes REST APIs that can be tested using **Postman**.

API areas include:

* Authentication
* Users
* Merchants
* Transactions
* UPI
* QR codes
* Admin operations
* Notifications

\---

## ⚙️ Setup \& Installation

### Prerequisites

* Android Studio
* JDK
* MySQL
* MySQL Workbench
* Git
* Postman

### Clone the Repository

```bash
git clone https://github.com/padminichoudhury0320/upi-micro-payment-app.git
cd upi-micro-payment-app
```

### Backend

Navigate to:

```text
spring-boot-api/
```

Configure the required local MySQL database and application settings.

For Windows:

```powershell
.\\\\mvnw.cmd spring-boot:run
```

### Android

Open:

```text
android-app/
```

in Android Studio.

Allow Gradle to sync, configure the required local Firebase configuration and backend API URL, and run the application on an Android emulator or physical Android device.

> Sensitive configuration files are intentionally excluded from this repository.

\---

## 📱 Application Screens

### User

* Splash Screen
* Role Selection
* Login
* Registration
* OTP Verification
* PIN Setup
* User Dashboard
* Send Money
* QR Scanner
* My QR
* Balance
* Transaction History
* Profile
* KYC
* Bank Linking

### Merchant

* Merchant Login
* Merchant Registration
* Merchant Dashboard
* Merchant Profile
* Merchant KYC
* Bank Linking
* QR Payment
* Transaction History
* Analytics
* Settings

### Admin

* Admin Login
* Admin Dashboard
* User Management
* Merchant Management
* Transaction Analytics
* User Analytics
* Merchant Analytics
* Revenue Analytics
* QR Analytics
* Fraud Alerts

\---

## 📸 Screenshots

Screenshots of the application can be added here as the project documentation is expanded.

Suggested screenshots:

* Login Screen
* Registration Screen
* User Dashboard
* Send Money
* QR Scanner
* Transaction History
* Merchant Dashboard
* Admin Dashboard
* Fraud Alerts
* Analytics Dashboard

\---

## 🚀 Future Enhancements

* Production deployment
* Enhanced fraud detection
* Automated testing
* CI/CD integration
* Improved analytics
* Additional payment workflows
* Enhanced notification workflows
* Cloud deployment
* Improved scalability and monitoring

\---

## 🎓 Project Highlights

This project provided practical experience with:

* Android application development
* Kotlin programming
* Spring Boot backend development
* REST API development
* MySQL database management
* JWT authentication
* Spring Security
* QR code integration
* Firebase Cloud Messaging
* Transaction processing
* Fraud detection
* Transaction reconciliation
* API testing using Postman
* Git and GitHub version control

\---

## 👥 Project Team

This project was developed collaboratively by:

* **Kavya Balla** — GitHub: `padminichoudhury0320`
* **BallaAnkitha0321** — GitHub: `BallaAnkitha0321`

\---

## 📄 License

This project is developed for **educational and portfolio purposes**.



