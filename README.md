# ACME Salary Management System

A web app for an HR Manager to manage salaries of 10,000 employees across multiple countries — replacing Excel-based tracking.

**Tech:** Spring Boot 4 (Java 17) · SQLite · Angular 21 · JWT auth

---

## Prerequisites

| Tool | Version | Check with |
|------|---------|-----------|
| Java | 17+ | `java -version` |
| Maven | 3.8+ | `mvn -version` |
| Node.js | 20+ | `node -v` |

---

## 1. Start the backend (terminal 1)

```powershell
cd backend
mvn spring-boot:run
```

- First run takes 1–2 minutes (downloads jars, creates tables, seeds data).
- On first startup it automatically creates:
  - **10,000 employees** across 8 countries
  - **8 currencies** with USD conversion rates
  - **HR admin login** (below)
- Server runs at `http://localhost:8080`

> To reset all data: stop the app, delete `backend/salary-management.db`, and start again.

## 2. Start the frontend (terminal 2)

```powershell
cd frontend
npm install
npm start
```

- Open **http://localhost:4200/login** in your browser.

## 3. Log in

| Email | Password |
|-------|----------|
| `hr@company.com` | `Password123!` |

You can now: search/filter/sort employees by country, add and edit employees, deactivate them, and see country-wise and global **median salary in USD**.

---

## Run the tests

```powershell
# Backend (26 tests)
cd backend
mvn test

# Frontend (12 tests)
cd frontend
npx ng test --watch=false
```

---

## Project structure

```
├── docs/        requirements, ER diagram, HLD, API design
├── backend/     Spring Boot REST API (JWT auth, SQLite, seed script)
└── frontend/    Angular app (login, employee list, add/edit form)
```

## Troubleshooting

- **Login does nothing / CORS error** → make sure the backend is running on port 8080.
- **Port already in use** → stop the other process or change `server.port` in `backend/src/main/resources/application.properties`.
- **Tables look wrong / stale data** → delete `backend/salary-management.db` and restart the backend.
- **Frontend shows old UI** → hard refresh (`Ctrl+Shift+R`) or restart `npm start`.
