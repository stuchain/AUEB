# Manager-Customer Distributed Booking System

A distributed Airbnb-style booking platform with a Java backend and Android frontend.  
The backend follows a Master/Worker/Reducer model to coordinate room search and booking workflows.

## System Architecture

- **Master**: receives client requests and coordinates worker execution.
- **Worker nodes**: process room, search, booking, and review operations.
- **Reducer**: aggregates distributed results.
- **Android frontend**: client interface for user actions and result display.

Key paths:
- Backend: [`DistributedSystems/backend/src/main/java/ds`](DistributedSystems/backend/src/main/java/ds)
- Frontend: [`FrontendFinal/app/src/main/java/com/example/frontend`](FrontendFinal/app/src/main/java/com/example/frontend)
- Worker config: [`DistributedSystems/workers.json`](DistributedSystems/workers.json)

## User Roles

### Managers (Hosts)
- Add and manage property listings.
- Maintain availability and monitor reservations.

### Customers
- Search listings with filters (location, date, price, rating, capacity).
- View listing details and submit booking requests.
- Rate completed stays and manage account-related actions.
- Access the system through the Android client.

## Technologies Used

- [Java](https://www.oracle.com/java/technologies/downloads/) (backend and console tooling)
- [Android Developers](https://developer.android.com/) (mobile frontend)
- [Maven](https://maven.apache.org/) and [Gradle](https://gradle.org/) (build tooling)
- Distributed systems and MapReduce concepts

## Setup and Run

1. Prepare backend:
   - Navigate to `DistributedSystems/backend`
   - Run `mvn clean compile`
2. Start distributed services in order:
   - Start `Reducer` first.
   - Start `Worker` instances (matching `DistributedSystems/workers.json` ports).
   - Start `Master` last.
3. Start clients:
   - Android app from `FrontendFinal` (Android Studio), or
   - Optional console/testing clients from backend package entry points.

## Workflow

1. User authenticates in the client.
2. Client sends search/booking actions to the Master service.
3. Master dispatches work to workers and uses reducer for aggregation when needed.
4. Aggregated results are returned to client for booking/rating actions.

## Configuration Notes

- Ensure worker host/port values align with your environment.
- Validate hardcoded IP/path values in backend/frontend classes before first run.
- Keep backend startup order unchanged to avoid failed socket communication.

## Troubleshooting

- **Connection refused:** verify `Reducer` and all workers are running before `Master`.
- **No results returned:** check `workers.json` and host/port consistency.
- **Frontend cannot reach backend:** update configured host IP and test on same network.

## Notes

- Some IP addresses and file paths are currently hardcoded in backend/frontend classes.
- Service startup order is important for successful communication.
- This repository may include generated build output directories (`build/`, `target/`) from previous runs.