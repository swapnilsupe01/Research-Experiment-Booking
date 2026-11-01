# PaperLab — Research Experiment Booking System

> A full-stack **Spring Boot + MySQL** web application for managing university research experiment schedules, laboratory time slots, student booking requests, approvals, cancellations, and booking history.
> The project ships with a restyled **PaperLab** front-end, REST APIs, Selenium UI tests, a Jenkins CI/CD pipeline, and Docker-based deployment.

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Technology Stack](#technology-stack)
3. [System Architecture](#system-architecture)
4. [Prerequisites](#prerequisites)
5. [Quick Start — Choose Your Method](#quick-start--choose-your-method)
   - [Method A — Run Locally (JAR)](#method-a--run-locally-jar)
   - [Method B — Run with Docker Compose](#method-b--run-with-docker-compose)
   - [Method C — Jenkins CI/CD Pipeline](#method-c--jenkins-cicd-pipeline-docker-cd)
6. [Application Pages and URLs](#application-pages-and-urls)
7. [Demo Login Credentials](#demo-login-credentials)
8. [Database Setup](#database-setup)
9. [REST API Reference](#rest-api-reference)
10. [Booking Workflow](#booking-workflow)
11. [Running Tests](#running-tests)
12. [Everyday Docker Commands](#everyday-docker-commands)
13. [Troubleshooting](#troubleshooting)
14. [Jenkins CI/CD Details](#jenkins-cicd-details)
15. [Selenium UI Testing](#selenium-ui-testing)
16. [Git Workflow](#git-workflow)
17. [Future Enhancements](#future-enhancements)

---

## Project Overview

Research laboratories need an organised way to manage experiment schedules, laboratory availability, and student booking requests. Manual scheduling causes conflicts and makes it hard to track who is booked where.

**PaperLab** provides a centralised application for:

- Viewing active experiments and available time slots.
- Registering students and submitting booking requests.
- Confirming, rejecting, or cancelling bookings.
- Recording a full history of booking status changes.
- Teacher dashboard for managing experiments and approvals.

---

## Technology Stack

| Component | Technology |
|---|---|
| Language | Java 21 |
| Backend framework | Spring Boot 4.x |
| Build tool | Maven (Maven Wrapper included) |
| Database | MySQL (XAMPP, port 3307) |
| Data persistence | Spring Data JPA / Hibernate |
| Security | Spring Security |
| Templating | Thymeleaf |
| Front-end styling | Tailwind CSS (CDN) + Custom CSS |
| Application server | Embedded Apache Tomcat (port 8081) |
| Containerisation | Docker + Docker Compose |
| CI/CD | Jenkins (port 8080) |
| Automated UI tests | Selenium WebDriver |
| Version control | Git / GitHub |
| Dev environment | Windows 11, PowerShell |

---

## System Architecture

```
Browser
   |
   v
Spring Boot — Tomcat :8081
   |-- SecurityConfig     (Spring Security — form login, RBAC)
   |-- Controllers        (HTTP request/response handling)
   |-- Services           (Business logic — bookings, experiments)
   |-- Repositories       (Spring Data JPA)
   `-- Entities           (User, Experiment, TimeSlot, Booking, BookingHistory)
         |
         v
    MySQL :3307
    (research_booking_system)
```

---

## Prerequisites

| Tool | Minimum Version | How to verify |
|---|---|---|
| Java JDK | 21 | `java -version` |
| Maven | 3.9+ (or use `mvnw.cmd`) | `mvn -version` |
| MySQL | 8.0+ | XAMPP Control Panel |
| XAMPP | Any recent | Open XAMPP Control Panel |
| Git | Any | `git --version` |
| Docker Desktop | 24+ | `docker --version` |
| Jenkins | LTS | `http://localhost:8080` |

> **MySQL port:** This project uses XAMPP MySQL on **port 3307** (not the default 3306).

---

## Quick Start — Choose Your Method

| Method | Best for | App URL |
|---|---|---|
| **A — Local JAR** | Development / quick testing | `http://localhost:8081` |
| **B — Docker Compose** | Production-like local testing | `http://localhost:8081` |
| **C — Jenkins CI/CD** | Full automated pipeline demo | `http://localhost:9091` |

---

## Method A — Run Locally (JAR)

### Step 1 — Start MySQL via XAMPP

1. Open **XAMPP Control Panel**.
2. Click **Start** next to **MySQL**.
3. Confirm MySQL shows green / Running on port **3307**.

### Step 2 — Create the database

```sql
CREATE DATABASE IF NOT EXISTS research_booking_system;
```

### Step 3 — Clone the repository

```powershell
git clone https://github.com/swapnilsupe01/Research-Experiment-Booking.git
cd Research-Experiment-Booking
```

### Step 4 — Build the JAR

```powershell
cd research-booking-system
.\mvnw.cmd clean package -DskipTests
```

Output JAR location:
```
research-booking-system/target/research-booking-system-0.0.1-SNAPSHOT.jar
```

### Step 5 — Run the application

```powershell
java -jar target\research-booking-system-0.0.1-SNAPSHOT.jar ^
  --spring.datasource.url=jdbc:mysql://127.0.0.1:3307/research_booking_system ^
  --spring.datasource.username=root ^
  --spring.datasource.password= ^
  --server.port=8081
```

> Replace `--spring.datasource.password=` with your MySQL root password if set.

### Step 6 — Wait for this log line

```
Started ResearchBookingSystemApplication in X.XXX seconds
```

### Step 7 — Open the application

| Page | URL |
|---|---|
| Landing / Hero | http://localhost:8081/landing |
| Student Login | http://localhost:8081/login |
| Teacher Login | http://localhost:8081/login?portal=teacher |
| Student Sign Up | http://localhost:8081/signup |
| Student Dashboard | http://localhost:8081/dashboard/student |
| Teacher Dashboard | http://localhost:8081/dashboard/teacher |

---

## Method B — Run with Docker Compose

### Step 1 — Start MySQL via XAMPP (required first)

Follow Method A Step 1. MySQL must be running before Docker starts.

### Step 2 — Build the JAR

```powershell
cd research-booking-system
.\mvnw.cmd clean package -DskipTests
cd ..
```

### Step 3 — Build and start the Docker container

Run from the **project root**:

```powershell
docker compose up -d --build
```

Expected output:
```
[+] Building ... done
[+] Running 1/1
 v Container research-booking-app  Started
```

### Step 4 — Verify the container

```powershell
docker ps --filter "name=research-booking-app"
docker logs -f research-booking-app
```

Wait for `Started ResearchBookingSystemApplication`. Press `Ctrl+C` to stop log streaming.

### Step 5 — Open the application

`http://localhost:8081/landing`

### Rebuild after any code changes

```powershell
docker compose down
cd research-booking-system
mvn clean package -DskipTests
cd ..
docker compose up -d --build
```

---

## Method C — Jenkins CI/CD Pipeline (Docker CD)

Jenkins runs the full pipeline automatically: checkout, build, test, Selenium, Docker build, push to registry, deploy, verify.

The deployed container is exposed on host port **9091**.

### Step 1 — Open Jenkins

`http://localhost:8080`

### Step 2 — Create the pipeline job

1. **New Item** → **Pipeline** → name: `Research-Experiment-Booking` → **OK**
2. **General** → check **This project is parameterized**
3. Add **String Parameter**:
   - Name: `HOST_PORT`
   - Default Value: `9091`
   - Description: `Host port for the Docker container`
4. **Build Triggers** → check **Poll SCM** → Schedule: `H/5 * * * *`
5. **Pipeline** → **Definition**: `Pipeline script from SCM`
   - SCM: `Git`
   - Repository URL: `https://github.com/swapnilsupe01/Research-Experiment-Booking.git`
   - Branch: `*/main`
   - Script Path: `Jenkinsfile`
6. **Save**

### Step 3 — Run the pipeline

Click **Build with Parameters** → confirm `HOST_PORT = 9091` → **Build**

### Step 4 — Pipeline stages

| Stage | What it does |
|---|---|
| Checkout | Pulls latest `main` from GitHub |
| Build and Test | `mvn clean verify` — compiles + all unit/integration tests |
| Selenium Continuous Tests | Starts JAR on port 8083, runs `SeleniumJourneyChecks`, stops JAR |
| Package | `mvn package -DskipTests` — final deployment JAR |
| Version | Tags image as `1.0.<BUILD_NUMBER>` |
| Docker Build | Builds image, tags in local registry `localhost:5000` |
| Push to Registry | Pushes to local Docker registry on port 5000 |
| Deploy Container | Removes old container, starts `research-booking-cd` on `9091:8081` |
| Verify Container | Polls `/api/experiments` and `/ui` until HTTP 200, writes `cd-evidence.txt` |

### Step 5 — Access the deployed app

```
http://localhost:9091/landing
http://localhost:9091/login
```

---

## Application Pages and URLs

| Page | Path | Login required |
|---|---|---|
| Landing / Hero | `/landing` or `/` | No |
| Student Login | `/login` | No |
| Teacher Login | `/login?portal=teacher` | No |
| Student Registration | `/signup` | No |
| Student Dashboard | `/dashboard/student` | Yes — STUDENT |
| Teacher Dashboard | `/dashboard/teacher` | Yes — TEACHER |
| Experiments API | `/api/experiments` | No |

---

## Demo Login Credentials

| Role | Email | Password |
|---|---|---|
| Teacher | `teacher@university.edu` | `TeacherPass123!` |
| Student | Register at `/signup` | Your chosen password |

---

## Database Setup

### application.properties (already configured)

```properties
spring.application.name=research-booking-system

spring.datasource.url=jdbc:mysql://127.0.0.1:3307/research_booking_system
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD:}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false

server.port=8081
```

> `ddl-auto=validate` checks the schema but does NOT create tables automatically.

### Required SQL (run once)

```sql
CREATE DATABASE IF NOT EXISTS research_booking_system;
-- Required tables: users, experiments, time_slots, bookings, booking_history
```

---

## REST API Reference

| Method | Endpoint | Auth | Purpose |
|---|---|---|---|
| POST | `/api/users/register` | None | Register a new student |
| GET | `/api/experiments` | None | List active experiments |
| GET | `/api/experiments/{id}` | None | Experiment details |
| GET | `/api/experiments/{id}/slots` | None | Slots for an experiment |
| GET | `/api/experiments/slots/upcoming` | Any | Upcoming slots |
| POST | `/api/bookings` | STUDENT | Submit booking request |
| GET | `/api/bookings/user/{userId}` | STUDENT | User's bookings |
| GET | `/api/bookings/{id}` | STUDENT | Single booking |
| PUT | `/api/bookings/{id}/status` | TEACHER | Confirm or reject |
| PUT | `/api/bookings/{id}/cancel` | STUDENT | Cancel booking |
| GET | `/api/bookings/{id}/history` | STUDENT | Status history |

---

## Booking Workflow

```
Select Experiment
      |
      v
Select Available Slot
      |
      v
Submit Booking  -->  PENDING
                        |
          +-------------+-------------+
          |                           |
          v                           v
     CONFIRMED                    REJECTED
          |
          v
     CANCELLED
```

Rules enforced by the service:
- Slot must be in the future.
- Slot must have remaining capacity.
- No duplicate booking for the same slot.

---

## Running Tests

### All unit and integration tests

```powershell
cd research-booking-system
.\mvnw.cmd clean test
```

Expected:
```
[INFO] Tests run: 57, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### Selenium UI tests only

```powershell
# Terminal 1 — start app on port 8083
java -jar target\research-booking-system-0.0.1-SNAPSHOT.jar --server.port=8083

# Terminal 2 — run Selenium tests
.\mvnw.cmd -Dtest=SeleniumJourneyChecks test
```

Reports: `target/surefire-reports/`
Screenshots: `target/selenium-screenshots/`

---

## Everyday Docker Commands

```powershell
# Start container
docker compose up -d

# Stop and remove container
docker compose down

# Restart app container
docker compose restart app

# View live logs
docker logs -f research-booking-app

# Rebuild after code changes
docker compose up -d --build

# List project containers
docker ps --filter "name=research-booking"

# Force remove stuck container
docker rm -f research-booking-app

# View local registry
curl http://localhost:5000/v2/_catalog
```

---

## Troubleshooting

| Problem | Likely cause | Fix |
|---|---|---|
| `Unable to determine Dialect` | MySQL not running or wrong port | Start XAMPP MySQL on port 3307 |
| `Port 8081 already in use` | Old process running | `netstat -ano \| findstr :8081` then kill PID |
| `Port 9091 already in use` | Old CD container | `docker rm -f research-booking-cd` |
| Container exits immediately | MySQL not reachable | Start XAMPP MySQL before Docker |
| `Container name already in use` | Old container not cleaned | `docker rm -f research-booking-app` |
| Teacher login fails | No teacher DB record | Insert user with `role=TEACHER`, `account_status=ACTIVE` |
| Hero illustration not visible | Old JAR without image bundled | `mvn package -DskipTests` then restart app |
| Page 9091 not loading | CD container not running | Run Jenkins Build with Parameters |
| Docker Build fails in Jenkins | Docker Desktop not running | Start Docker Desktop, retry build |

---

## Jenkins CI/CD Details

### Port reference

| Service | Port |
|---|---|
| Jenkins | 8080 |
| Spring Boot (app internal) | 8081 |
| Selenium test run | 8083 |
| Local Docker registry | 5000 |
| CD deployed container (host) | 9091 |

### SCM auto-trigger

```
H/5 * * * *
```

Push any commit to `main` — Jenkins detects it within 5 minutes and runs the full pipeline.

### CD evidence file sample

```
Build 12 | Version 1.0.12 | Commit a1b2c3d | 2026-10-26T18:10:00Z
---- docker ps ----
research-booking-cd   Up 2 minutes   0.0.0.0:9091->8081/tcp
---- HTTP checks ----
api=200 ui=200
```

---

## Selenium UI Testing

The `SeleniumJourneyChecks` class verifies:
- Experiment cards render on the page.
- Experiment details and slot data load.
- Registration form validation works.
- Student registration completes successfully.

### Locators used (do not rename)

| CSS selector | Element |
|---|---|
| `#experiments article.experiment-card` | Experiment card |
| `#experiments article.experiment-card button` | Book Slot button |
| `#details h3` | Experiment title in detail view |

---

## Git Workflow

```powershell
git switch -c feature/my-feature
git add .
git commit -m "feat: describe the change"
git push -u origin feature/my-feature
```

| Branch type | Example |
|---|---|
| Feature | `feature/booking-history` |
| Bug fix | `fix/slot-validation` |
| Documentation | `docs/update-readme` |
| Tests | `test/selenium-registration` |

---

## Future Enhancements

- Complete role-based authorisation for all teacher operations.
- Add teacher screens for creating and managing experiments and slots.
- Improve booking validation and concurrent request handling.
- Expand automated API and UI test coverage.
- Add OpenAPI / Swagger documentation.
- Automate DB migrations with Flyway or Liquibase.

---

## Author

**Swapnil Supe** — PaperLab Research Experiment Booking System

Academic full-stack project · Spring Boot · MySQL · Docker · Jenkins · Selenium

Repository: https://github.com/swapnilsupe01/Research-Experiment-Booking
