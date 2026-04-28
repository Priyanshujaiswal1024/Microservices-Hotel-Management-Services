# 🏨 Microservices Hotel Management System

A **scalable microservices-based backend system** for managing hotel operations like booking, user management, notifications, and more. Built using **Spring Boot, Spring Cloud, and REST APIs** following industry best practices.

---

## 🚀 Features

* 🔐 Authentication & Authorization (JWT based)
* 🏨 Hotel & Room Management
* 📅 Booking System with Availability Check
* 🔁 Inter-service communication using Feign Client
* 📦 API Gateway for centralized routing
* ⚙️ Config Server for centralized configuration
* 🔔 Notification Service (Email/SMS ready)
* 📊 Swagger API Documentation
* 🧪 Unit Testing (JUnit & Mockito)
* 🐳 Dockerized services

---

## 🧱 Microservices Architecture

This project follows **Microservices Architecture**, where each service is independent and communicates via REST APIs.

### 📌 Services Included:

* **API Gateway** → Entry point for all client requests
* **Config Server** → Centralized configuration management
* **Booking Service** → Handles booking logic
* **User Service** → Manages users
* **Hotel Service** → Manages hotels
* **Notification Service** → Sends notifications

---

## 🏗️ Architecture Diagram

```
Client
  |
  v
API Gateway
  |
  ├── Booking Service
  ├── User Service
  ├── Hotel Service
  └── Notification Service
        |
        v
     Database (MySQL)
```

---

## 🛠️ Tech Stack

| Category         | Technology           |
| ---------------- | -------------------- |
| Backend          | Java, Spring Boot    |
| Microservices    | Spring Cloud         |
| API Gateway      | Spring Cloud Gateway |
| Communication    | OpenFeign            |
| Security         | Spring Security, JWT |
| Database         | MySQL                |
| Build Tool       | Maven                |
| Testing          | JUnit, Mockito       |
| Documentation    | Swagger              |
| Containerization | Docker               |

---

## ⚙️ Installation & Setup

### 🔹 Prerequisites

* Java 17+
* Maven
* MySQL
* Docker (optional)

---

### 🔹 Steps to Run

1. Clone the repository:

```bash
git clone https://github.com/your-username/Microservices-Hotel-Management-Services.git
```

2. Navigate to project:

```bash
cd microservices
```

3. Start Config Server:

```bash
cd config-server
mvn spring-boot:run
```

4. Start other services one by one:

```bash
cd booking-service
mvn spring-boot:run
```

👉 Repeat for all services

---

## 🔗 API Documentation

👉 Swagger UI available at:

```
http://localhost:<port>/swagger-ui.html
```

---

## 📂 Project Structure

```
microservices/
 ├── api-gateway
 ├── config-server
 ├── booking-service
 ├── user-service
 ├── hotel-service
 ├── notification-service
```

---

## 🔐 Security

* JWT-based authentication
* Role-based authorization
* Secure API endpoints

---

## 🧪 Testing

Run tests using:

```bash
mvn test
```

---

## 🐳 Docker Support

Build Docker image:

```bash
docker build -t booking-service .
```

Run container:

```bash
docker run -p 8080:8080 booking-service
```

---

## 📌 Future Enhancements

* 🔄 Kafka Integration (Event-driven architecture)
* ⚡ Redis Caching
* ☁️ AWS Deployment
* 📊 Monitoring with Prometheus & Grafana
* 🔐 OAuth2 Security

---

## 👨‍💻 Author

**Priyanshu Jaiswal**

* Java Full Stack Developer
* Passionate about Microservices & Backend Systems

---

## ⭐ Contribute

Feel free to fork this repo and contribute!

---

## 📜 License

This project is licensed under the MIT License.
