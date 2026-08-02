# Farm Management System

Biosecurity plan tracking and animal movement records for farms, with role-based access for producers, reviewers, and state officials.

## What it does

- Producers register farms and submit biosecurity plans (fencing, visitor logs, disinfection protocols) for review
- Reviewers approve or reject submitted plans
- State officials get read-only visibility into farms/plans within their own state
- Movement records track animal shipments between farms (farm-to-farm traceability)

## Stack

- **Backend**: Spring Boot 4.1 (Java 17, Maven), Spring Security + JWT, Spring Data JPA, H2 (in-memory)
- **Frontend**: Angular 21 (standalone components, zoneless), plain CSS

## Running locally

**Backend** (port 8080):

```bash
cd backend
./mvnw spring-boot:run
```

Seeds a fresh set of demo users on every boot (printed to console), e.g. `producer1` / `password123`. H2 console at `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:farmdb`).

**Frontend** (port 4200):

```bash
cd frontend
npm install
npm start
```

Open `http://localhost:4200` and log in with any seeded user.

## Architecture

See `ARCHITECTURE.md` for the JWT auth flow (login → token issuance → request validation → role enforcement).
