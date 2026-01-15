\# STUvents - Full Stack Event Management Platform



!\[Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge\&logo=openjdk\&logoColor=white)

!\[Spring Boot](https://img.shields.io/badge/Spring\_Boot-F2F4F9?style=for-the-badge\&logo=spring-boot)

!\[React](https://img.shields.io/badge/React-20232A?style=for-the-badge\&logo=react\&logoColor=61DAFB)

!\[PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge\&logo=postgresql\&logoColor=white)

!\[Docker](https://img.shields.io/badge/Docker-2CA5E0?style=for-the-badge\&logo=docker\&logoColor=white)



STUvents is a comprehensive event ticketing and management platform designed to connect students with local events. It features a complete Role-Based Access Control (RBAC) system, interactive mapping, ticket purchasing simulation, and organizer dashboards.



> \*\* Deployment Status:\*\*

> This application was previously live, deployed with a \*\*Spring Boot Backend on Render\*\* and a \*\*React Frontend on Vercel\*\*. It is currently offline to conserve resources. The application is fully \*\*Dockerized\*\* and can be run locally.



---



\##  Project Workflows \& Role-Based Access



This project implements a strict hierarchy of roles: \*\*User\*\*, \*\*Organizer\*\*, and \*\*Admin\*\*.



\### 1. User Workflow

\*The foundation of the platform. Every registered account starts here.\*



\*\*Capabilities:\*\*

\*   \*\*Event Discovery:\*\* Infinite scroll through events, search by name, and filter by category/city.

\*   \*\*Geolocation:\*\* Interactive \*\*Google Maps API\*\* integration to find events on the map.

\*   \*\*Ticketing:\*\* Buy tickets (Simulated payment + Availability checks).

\*   \*\*Profile:\*\* Update personal details and upload profile picture (via Cloudinary).

\*   \*\*History:\*\* View "My Bookings" list.

\*   \*\*Organizer Application:\*\* Apply to become an event Organizer.



<!-- DRAG AND DROP YOUR USER VIDEO HERE -->





\### 2. Organizer Workflow

\*Inherits all User features, plus management capabilities.\*



\*\*Capabilities:\*\*

\*   \*\*Dashboard:\*\* Access to the specialized Organizer Dashboard.

\*   \*\*Event Management:\*\* Create new events, Edit existing events, Delete events.

\*   \*\*Analytics:\*\* View real-time statistics (Total Revenue, Tickets Sold vs Capacity).

\*   \*\*Attendees:\*\* View a list of users who purchased tickets for their specific events.



<!-- DRAG AND DROP YOUR ORGANIZER VIDEO HERE -->





\### 3. Admin Workflow

\*Inherits all User and Organizer features, plus platform oversight.\*



\*\*Capabilities:\*\*

\*   \*\*Application Review:\*\* Review pending Organizer applications. Read the "Reason" submitted by users and \*\*Approve\*\* (automatically grants role) or \*\*Deny\*\*.

\*   \*\*User Management:\*\* View all registered users and manually change roles (Promote/Demote).

\*   \*\*Platform Filters:\*\* Create or Delete Categories and Cities available in the app.



<!-- DRAG AND DROP YOUR ADMIN VIDEO HERE -->





---



\##  Tech Stack



\### Backend

\*   \*\*Framework:\*\* Java 17, Spring Boot 3

\*   \*\*Security:\*\* Spring Security (JWT Auth, Google OAuth2, Role-Based Access)

\*   \*\*Database:\*\* PostgreSQL (Production), H2 (Testing)

\*   \*\*External Services:\*\*

&nbsp;   \*   \*\*Google Maps API:\*\* Geocoding addresses.

&nbsp;   \*   \*\*Cloudinary:\*\* Image hosting.

&nbsp;   \*   \*\*Brevo:\*\* Transactional emails (Verification/Forgot Password).



\### Frontend

\*   \*\*Framework:\*\* React (Vite)

\*   \*\*Styling:\*\* CSS Modules / Tailwind

\*   \*\*State:\*\* React Context

\*   \*\*HTTP Client:\*\* Axios



\### DevOps

\*   \*\*Containerization:\*\* Fully Dockerized (Dockerfile + Docker Compose).

\*   \*\*Hosting History:\*\* Render (Backend/DB) \& Vercel (Frontend).



---



\##  Testing \& Quality Assurance



Reliability was a key focus of development. The application includes a comprehensive test suite.



\*   \*\*Backend Testing:\*\*

&nbsp;   \*   \*\*JUnit 5 \& Mockito:\*\* Used for isolated service-layer testing.

&nbsp;   \*   \*\*Coverage:\*\* Unit tests cover booking logic, role assignment, ticket availability calculations, and security constraints.

\*   \*\*Frontend Testing:\*\*

&nbsp;   \*   \*\*Unit Tests:\*\* Ensures components render correctly and state updates behave as expected.



---



\##  Getting Started



1\.  \*\*Clone the repository\*\*

2\.  \*\*Configure Environment Variables\*\* (Create a `.env` file with DB, Cloudinary, and Google Maps keys).

3\.  \*\*Run with Docker:\*\*

&nbsp;   ```bash

&nbsp;   docker-compose up --build

&nbsp;   ```

4\.  \*\*Access:\*\*

&nbsp;   \*   Frontend: `http://localhost:5173`

&nbsp;   \*   Backend: `http://localhost:8080`



---



\##  Contact



\*\*\[Your Name]\*\*

\*   \[Your Portfolio/LinkedIn Link]

\*   \[Your Email]

