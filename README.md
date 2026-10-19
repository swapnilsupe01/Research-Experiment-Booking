# Research Experiment Booking System

A web-based application developed using Spring Boot and MySQL to manage research experiments, laboratory time slots, booking requests, approvals, cancellations, and booking history.

The project includes REST APIs, a web UI, automated Selenium tests, Jenkins CI/CD integration, Nginx reverse proxy support, and Docker-based deployment.

## Project Overview

Research laboratories need an organized way to manage experiment schedules, laboratory availability, and student booking requests. Manual scheduling can cause conflicts and make it difficult to track bookings.

The Research Experiment Booking System provides a centralized application for viewing experiments, registering students, requesting bookings, managing booking statuses, and recording booking history.

## Objectives

- Centralize research experiment and laboratory slot information.
- Display experiment details and available time slots.
- Allow students to register and submit booking requests.
- Support booking confirmation, rejection, and cancellation.
- Maintain a history of booking status changes.
- Store application data in a MySQL relational database.
- Automate build and testing through Jenkins.
- Support deployment through Docker and Nginx.

## Features

### Experiment Management
- Retrieve active experiments.
- View experiment details.
- Retrieve experiment time slots.
- View upcoming slots.

### Booking Management
- Submit booking requests.
- Retrieve booking details and user bookings.
- Confirm or reject pending booking requests.
- Cancel eligible bookings.
- Validate future slots and booking capacity.

### Booking History
- Record previous and new booking statuses.
- Record the user associated with a status change.
- Maintain timestamps and remarks.

### User Management and Security
- Register students through the registration API.
- Authenticate protected requests using Spring Security HTTP Basic authentication.
- Require authentication for protected endpoints.

**Security note:** Authentication and role-based authorization are different. Teacher-only operations must be protected by backend role checks before the application can be considered fully role-secured.

## Technology Stack

| Component | Technology |
|---|---|
| Programming language | Java |
| Backend framework | Spring Boot |
| Build tool | Maven |
| Database | MySQL |
| Data persistence | Spring Data JPA |
| ORM | Hibernate |
| Security | Spring Security |
| API style | REST API |
| Application server | Embedded Apache Tomcat |
| Reverse proxy | Nginx |
| Containerization | Docker |
| CI/CD | Jenkins |
| Automated UI testing | Selenium |
| Version control | Git |
| Repository hosting | GitHub |
| Development environment | Windows, PowerShell |

## System Architecture

The application follows a layered architecture:

1. **Controller layer:** Handles HTTP requests and responses.
2. **Service layer:** Implements application and booking business logic.
3. **Repository layer:** Accesses data through Spring Data JPA.
4. **Entity layer:** Represents users, experiments, slots, and bookings.
5. **Database layer:** Stores application records in MySQL.

The application can be accessed directly through Spring Boot, through Nginx, or through the Docker CD deployment, depending on which services are running.

## Application URLs and Deployment

Use the URL that corresponds to the deployment you want to access.

| Deployment | URL | Description |
|---|---|---|
| Direct Spring Boot | `http://localhost:8081/ui` | Opens the UI served by the local Spring Boot application. |
| Nginx reverse proxy | `http://localhost:8082/ui` | Opens the UI through Nginx, which forwards requests to the Spring Boot application. |
| Docker CD deployment | `http://localhost:9091/ui` | Opens the UI served by the deployed Docker container. |
| Jenkins | `http://localhost:8080` | Opens Jenkins to view pipeline builds, test results, and deployment logs. |

**Important:** These URLs work only when their corresponding services are running. The Docker deployment on port `9091` is separate from the direct application on port `8081`.

### Direct Spring Boot Access

Start the application locally:

```powershell
.\mvnw.cmd spring-boot:run
```

When startup succeeds and the application is listening on port `8081`, open:

```text
http://localhost:8081/ui
```

### Nginx Reverse Proxy Access

Nginx is configured to receive requests on port `8082` and forward them to the Spring Boot application on port `8081`.

Open:

```text
http://localhost:8082/ui
```

The Spring Boot application must be running on the configured upstream port for the proxied UI to work.

### Docker Deployment Access

The Jenkins CD deployment uses the Docker image and container configuration established by the deployment pipeline.

The deployed container is named `research-booking-cd` and is exposed on host port `9091`.

Open:

```text
http://localhost:9091/ui
```

The deployment previously used the image tag:

```text
localhost:5000/research-booking-system:1.0.20
```

The image tag can change with subsequent builds. Use the tag recorded in the relevant Jenkins build log rather than assuming this tag is always the latest.

## Prerequisites

Install or configure:

- A compatible Java Development Kit.
- MySQL Server or XAMPP with MySQL enabled.
- Git.
- PowerShell or another terminal.
- Docker Desktop when building or running containers locally.
- Jenkins for CI/CD.
- Nginx if using the reverse proxy.
- The Maven Wrapper included in the repository.

## Local Installation and Setup

### 1. Clone the repository

```powershell
git clone https://github.com/swapnilsupe01/Research-Experiment-Booking.git
cd Research-Experiment-Booking/research-booking-system
```

If the application root is directly inside the cloned repository, use the actual directory created by Git instead of assuming a nested folder exists.

### 2. Configure MySQL

Create the database if it does not already exist:

```sql
CREATE DATABASE research_booking_system;
```

Configure `src/main/resources/application.properties` for your environment.

Example:

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

Adjust the database port and credentials to match your MySQL installation.

The `ddl-auto=validate` setting checks the existing database schema. It does not automatically create missing tables. The required schema must exist before startup.

Do not commit production credentials to GitHub.

### 3. Build and test

```powershell
.\mvnw.cmd clean test
```

### 4. Package the application

```powershell
.\mvnw.cmd package
```

### 5. Start the application

```powershell
.\mvnw.cmd spring-boot:run
```

After successful startup, open `http://localhost:8081/ui`.

## REST API Endpoints

The following endpoints are implemented in the current application.

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/users/register` | Register a student |
| GET | `/api/experiments` | Retrieve active experiments |
| GET | `/api/experiments/{experimentId}` | Retrieve experiment details |
| GET | `/api/experiments/{experimentId}/slots` | Retrieve slots for an experiment |
| GET | `/api/experiments/slots/upcoming` | Retrieve upcoming slots |
| POST | `/api/bookings` | Submit a booking request |
| GET | `/api/bookings/user/{userId}` | Retrieve bookings for a user |
| GET | `/api/bookings/{bookingId}` | Retrieve a booking |
| PUT | `/api/bookings/{bookingId}/status` | Confirm or reject a pending booking |
| PUT | `/api/bookings/{bookingId}/cancel` | Cancel an eligible booking |
| GET | `/api/bookings/{bookingId}/history` | Retrieve booking status history |

Protected endpoints require authentication according to the current Spring Security configuration. Authentication does not automatically enforce teacher-specific permissions.

## Booking Workflow

A typical booking lifecycle is:

```text
Select Experiment
       |
       v
Select Available Slot
       |
       v
Submit Booking Request
       |
       v
    PENDING
       |
       +----------> CONFIRMED
       |                 |
       |                 v
       |             CANCELLED
       |
       +----------> REJECTED
```

The service validates whether the slot is in the future and checks its confirmed-booking capacity. Booking history records status transitions performed by the application.

## Jenkins CI/CD Integration

Jenkins automates the build, test, and deployment workflow.

Jenkins URL:

```text
http://localhost:8080
```

The project uses the GitHub repository:

```text
https://github.com/swapnilsupe01/Research-Experiment-Booking.git
```

The configured SCM polling schedule is:

```text
H/5 * * * *
```

This schedules repository polling approximately every five minutes, with Jenkins distributing the exact minute using its hash-based scheduling.

### Jenkins SCM Polling Test

The purpose of this test is to verify that Jenkins detects changes in the GitHub repository and starts the configured pipeline.

#### Step 1: Make a small test change

Edit a harmless README line, for example:

```text
Documentation updated to verify Jenkins SCM polling.
```

#### Step 2: Commit and push the change

```powershell
git add README.md
git commit -m "docs: verify Jenkins SCM polling"
git push origin main
```

Run these commands from the repository's Git working directory.

#### Step 3: Check Jenkins

1. Open `http://localhost:8080`.
2. Open the `Research-Experiment-Booking` Jenkins job.
3. Check the build history after the polling interval.
4. Open the new build and inspect **Console Output**.
5. Verify that Jenkins detected the repository change and started the pipeline.

If the build does not start, check the SCM polling configuration, repository URL, branch configuration, Jenkins credentials, and console logs.

#### Step 4: Verify the pipeline result

Confirm the following, where configured in the pipeline:

- The repository change was detected.
- The source code was checked out.
- Maven build and test stages completed.
- Selenium test reports were published.
- Deployment proceeded only after required tests passed.
- The deployment verification stage completed successfully.

A successful manual build alone does not prove SCM polling works. The test must demonstrate that a pushed repository change is detected and triggers a build.

## Selenium UI Testing

The Selenium suite checks important UI workflows, including:

- Experiment cards displayed on the dashboard.
- Experiment details and slot information loaded.
- Required registration fields validated.
- Student registration completed successfully.

Run the Selenium suite locally with:

```powershell
mvn "-Dtest=SeleniumJourneyChecks" test
```

The default Selenium base URL is `http://localhost:8083/ui`, as configured in the test class. Ensure the application under test is available at that URL before running the suite.

The test reports are generated under:

```text
target/surefire-reports/
```

Failure screenshots, when generated, are saved under:

```text
target/selenium-screenshots/
```

The Jenkins pipeline should publish the JUnit XML reports and stop deployment when required tests fail.

## Git Workflow

Suggested branch naming conventions:

| Type | Example |
|---|---|
| Feature | `feature/booking-history` |
| Bug fix | `fix/slot-validation` |
| Documentation | `docs/update-readme` |
| Tests | `test/booking-workflow` |

Example:

```powershell
git switch -c docs/update-readme
git add README.md
git commit -m "docs: update deployment and Jenkins instructions"
git push -u origin docs/update-readme
```

Use the repository's normal review and merge process before merging changes into `main`.

Ansible Configuration Management (Task 14).
1. Task 14 Overview — Configuration management using Ansible on Ubuntu (WSL).
2. Tools Used — Ubuntu/WSL, Ansible, localhost, sudo, and systemd.
3. Directory Structure — Task14-Configuration/ containing:
   - playbook.yml — Ansible automation.
   - inventory.ini — Target host configuration.
   - execution-log.txt — Successful execution log.
4. Ubuntu Setup Steps — Commands to navigate to the project directory and verify Ansible installation.
5. Ansible Connectivity Test — Verify the localhost connection using ansible -i inventory.ini localhost -m ping.
6. Playbook Execution — Run the playbook using ansible-playbook -i inventory.ini playbook.yml --ask-become-pass.
7. Execution Logging — Save output using tee execution-log.txt.
8. Configuration Verification — Display /etc/research-booking/application.conf and verify its contents.
9. Service Verification — Confirm that the cron service is active using systemctl is-active cron.
10. User and Directory Verification — Check the researchapp user and the application, configuration, and log directories.
11. Execution Results — Document ok=7, changed=0, unreachable=0, and failed=0.
12. College Submission Checklist — Include the three required files and screenshots of the successful execution.

## Future Enhancements

- Complete role-based authorization for student and teacher operations.
- Add teacher experiment and slot management screens.
- Improve booking validation and error handling.
- Expand automated API and UI tests.
- Add OpenAPI/Swagger documentation.
- Improve handling of concurrent booking requests.
- Automate configuration management for deployment prerequisites.

## License

No license has been specified yet. Add an appropriate `LICENSE` file if the project is intended for public reuse.

## Author

Research Experiment Booking System — academic software project.

Refer to the repository's documentation, Jenkins build history, and GitHub Issues for project setup and development tasks.
