# Teacher Backend Audit — Research Experiment Booking System

**Audit date:** 2026-10-20  
**Auditor:** AI coding agent  
**Project root:** `research-booking-system/`

---

## Summary

The existing backend provides a functional student-facing application with experiments, slots, bookings, and user registration. **Teacher-specific features are PARTIALLY implemented** — the data model and some services exist, but dedicated teacher CRUD endpoints for experiments and slots are MISSING. Booking status management (approve/reject) IS implemented but lacks server-side role enforcement.

---

## Feature-by-Feature Audit

### 1. Teacher Login

| Attribute | Detail |
|---|---|
| **Status** | **IMPLEMENTED** |
| **File** | `service/CustomUserDetailsService.java`, `config/SecurityConfig.java` |
| **Mechanism** | HTTP Basic auth via Spring Security. `CustomUserDetailsService` loads users by email, maps `user.role` to a Spring Security granted authority via `.roles(user.role)`. |
| **Roles stored** | `User.role` field stores `"STUDENT"` (set by registration) or `"TEACHER"` (must be pre-seeded in DB). |
| **Evidence** | `CustomUserDetailsService` line 31: `.roles(user.role)` — if role is `"TEACHER"`, the granted authority becomes `ROLE_TEACHER`. |
| **Missing** | No teacher registration endpoint — teachers must be created directly in MySQL. No form-based login page; only HTTP Basic auth is configured. |

### 2. Teacher Dashboard

| Attribute | Detail |
|---|---|
| **Status** | **MISSING** |
| **File** | N/A |
| **Endpoint** | No dedicated dashboard API or controller for teacher dashboard data. |
| **Missing** | No endpoint aggregating experiment count, slot count, or booking counts for a teacher. The frontend can compose this from existing APIs. |

### 3. View Experiments

| Attribute | Detail |
|---|---|
| **Status** | **IMPLEMENTED** |
| **File** | `controller/ExperimentController.java`, `service/ExperimentService.java` |
| **HTTP** | `GET /api/experiments` — returns all active experiments. `GET /api/experiments/{id}` — returns one experiment. |
| **Response** | JSON array/object with fields: `experimentId`, `title`, `description`, `laboratoryName`, `supervisorId`, `durationMinutes`, `active`, `createdAt`. |
| **Auth** | `GET /api/experiments/**` is `permitAll()` — no authentication required. |
| **Missing** | No endpoint to list experiments by supervisor (teacher). The existing `findByActiveTrue()` returns ALL active experiments, not filtered by teacher. A teacher dashboard needs `findBySupervisorId(Long)`. |

### 4. Create Experiment

| Attribute | Detail |
|---|---|
| **Status** | **MISSING** |
| **File** | `ExperimentController.java` has only GET mappings. `ExperimentService.java` has only read methods. |
| **Endpoint** | No `POST /api/experiments` endpoint. |
| **Entity** | `Experiment` entity has all required fields: `title`, `description`, `laboratoryName`, `supervisorId`, `durationMinutes`, `active`. |
| **Missing** | POST endpoint, service create method, input validation, and teacher authorization. |

### 5. Edit Experiment

| Attribute | Detail |
|---|---|
| **Status** | **MISSING** |
| **File** | No PUT/PATCH mapping in `ExperimentController.java`. |
| **Endpoint** | No `PUT /api/experiments/{id}` endpoint. |
| **Missing** | PUT endpoint, service update method, ownership validation (ensure only the supervising teacher can edit). |

### 6. Delete Experiment

| Attribute | Detail |
|---|---|
| **Status** | **MISSING** |
| **File** | No DELETE mapping in `ExperimentController.java`. |
| **Endpoint** | No `DELETE /api/experiments/{id}` endpoint. |
| **Missing** | DELETE endpoint, service delete method, ownership check, cascade/integrity rules for existing slots and bookings. |

### 7. Create Experiment Slots

| Attribute | Detail |
|---|---|
| **Status** | **MISSING** |
| **File** | `ExperimentSlot` entity exists. `ExperimentSlotRepository` exists with `findByExperimentId()` and `findByStartTimeAfter()`. |
| **Endpoint** | No `POST /api/experiments/{id}/slots` endpoint. |
| **Entity** | `ExperimentSlot` has fields: `slotId`, `experimentId`, `startTime`, `endTime`, `capacity`, `createdAt`. |
| **Missing** | POST endpoint, service create method, validation (start < end, future time, positive capacity), teacher authorization. |

### 8. View/Manage Slots

| Attribute | Detail |
|---|---|
| **Status** | **PARTIAL** |
| **File** | `controller/ExperimentController.java`, `service/ExperimentService.java` |
| **Existing** | `GET /api/experiments/{id}/slots` — returns future slots. `GET /api/experiments/slots/upcoming` — all upcoming slots. |
| **Missing** | No PUT endpoint to update a slot. No DELETE endpoint to remove a slot. The existing GET filters by future time only (no historical slots visible to teachers). |

### 9. View Booking Requests

| Attribute | Detail |
|---|---|
| **Status** | **PARTIAL** |
| **File** | `controller/BookingController.java`, `service/BookingService.java` |
| **Existing** | `GET /api/bookings/user/{userId}` — bookings for a user. `GET /api/bookings/{bookingId}` — single booking. `BookingRepository.findByStatus(String)` exists in the repository but is not exposed via controller. `BookingRepository.findBySlotId(Long)` exists but is only used internally. |
| **Missing** | No endpoint to list all bookings for a teacher's experiments. No `GET /api/bookings?status=PENDING` or similar filtering endpoint. |

### 10. Approve/Reject Bookings

| Attribute | Detail |
|---|---|
| **Status** | **IMPLEMENTED** |
| **File** | `controller/BookingController.java` line 58, `service/BookingService.java` line 72 |
| **HTTP** | `PUT /api/bookings/{bookingId}/status` |
| **Request** | JSON: `{ "changedBy": <userId>, "status": "CONFIRMED" or "REJECTED", "remarks": "optional" }` |
| **Business rules** | Only `PENDING` bookings can be confirmed/rejected. On `CONFIRMED`, capacity is checked. Status history is recorded. |
| **Auth** | Requires authentication but does NOT check that the authenticated user is a teacher or the experiment's supervisor. |

### 11. View Booking History

| Attribute | Detail |
|---|---|
| **Status** | **IMPLEMENTED** |
| **File** | `controller/BookingController.java` line 94, `service/BookingService.java` line 154 |
| **HTTP** | `GET /api/bookings/{bookingId}/history` |
| **Response** | JSON array of `BookingStatusHistory` objects. |

### 12. Role-Based Security

| Attribute | Detail |
|---|---|
| **Status** | **PARTIAL** |
| **File** | `config/SecurityConfig.java` |
| **Implemented** | `permitAll()` for registration and GET experiments. `.anyRequest().authenticated()` for everything else. `CustomUserDetailsService` assigns Spring Security roles based on `user.role`. |
| **Missing** | No `.hasRole("TEACHER")` rules on any endpoint. Teacher operations are not restricted to the TEACHER role. |

### 13. Database Integration

| Attribute | Detail |
|---|---|
| **Status** | **IMPLEMENTED** |
| **Entities** | `User`, `Experiment`, `ExperimentSlot`, `Booking`, `BookingCancellation`, `BookingStatusHistory`. |
| **Config** | `ddl-auto=validate` — schema must pre-exist. MySQL on port 3307. |
| **Repositories** | All six JPA repositories exist and extend `JpaRepository`. |

---

## Backend Changes Required

### New Endpoints Needed

| Priority | Endpoint | Method | Purpose |
|---|---|---|---|
| HIGH | `/api/experiments` | POST | Create experiment |
| HIGH | `/api/experiments/{id}` | PUT | Update experiment |
| HIGH | `/api/experiments/{id}` | DELETE | Delete experiment |
| HIGH | `/api/experiments/{id}/slots` | POST | Create slot |
| MEDIUM | `/api/experiments/slots/{slotId}` | PUT | Update slot |
| MEDIUM | `/api/experiments/slots/{slotId}` | DELETE | Delete slot |
| HIGH | `/api/bookings` | GET | List all bookings (with filters) |
| MEDIUM | `/api/experiments/supervisor/{userId}` | GET | Experiments by supervisor |

### Required Repository Methods

| Repository | Method | Purpose |
|---|---|---|
| `ExperimentRepository` | `findBySupervisorId(Long)` | Teacher's own experiments |
| `BookingRepository` | `findBySlotIdIn(List<Long>)` | Bookings across teacher's slots |

### Required Security Changes

Add role-based rules to `SecurityConfig`:
- `POST/PUT/DELETE /api/experiments/**` requires `ROLE_TEACHER`
- `POST/PUT/DELETE` on slots requires `ROLE_TEACHER`
- `PUT /api/bookings/{id}/status` requires `ROLE_TEACHER`
