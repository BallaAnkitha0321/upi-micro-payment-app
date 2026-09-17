\# UPI Micro Payment App



A UPI-based micro payment application developed as a group project using \*\*Kotlin, Spring Boot, and MySQL\*\*.



The application provides separate experiences for \*\*Users, Merchants, and Administrators\*\*, with features such as authentication, UPI payments, QR-based transactions, bank linking, KYC, transaction management, notifications, fraud monitoring, and analytics.



\---



\## 🎯 Project Objective



The objective of this project is to develop a digital payment application that allows users and merchants to securely manage UPI-based transactions through an Android application connected to a Spring Boot REST API.



The project demonstrates the integration of:



\- Android mobile application

\- RESTful backend services

\- MySQL database

\- Authentication and authorization

\- QR-based payments

\- Firebase notifications

\- Transaction and fraud management

\- Administrative analytics



\---



\## ✨ Features



\### 👤 User



\- User registration and login

\- OTP verification

\- PIN setup and verification

\- User profile management

\- KYC management

\- Bank account linking

\- UPI ID verification

\- Generate personal QR code

\- Scan QR codes

\- Send money

\- Add money

\- Check balance

\- Transaction history

\- Payment status

\- Push notifications



\### 🏪 Merchant



\- Merchant registration and login

\- Merchant profile management

\- Merchant KYC

\- Bank account linking

\- QR code generation

\- Payment collection

\- Transaction history

\- Merchant dashboard

\- Revenue analytics

\- Transaction analytics



\### 🛡️ Admin



\- Admin login

\- User management

\- Merchant management

\- Transaction analytics

\- User analytics

\- Merchant analytics

\- Revenue analytics

\- QR payment analytics

\- Fraud transaction monitoring

\- Fraud alerts

\- Dashboard KPIs



\---



\## 🏗️ Architecture



┌──────────────────────────────┐

│       Android Application    │

│          Kotlin + XML        │

└──────────────┬───────────────┘

&#x20;              │

&#x20;              │ REST APIs

&#x20;              ▼

┌──────────────────────────────┐

│       Spring Boot API        │

│            Kotlin            │

├──────────────────────────────┤

│ Controllers                  │

│ Services                     │

│ Repositories                 │

│ Spring Security / JWT        │

│ Transaction Management       │

│ Fraud Detection              │

│ Notification Services        │

│ Reconciliation               │

└──────────────┬───────────────┘

&#x20;              │

&#x20;              │ JPA / Hibernate

&#x20;              ▼

┌──────────────────────────────┐

│            MySQL             │

│           Database           │

└──────────────────────────────┘



&#x20;              │

&#x20;              ▼

┌──────────────────────────────┐

│    Firebase Cloud Messaging  │

│        Notifications         │

└──────────────────────────────┘





\---



\## 🛠️ Technologies Used



\### Android



\* Kotlin

\* Android SDK

\* XML

\* Android Studio

\* Retrofit

\* Firebase Cloud Messaging

\* QR Code processing



\### Backend



\* Kotlin

\* Spring Boot

\* Spring Security

\* JWT

\* Spring Data JPA

\* Hibernate

\* Maven

\* REST APIs



\### Database



\* MySQL

\* MySQL Workbench



\### Tools



\* Android Studio

\* VS Code

\* Postman

\* Git

\* GitHub



\---



\## 🔐 Security



The application implements security mechanisms including:



\* JWT-based authentication

\* Spring Security

\* Role-based access

\* OTP verification

\* PIN verification

\* Protected REST APIs



Sensitive configuration files and credentials are excluded from the Git repository using `.gitignore`.



Examples include:



\* Environment variables

\* Database credentials

\* Firebase service-account credentials

\* Local Firebase configuration



\---



\## 💳 Payment \& Transaction Flow





User

&#x20;│

&#x20;▼

Android Application

&#x20;│

&#x20;▼

Authentication

&#x20;│

&#x20;├── Registration

&#x20;├── OTP Verification

&#x20;└── PIN Verification

&#x20;│

&#x20;▼

User Dashboard

&#x20;│

&#x20;├── Send Money

&#x20;├── Scan QR

&#x20;├── Add Money

&#x20;├── Check Balance

&#x20;└── Transaction History

&#x20;│

&#x20;▼

Spring Boot REST API

&#x20;│

&#x20;▼

Business Logic

&#x20;│

&#x20;▼

MySQL Database

```



\---



\## 🚨 Fraud Detection



The backend contains a fraud detection module for identifying potentially suspicious transactions.



The implementation includes:



\* Fraud Detection Service

\* Fraud Rule Engine

\* Fraud-related constants

\* Fraud transaction monitoring

\* Admin fraud alerts



\---



\## 📊 Analytics \& Management



The admin module provides dashboards and analytics for monitoring application activity.



It includes:



\* User analytics

\* Merchant analytics

\* Transaction analytics

\* Revenue analytics

\* QR payment analytics

\* Dashboard KPIs

\* Fraud monitoring



\---



\## 🧪 API Testing



The backend REST APIs can be tested using \*\*Postman\*\*.



API areas include:



\* Authentication

\* Users

\* Merchants

\* Transactions

\* UPI

\* QR codes

\* Admin operations

\* Notifications



\---



\## 🚀 Future Enhancements



\* Production deployment

\* Enhanced fraud detection

\* Automated testing

\* CI/CD integration

\* Improved analytics

\* Additional payment workflows

\* Enhanced notification workflows



\---



\## 📄 License



This project is developed for educational and portfolio purposes.









