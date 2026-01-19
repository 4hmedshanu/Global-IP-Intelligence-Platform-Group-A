# Global IP Intelligence Platform

A full-stack web application for IP intelligence monitoring and analysis, featuring role-based access control, real-time analytics, and comprehensive user management.

## 📋 Overview

The Global IP Intelligence Platform is a comprehensive solution for monitoring, analyzing, and managing IP-related intelligence data. It provides different interfaces for Admins, Analysts, and Users, each with role-specific features and capabilities.

## 🎯 Project Milestones

### ✅ Milestone 1: Week 1 & 2 — Authentication & Setup (COMPLETED)

**Objectives:**
- Define roles: User, Analyst, Admin
- Initialize Spring Boot backend and React frontend
- Implement JWT and OAuth2 authentication
- Create registration/login UI and profile management

**Completed Tasks:**

#### Backend Implementation
- ✅ Spring Boot 3.5.5 project setup with Maven
- ✅ PostgreSQL database configuration and integration
- ✅ User entity with role-based access (USER, ANALYST, ADMIN)
- ✅ JWT token generation and validation service
- ✅ OAuth2 Google Sign-In integration
- ✅ Authentication REST endpoints (`/api/auth/register`, `/api/auth/login`)
- ✅ Spring Security configuration with role-based authorization
- ✅ Password encryption using BCrypt
- ✅ Custom UserDetailsService implementation
- ✅ CORS configuration for frontend integration

#### Frontend Implementation
- ✅ React 19.2.1 application setup with React Router 7.10.1
- ✅ Tailwind CSS 3.4.18 integration for responsive design
- ✅ Dark mode support with ThemeContext
- ✅ Login page with email/password and Google OAuth2 button
- ✅ Registration page with role selection dropdown
- ✅ OAuth2RedirectHandler component for Google auth callback
- ✅ ProtectedRoute component for route guarding
- ✅ JWT token management (localStorage)
- ✅ Axios interceptor for automatic authentication headers
- ✅ Toast notification system for user feedback
- ✅ DarkModeToggle component

#### UI Components Created
- ✅ LandingPage with feature showcase
- ✅ UserDashboard with personalized interface
- ✅ AnalystDashboard with analytics tools preview
- ✅ AdminDashboard with user management interface
- ✅ Profile management module
- ✅ Responsive navigation with role-based routing

**Outcome:**
- ✅ **Auth endpoints**: Fully functional registration and login APIs
- ✅ **Role-based login**: JWT authentication with USER, ANALYST, ADMIN roles
- ✅ **Profile module**: Role-specific dashboards with protected routes
- ✅ Secure backend with Spring Security and BCrypt password hashing
- ✅ Modern, responsive UI with dark mode support
- ✅ OAuth2 Google Sign-In working seamlessly

---

### ✅ Milestone 2: Week 3 & 4 — Search & Integration (COMPLETED)

**Objectives:**
- Integrate WIPO, USPTO, EPO, TMView APIs for global IP data
- Build a robust search UI with advanced filters (keywords, inventor, assignee, jurisdiction)
- Store search results and enable detailed IP asset views for users

#### ✅ Key Features Delivered in Milestone 2

- **Search Page**: Unified search interface for patents and trademarks. (Frontend: `SearchPage.jsx`)
- **Patent Results with Keyword**: Search patents by keyword using `/api/search/all` and `/api/search/source/USPTO|EPO|WIPO`.
- **Full Details of Patent**: View complete patent info via `/api/search/asset/{assetId}` or `/api/search/asset/{source}/{externalId}`.
- **Filters On**: Enable filters (date, jurisdiction, assignee, inventor) for all search types. Filters are sent in the search request to backend APIs.
- **Date Filter Working**: Date range filters (`fromDate`, `toDate`) work for patents (all sources) and are supported in backend via `SearchRequestDto` and API clients.
- **Trademark Filter**: Trademark search (via TMView) supports keyword and assignee filters. (API: `/api/search/all` with `assetType: TRADEMARK`)
- **Search by Inventor**: Patent search supports inventor filter (API: `/api/search/all` with `inventor` field). Not supported for trademarks (UI disables this for trademarks).
- **Search by Assignee**: Both patent and trademark search support assignee filter (API: `/api/search/all` with `assignee` field).

**How it works:**
- Frontend collects all filter values and sends them in the search request to `/api/search/all` or `/api/search/source/{dataSource}`.
- Backend (Spring Boot) receives the request, and for patents, applies all filters (date, jurisdiction, inventor, assignee, etc.) in the API clients and combines results.
- For trademarks, TMView API/Selenium supports keyword and assignee filters; date/jurisdiction filters may be limited by TMView capabilities.
- Full details for any asset are fetched via `/api/search/asset/{assetId}` or `/api/search/asset/{source}/{externalId}`.

**APIs Used:**
- `/api/search/all` — Multi-source search (patents & trademarks)
- `/api/search/source/{dataSource}` — Single-source search
- `/api/search/asset/{assetId}` — Full asset details
- `/api/search/asset/{source}/{externalId}` — Full asset details by external ID

**Outcome:**
- ✅ Search page with filters for patents and trademarks
- ✅ Patent results with keyword, inventor, assignee, date, jurisdiction filters
- ✅ Trademark results with keyword and assignee filters
- ✅ Full details view for any patent or trademark

---

### ✅ Milestone 3: Week 5 & 6 — Analytics, Reporting & System Logs (COMPLETED)

**Objectives:**
- Implement analytics dashboards for Admin and Analyst roles
- Add reporting features and export options
- Integrate system logging and usage metrics

#### Key Features Delivered in Milestone 3
- **Admin Dashboard Analytics**: Visualizations for user activity, search trends, and system health
- **Analyst Dashboard Analytics**: IP landscape analysis, competitor tracking, and advanced filtering
- **Report Generation**: Export search results and analytics as PDF/CSV
- **System Logs**: Track user actions, API usage, and errors (Backend: `SystemLog`, `SystemLogRepository`, `SystemLogService`)
- **Usage Metrics**: Collect and display system usage statistics for admins
- **Backend Enhancements**: Improved error handling, logging, and monitoring

**Outcome:**
- ✅ Real-time analytics dashboards for Admin and Analyst
- ✅ Exportable reports for search and analytics
- ✅ System logs and usage metrics for monitoring

---

### ✅ Milestone 4: Week 7 & 8 — Finalization, Testing & Deployment (COMPLETED)

**Objectives:**
- Finalize all features and fix bugs
- Write and run comprehensive tests (unit, integration, UI)
- Prepare for deployment and documentation

#### Key Features Delivered in Milestone 4
- **Comprehensive Testing**: Backend (JUnit, Mockito), Frontend (Jest, React Testing Library)
- **Bug Fixes**: Addressed issues from previous milestones
- **Deployment Scripts**: Added scripts for backend and frontend deployment
- **Documentation**: Updated README, API docs, and setup guides
- **Production Builds**: Optimized builds for backend and frontend
- **Security Review**: Final audit of authentication, authorization, and secrets management

**Outcome:**
- ✅ All features finalized and tested
- ✅ Ready for production deployment
- ✅ Complete documentation and guides

---

## ✨ Features

- **Authentication & Security**: JWT-based authentication with OAuth2 Google Sign-In
- **Role-Based Access Control**: Admin, Analyst, and User roles with specific permissions
- **Multi-Source IP Search**: Integrated search across USPTO, WIPO, EPO, and TMView databases
- **Advanced Filtering**: Search by keywords, inventor, assignee, jurisdiction, and date ranges
- **IP Asset Details**: Comprehensive view of patents and trademarks with classifications
- **Search History**: Track and replay previous searches with result counts
- **Pagination**: Navigate through large result sets with customizable page sizes
- **Real-time Analytics**: Interactive dashboards with data visualization
- **Responsive Design**: Works seamlessly on all devices with dark mode support

## 🛠️ Tech Stack

**Frontend:**
- React 19.2.1
- React Router 7.10.1
- Tailwind CSS 3.4.18
- Recharts 3.5.1
- Axios 1.13.2

**Backend:**
- Spring Boot 3.5.5
- Spring Security
- Spring Data JPA
- PostgreSQL Database
- JWT Authentication
- Java 21

## 📋 Prerequisites

- **Java JDK 21** or higher
- **Maven 3.6+**
- **PostgreSQL 14+**
- **npm** or **yarn**
- **Git**

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone https://github.com/springboardmentor1311/Global-IP-Intelligence-Platform-Group-A.git
cd Global-IP-Intelligence-Platform-Group-A
```

### 2. Setup Database

```sql
CREATE DATABASE ip_intelligence;
```

### 3. Start Backend

```bash
cd GlobalIpBackend
# Configure application.properties with your database credentials
./mvnw spring-boot:run
```

Backend will run on `http://localhost:8080`

### 4. Start Frontend

```bash
cd GlobalIpFrontend
npm install
npm start
```

Frontend will run on `http://localhost:3000`

## 📁 Project Structure

```
Global-IP-Intelligence-Platform/
├── README.md                    # Main project documentation
├── GlobalIpBackend/             # Spring Boot backend
│   ├── README.md               # Backend-specific documentation
│   ├── src/                    # Java source files
│   ├── pom.xml                 # Maven configuration
│   └── ...
└── GlobalIpFrontend/           # React frontend
    ├── README.md               # Frontend-specific documentation
    ├── src/                    # React source files
    ├── package.json            # npm dependencies
    └── ...
```

## 📚 Documentation

For detailed setup and configuration instructions, please refer to:
- [Backend Documentation](./GlobalIpBackend/README.md)
- [Frontend Documentation](./GlobalIpFrontend/README.md)

## 🔑 Key Features by Role

### Admin Dashboard
- User management (create, update, delete)
- System configuration
- Analytics overview

### Analyst Dashboard
- IP analysis tools
- Threat intelligence reports
- Data visualization

### User Dashboard
- Personal IP monitoring
- Report generation
- Profile management

## 🌐 API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/auth/register` | POST | User registration |
| `/api/auth/login` | POST | User login |
| `/api/profile` | GET | Get user profile |
| `/api/admin/*` | * | Admin endpoints |
| `/api/analyst/*` | * | Analyst endpoints |
| `/api/user/*` | * | User endpoints |

## 🧪 Testing

**Backend:**
```bash
cd GlobalIpBackend
./mvnw test
```

**Frontend:**
```bash
cd GlobalIpFrontend
npm test
```

## 📦 Building for Production

**Backend:**
```bash
cd GlobalIpBackend
./mvnw clean package
```

**Frontend:**
```bash
cd GlobalIpFrontend
npm run build
```

## 🚀 Deployment

### Backend Deployment
The backend generates a JAR file that can be deployed to any Java-compatible server:
```bash
java -jar target/ip-backend-0.0.1-SNAPSHOT.jar
```

### Frontend Deployment
The frontend build can be deployed to:
- Vercel
- Netlify
- AWS S3 + CloudFront
- Azure Static Web Apps

## 🔒 Security

- JWT-based authentication
- Role-based authorization
- Secure password hashing
- OAuth2 integration
- CORS configuration

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 🐛 Troubleshooting

**Backend not starting?**
- Check PostgreSQL is running
- Verify database credentials in `application.properties`
- Ensure port 8080 is available

**Frontend not connecting to backend?**
- Verify backend is running on port 8080
- Check proxy configuration in `package.json`
- Clear browser cache

## 📄 License

This project is developed as part of the Springboard mentorship program.

## 👥 Team

Global IP Intelligence Platform - Group A

## 📞 Support

For issues and questions, please create an issue in the GitHub repository.

---

**Version:** 1.0.0  
**Last Updated:** December 2025
