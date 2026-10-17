 
# Research Experiment Booking System

A web-based application developed using Spring Boot and MySQL to manage research experiments, laboratory time slots, booking requests, booking approvals, cancellations, and booking history.

The system provides REST APIs for students and other authorized users to interact with research experiments and manage laboratory bookings.

## Project Overview

Research laboratories require an organized way to manage experiment schedules, laboratory availability, and student booking requests. Manual scheduling can result in conflicts, inefficient resource utilization, and difficulty tracking booking status.

The Research Experiment Booking System addresses these challenges by providing a centralized application for viewing experiments, requesting bookings, confirming or rejecting requests, cancelling bookings, and maintaining a history of booking status changes.

## Objectives

- Provide a centralized system for managing research experiments.
- Display experiment details and available laboratory time slots.
- Allow users to submit booking requests.
- Support booking confirmation, rejection, and cancellation workflows.
- Maintain a history of booking status changes.
- Store application data in a MySQL relational database.
- Provide REST APIs for application operations.

## Features

### Experiment Management
- Retrieve the list of research experiments.
- View individual experiment details.
- Retrieve experiment time slots.
- View upcoming experiment slots.

### Booking Management
- Submit a booking request for an experiment slot.
- Retrieve booking details.
- View bookings associated with a user.
- Confirm or reject pending booking requests.
- Cancel pending or confirmed bookings, subject to application rules.

### Booking History
- Record booking status transitions.
- Store the previous and new booking statuses.
- Record the user responsible for a change.
- Store timestamps and remarks for status changes.

### User Management and Security
- Register users through the registration API.
- Authenticate protected API requests using HTTP Basic authentication.
- Restrict unauthenticated access to protected endpoints through Spring Security.

> Note: Role-based authorization for individual operations should be verified separately. Authentication alone does not establish that every action is restricted to the appropriate role.

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
| Development environment | Windows and PowerShell |
| Version control | Git |
| Repository hosting | GitHub |

## System Architecture

The application follows a layered architecture.

1. **Controller layer:** Receives HTTP requests and returns API responses.
2. **Service layer:** Implements booking, experiment, and user-management logic.
3. **Repository layer:** Uses Spring Data JPA to interact with the database.
4. **Entity layer:** Represents application data as Java objects.
5. **Database layer:** Stores users, experiments, slots, bookings, cancellations, and booking history.

```text
Client / REST API Consumer
           |
           v
     Spring Security
           |
           v
    Controller Layer
           |
           v
      Service Layer
           |
           v
    Repository Layer
           |
           v
       Hibernate / JPA
           |
           v
       MySQL Database
```

## Project Folder Structure

The main source structure is organized as follows. Individual files may vary as the application develops.

```text
research-booking-system/
├── .github/
│   └── ISSUE_TEMPLATE/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── researchbooking/
│   │   │           └── research_booking_system/
│   │   │               ├── config/
│   │   │               ├── controller/
│   │   │               ├── entity/
│   │   │               ├── repository/
│   │   │               ├── service/
│   │   │               ├── HomeController.java
│   │   │               └── ResearchBookingSystemApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/
├── .gitignore
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

## Prerequisites

Install or configure the following before running the application:

- Java Development Kit compatible with the project's configured Java version.
- MySQL Server or XAMPP with MySQL enabled.
- Git.
- A terminal such as PowerShell.
- Maven Wrapper included in the repository.

## Local Installation and Setup

### 1. Clone the repository

Replace the repository URL below if your GitHub username or repository name differs.

```bash
git clone https://github.com/swapnilsupe01/Research-Experiment-Booking.git
cd Research-Experiment-Booking/research-booking-system
```

If the GitHub repository itself contains the application root directly, use the directory created by `git clone` instead of the nested path above.

### 2. Configure MySQL

Create a database named:

```sql
CREATE DATABASE research_booking_system;
```

Configure the database connection in `src/main/resources/application.properties`.

Example configuration for the current local setup:

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

**Important:** The application uses `ddl-auto=validate`, so the required database tables and schema must exist before startup. Configure the correct database port, username, password, and schema for your environment. Do not commit production credentials to GitHub.

### 3. Build and test the application

On Windows PowerShell, run:

```powershell
.\mvnw.cmd clean test
```

To build the application package:

```powershell
.\mvnw.cmd package
```

### 4. Start the application

```powershell
.\mvnw.cmd spring-boot:run
```

When startup succeeds, the local application runs on:

```text
http://localhost:8081
```

## REST API Endpoints

The following endpoints are implemented in the current application.

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/users/register` | Register a user |
| GET | `/api/experiments` | Retrieve experiments |
| GET | `/api/experiments/{experimentId}` | Retrieve experiment details |
| GET | `/api/experiments/{experimentId}/slots` | Retrieve experiment slots |
| GET | `/api/experiments/slots/upcoming` | Retrieve upcoming slots |
| POST | `/api/bookings` | Create a booking request |
| GET | `/api/bookings/user/{userId}` | Retrieve bookings for a user |
| GET | `/api/bookings/{bookingId}` | Retrieve a booking |
| PUT | `/api/bookings/{bookingId}/status` | Update booking status |
| PUT | `/api/bookings/{bookingId}/cancel` | Cancel a booking |
| GET | `/api/bookings/{bookingId}/history` | Retrieve booking status history |

Protected endpoints require authentication according to the current Spring Security configuration.

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
       v
Confirm or Reject
       |
       +------> CONFIRMED
       |             |
       |             v
       |          CANCELLED
       |
       +------> REJECTED
```

The exact permitted transitions depend on the service-layer business rules. Booking history records the status changes performed by the application.

## Testing

Run the automated tests using:

```powershell
.\mvnw.cmd test
```

The application has been tested locally for the following operations:

- Application startup.
- Experiment and time-slot retrieval.
- User registration and authenticated requests.
- Booking creation.
- Booking confirmation.
- Booking cancellation.
- Booking status history retrieval.

The final recorded Maven test run completed successfully with one automated test, zero failures, zero errors, and zero skipped tests.

## Git and GitHub Workflow

The project uses Git for version control and GitHub for repository hosting.

### Branch Naming Convention

| Branch type | Naming convention | Example |
|---|---|---|
| Main branch | `main` | `main` |
| Feature | `feature/description` | `feature/booking-history` |
| Bug fix | `fix/description` | `fix/slot-validation` |
| Documentation | `docs/description` | `docs/update-readme` |
| Tests | `test/description` | `test/booking-workflow` |

### Commit Message Convention

Use meaningful commit messages that describe the change.

Examples:

```text
chore: initialize Spring Boot project
docs: add project setup instructions
feat: implement experiment retrieval API
feat: add booking cancellation workflow
test: verify booking service behavior
fix: correct booking status validation
```

Create a feature branch before starting a new change:

```bash
git switch -c feature/booking-history
```

Commit your changes and push the branch:

```bash
git add .
git commit -m "feat: improve booking history tracking"
git push -u origin feature/booking-history
```

Use pull requests to review changes before merging them into `main`. Repository branch protection should be configured in GitHub if required by the project policy.

## Issue Tracking

GitHub Issues can be used to track development work, bugs, enhancements, and testing tasks.

Suggested initial issues:

1. Document the local installation and database setup.
2. Verify booking slot availability and capacity validation.
3. Improve role-based authorization for booking status changes.
4. Add automated tests for booking creation and cancellation.
5. Document the REST API request and response examples.

Issue templates can be maintained under `.github/ISSUE_TEMPLATE/`.

## Future Enhancements

- Role-based authorization for student and supervisor operations.
- Improved validation and error responses.
- Automated integration tests for REST endpoints.
- API documentation using OpenAPI/Swagger.
- Improved handling of concurrent booking requests.
- A web-based user interface for experiment and booking management.

## License

No license has been specified yet. Add an appropriate `LICENSE` file if the project is intended for public reuse.

## Author

Research Experiment Booking System — academic software project.

For project information, setup instructions, or development tasks, refer to this repository's documentation and GitHub Issues.
