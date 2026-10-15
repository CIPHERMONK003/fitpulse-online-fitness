# FitPulse - Online Fitness Coaching Enterprise Platform

> **Language**: Java 17+ (No TypeScript used - 100% Pure Java Backend with Dynamic Web Servlets & Responsive HTML5/CSS3)  
> **Architecture**: Model-View-Controller (MVC) • Generic DAO Pattern • Embedded Jetty Web Server • H2 JDBC Engine • Multithreaded Worker Core  

---

## 📋 Marking Rubric Mapping & Evidence

| Rubric Requirement | Marks | Key Java Files & Implementation Details |
| :--- | :---: | :--- |
| **OOP Implementation**<br>*(Polymorphism, Inheritance, Exception Handling, Interfaces)* | **10** | • **Inheritance**: `User` abstract base class $\rightarrow$ `ClientUser`, `CoachUser`, `AdminUser`. `WorkoutPlan` abstract base class $\rightarrow$ `StrengthWorkoutPlan`, `CardioWorkoutPlan`, `HIITWorkoutPlan`.<br>• **Polymorphism (Overriding)**: `calculateCaloriesBurned()`, `getDashboardUrl()`, `generateWorkoutSummary()`.<br>• **Polymorphism (Overloading)**: `ProgressService.logProgress(...)`, `WorkoutPlanService.filterPlans(...)`.<br>• **Interfaces**: `WorkoutCalculable` (with default methods), `Identifiable<ID>`, `Notifiable`, `GenericDao<T, ID>`.<br>• **Exception Handling**: `FitnessPlatformException`, `UserNotFoundException`, `UserAlreadyExistsException`, `AuthenticationException`, `InvalidWorkoutPlanException`, `UnauthorizedAccessException`, `DatabaseOperationException` (with exception chaining). |
| **Collections & Generics** | **6** | • **Generics**: `GenericDao<T, ID>`, `ApiResponse<T>`, generic utility methods `CollectionUtils.filter()`, `map()`, `sorted()`.<br>• **Collections**: `List`, `Map`, `Set`, `ConcurrentHashMap`, `CopyOnWriteArrayList`.<br>• **Custom Comparators**: `ProgressRecord` sorting by calories burned descending (`byCaloriesDesc`).<br>• **Stream API**: `Collectors.groupingBy()`, `Collectors.summingDouble()`, filtering, mapping. |
| **Multithreading & Synchronization** | **4** | • **Producer-Consumer Notification Queue**: `NotificationQueueService` utilizing `LinkedBlockingQueue<NotificationTask>` and a background worker daemon thread.<br>• **Thread-Safe Synchronized Metrics**: `PlatformMetricsManager` with `synchronized` methods, `AtomicInteger`, `AtomicLong`, and `ReentrantReadWriteLock` for concurrent metric updates under servlet requests.<br>• **Background Scheduler**: `MaintenanceScheduler` with `ScheduledExecutorService` for periodic health audits. |
| **Classes for Database Operations (DAO)** | **7** | • `UserDao`, `WorkoutPlanDao`, `ProgressRecordDao`, `CoachFeedbackDao`, `AuditLogDao`.<br>• Clean separation of concerns (Model $\rightarrow$ DAO $\rightarrow$ Service $\rightarrow$ Servlet). |
| **Database Connectivity (JDBC)** | **3** | • `DatabaseManager` providing JDBC `DriverManager` connectivity, embedded H2 database engine, connection pooling (`ArrayBlockingQueue`), metadata inspection (`DatabaseMetaData`). |
| **Database Integration (JDBC)** | **8** | • Auto-schema initialization (`schema.sql` DDL statements across 11 relational tables).<br>• 100% `PreparedStatement` usage (preventing SQL Injection).<br>• JDBC Transactions (`setAutoCommit(false)`, `commit()`, `rollback()`).<br>• Auto-seeding initial mock data (Admin, Coaches, Clients, Plans, Logs). |
| **Servlets & Web Integration** | **7** | • `AuthServlet` (`/auth/*`), `DashboardServlet` (`/dashboard`), `ClientServlet` (`/client/*`), `ProgressServlet` (`/progress/*`), `CoachServlet` (`/coach/*`), `AdminServlet` (`/admin/*`), `ApiServlet` (`/api/*`).<br>• `AuthenticationFilter` implementing `Filter` for role-based URL protection (`/client/*`, `/coach/*`, `/admin/*`).<br>• `HttpSession` session management. |
| **Problem Understanding & Solution Design** | **8** | • Comprehensive solution with 3 distinct roles: **Clients** (track progress, enroll in plans, log workouts, BMI stats), **Coaches** (create polymorphic plans, give advice/feedback), **Admins** (user moderation, content removal, audit trail, concurrency stats). |
| **Core Java Concepts** | **10** | • Strong typing, memory hygiene, clean package separation, `try-with-resources`, unit test coverage with JUnit (15/15 tests passing). |

---

## 🏗️ Architecture & Package Layout

```
online-fitness-coaching/
├── pom.xml                               # Maven project configuration
├── run.bat                               # Windows 1-click launcher
├── run.ps1                               # PowerShell launcher
├── test.bat                              # 1-click JUnit test suite runner
├── README.md                             # Complete project documentation
└── src/
    ├── main/
    │   ├── resources/
    │   │   └── db.properties             # JDBC database connection settings
    │   └── java/com/fitness/
    │       ├── FitnessAppServer.java     # Application Bootstrap & Embedded Jetty Server
    │       ├── config/
    │       │   ├── AppConfig.java        # System & DB properties loader
    │       │   └── DatabaseManager.java  # JDBC Connection Pool, Transactions, DDL Schema
    │       ├── model/                    # OOP Domain Models (Inheritance, Polymorphism)
    │       │   ├── Identifiable.java     # Generic ID interface
    │       │   ├── Role.java             # CLIENT, COACH, ADMIN enums
    │       │   ├── DifficultyLevel.java  # BEGINNER, INTERMEDIATE, ADVANCED enums
    │       │   ├── PlanType.java         # STRENGTH, CARDIO, HIIT enums
    │       │   ├── WorkoutCalculable.java# Functional interface with default method
    │       │   ├── Notifiable.java       # Notification interface
    │       │   ├── User.java             # Abstract User class
    │       │   ├── ClientUser.java       # User subclass (goals, weight, BMI, coach link)
    │       │   ├── CoachUser.java        # User subclass (specialization, certs, rating)
    │       │   ├── AdminUser.java        # User subclass (department, adminLevel)
    │       │   ├── WorkoutPlan.java      # Abstract WorkoutPlan class
    │       │   ├── StrengthWorkoutPlan.java # Resistance & muscle overload formula
    │       │   ├── CardioWorkoutPlan.java   # Aerobic endurance & HR zone formula
    │       │   ├── HIITWorkoutPlan.java     # Anaerobic intervals & EPOC formula
    │       │   ├── ProgressRecord.java   # Daily workout log entity
    │       │   ├── CoachFeedback.java    # Coach advice & rating message entity
    │       │   └── AuditLog.java         # Security & administrative audit entity
    │       ├── exception/                # Custom Exception Hierarchy
    │       │   ├── FitnessPlatformException.java # Base checked exception
    │       │   ├── UserNotFoundException.java
    │       │   ├── UserAlreadyExistsException.java
    │       │   ├── AuthenticationException.java
    │       │   ├── InvalidWorkoutPlanException.java
    │       │   ├── UnauthorizedAccessException.java
    │       │   └── DatabaseOperationException.java # Unchecked exception chaining
    │       ├── concurrency/              # Multithreading & Synchronization
    │       │   ├── NotificationTask.java
    │       │   ├── NotificationQueueService.java # BlockingQueue worker thread
    │       │   ├── PlatformMetricsManager.java   # ReentrantReadWriteLock & Atomics
    │       │   └── MaintenanceScheduler.java     # ScheduledExecutorService
    │       ├── dao/                      # Database Operations & PreparedStatements
    │       │   ├── GenericDao.java       # Generic DAO interface <T, ID>
    │       │   ├── UserDao.java          # Polymorphic user persistence & transactions
    │       │   ├── WorkoutPlanDao.java   # Polymorphic plan persistence & enrollments
    │       │   ├── ProgressRecordDao.java# Workouts logging & calorie queries
    │       │   ├── CoachFeedbackDao.java # Coach-client feedback queries
    │       │   └── AuditLogDao.java      # System audit logging queries
    │       ├── util/                     # Collections & Generics Utilities
    │       │   ├── ApiResponse.java      # Generic ApiResponse<T>
    │       │   ├── CollectionUtils.java  # Generic filter, map, sorted, groupBy
    │       │   └── PasswordUtil.java     # SHA-256 password hashing with salt
    │       ├── service/                  # Business Logic Layer
    │       │   ├── UserService.java      # Auth & registration
    │       │   ├── WorkoutPlanService.java # Overloaded filter methods & enrollments
    │       │   ├── ProgressService.java  # Overloaded progress logging & stream stats
    │       │   ├── CoachFeedbackService.java
    │       │   └── AdminService.java     # Moderation & diagnostic queries
    │       ├── servlet/                  # Java Servlets (HTTP Request Handling)
    │       │   ├── HomeServlet.java      # Root router
    │       │   ├── AuthServlet.java      # Login, Register, Logout
    │       │   ├── DashboardServlet.java # Dynamic role-based dashboard router
    │       │   ├── ClientServlet.java    # Catalog & enrollment
    │       │   ├── ProgressServlet.java  # Workout logging & analytics
    │       │   ├── CoachServlet.java     # Plan authoring & feedback
    │       │   ├── AdminServlet.java     # User moderation & plan deletion
    │       │   └── ApiServlet.java       # RESTful JSON API endpoints
    │       ├── filter/
    │       │   └── AuthenticationFilter.java # Role-based security filter
    │       └── view/
    │           └── HtmlRenderer.java     # Modern responsive HTML5/CSS3 renderer
    └── test/java/com/fitness/
        ├── OopAndPolymorphismTest.java       # JUnit tests for OOP requirements
        ├── CollectionsAndGenericsTest.java   # JUnit tests for Generics/Collections
        ├── MultithreadingTest.java           # JUnit tests for Concurrency
        └── DatabaseIntegrationTest.java      # JUnit tests for JDBC & DAOs
```

---

## 🚀 How to Run the Platform

### Option 1: Double-Click Batch File (Windows)
Double-click `run.bat` in the `online-fitness-coaching` directory.

### Option 2: PowerShell
```powershell
cd C:\Users\krishna\.gemini\antigravity\scratch\online-fitness-coaching
.\run.ps1
```

### Option 3: Standard Maven Command
```bash
mvn exec:java
```

Once started, open your web browser at:
👉 **`http://localhost:8080`**

---

## 🔑 Pre-Seeded Demo User Accounts

The database automatically seeds with realistic demo data on initial launch:

| Role | Email | Password | Details |
| :--- | :--- | :--- | :--- |
| **Client** | `alice.client@fitness.com` | `client123` | Enrolled in 5K Aerobic Cardio; 2 logged workouts; assigned to Coach Mike. |
| **Coach** | `sarah.coach@fitness.com` | `coach123` | Strength Specialist (CSCS, NASM); author of Strength Accelerator plan. |
| **Coach** | `mike.coach@fitness.com` | `coach123` | Cardio & HIIT Specialist; author of 5K Run & Metabolic Inferno HIIT. |
| **Admin** | `admin@fitness.com` | `admin123` | Full administrative privileges, user toggle, moderation & metrics reset. |

---

## 🧪 Running Unit & Integration Tests

To run the complete test suite (15 passing tests):
```bash
test.bat
# or
mvn test
```
All 15 tests run against in-memory/file JDBC database, multithreaded queues, and polymorphic classes with **0 failures and 0 errors**.

---

## 🌐 How to Publish Globally (Deploy to the Web)

This project has been packaged and configured for **global, public deployment**. You can choose any of the options below:

### 🌟 Method 1: Instant 1-Click Global Public HTTPS URL (No Cloud Account Needed!)
To immediately share your live running platform with evaluators, friends, or anyone worldwide:
1. Double-click **`publish-global.bat`** (or run `.\publish-global.ps1` in PowerShell).
2. The script boots the Java application and initializes a secure Cloudflare Edge Tunnel.
3. It prints a live public HTTPS URL on screen:
   ```
   https://fitpulse-live-xxxx.trycloudflare.com
   ```
4. Anyone on any mobile phone, tablet, or PC worldwide can now open this link and access your platform in real time!

---

### 📦 Method 2: Standalone Production Executable Fat JAR
We configured `maven-shade-plugin` to build a single self-contained, runnable JAR containing the Jetty engine, servlets, and H2 database:
```bash
# Package the standalone Fat JAR
mvn clean package -DskipTests

# Run anywhere in the world on any OS (Windows, Linux, macOS, AWS, DigitalOcean)
java -jar target/fitpulse-global-app.jar
```

---

### 🐳 Method 3: Production Docker & Docker Compose
The repository includes a production multi-stage `Dockerfile` and `docker-compose.yml`:
```bash
# Build and run containerized platform with persistent volume
docker-compose up --build -d

# Check live logs
docker-compose logs -f
```

---

### ☁️ Method 4: Free 1-Click Cloud Hosting (Render / Railway / Fly.io)

#### Deploy to Render.com (Free Global Web Service):
1. Push this folder to a GitHub repository.
2. Sign in to [Render.com](https://render.com) and click **New > Blueprint**.
3. Select your GitHub repository; Render will detect [`render.yaml`](file:///C:/Users/krishna/.gemini/antigravity/scratch/online-fitness-coaching/render.yaml) and automatically build and host the Docker container.
4. Your platform will be permanently live globally at `https://fitpulse-fitness-platform.onrender.com`.

#### Deploy to Railway.app:
1. Sign in to [Railway.app](https://railway.app).
2. Click **New Project > Deploy from GitHub Repo**.
3. Railway automatically detects [`railway.json`](file:///C:/Users/krishna/.gemini/antigravity/scratch/online-fitness-coaching/railway.json) and [`Dockerfile`](file:///C:/Users/krishna/.gemini/antigravity/scratch/online-fitness-coaching/Dockerfile) and assigns a public domain.

