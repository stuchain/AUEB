# Manager-Customer Distributed Booking System

A distributed Airbnb-style booking platform with a Java backend and Android frontend.  
The backend follows a Master/Worker/Reducer model to coordinate room search and booking workflows.

## System Architecture

- **Backend (`DistributedSystems/backend`)**  
  Components include `Master`, `Worker`, and `Reducer` services connected through sockets.
- **Frontend (`FrontendFinal`)**  
  Android application for customer login, search, booking, and review flows.
- **Data processing model**  
  MapReduce-inspired distribution for search and aggregation tasks.

Key paths:
- Backend: [`DistributedSystems/backend/src/main/java/ds`](DistributedSystems/backend/src/main/java/ds)
- Frontend: [`FrontendFinal/app/src/main/java/com/example/frontend`](FrontendFinal/app/src/main/java/com/example/frontend)

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

1. Build backend:
   - Navigate to `DistributedSystems/backend`
   - Run `mvn clean compile`
2. Start backend services in order:
   - `Reducer`
   - `Worker` instances (ports should match `DistributedSystems/workers.json`)
   - `Master`
3. Open `FrontendFinal` in Android Studio and run the app on an emulator/device.

## Notes

- Some IP addresses and file paths are currently hardcoded in backend/frontend classes.
- Service startup order is important for successful communication.
- This repository may include generated build output directories (`build/`, `target/`) from previous runs.