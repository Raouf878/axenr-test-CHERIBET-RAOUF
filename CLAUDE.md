# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an AxENR technical test repository for implementing automatic task date calculation in Axelor ERP. The project demonstrates task dependency management and scheduling for photovoltaic installation projects.

The repository contains:
- `poc/` - Standalone Java proof-of-concept demonstrating the task planning algorithm
- `axelor/` - Full Axelor ERP integration with the axenr module

## Build and Run Commands

### Database Setup

Start PostgreSQL database:
```bash
cd axelor
docker-compose up -d
```

The database runs on port 5434 (mapped from 5432 to avoid conflicts).

### Axelor Application

Build the application:
```bash
cd axelor
./gradlew build
```

Run the application:
```bash
cd axelor
./gradlew run
```

The application will be available at `http://localhost:8080`

Generate domain model classes (after modifying XML domain files):
```bash
cd axelor
./gradlew generateCode
```

Clean build artifacts:
```bash
cd axelor
./gradlew clean
```

### POC

Run the standalone proof-of-concept:
```bash
cd poc
javac TaskPlanningPOC.java
java TaskPlanningPOC
```

## Architecture

### Module Structure

The Axelor application follows a modular architecture:

```
axelor/
├── modules/axenr/               # Custom AxENR module
│   └── src/main/
│       ├── java/fr/axenr/apps/
│       │   ├── service/         # Business logic layer
│       │   │   └── TaskPlanningService.java
│       │   ├── web/             # Controller layer (action handlers)
│       │   │   └── ProjectController.java
│       │   └── AxEnrModule.java # Module configuration
│       └── resources/
│           ├── domains/         # Entity definitions (XML)
│           │   ├── Project.xml
│           │   └── Task.xml
│           └── views/           # UI definitions (XML)
│               ├── Project.xml
│               └── Task.xml
└── src/main/resources/
    └── axelor-config.properties # Application configuration
```

### Domain Model

**Project** entity:
- `name` (String) - Project name
- `startDate` (LocalDate) - Project start date
- `endDate` (LocalDate) - Calculated project end date
- `taskList` (OneToMany) - List of tasks

**Task** entity:
- `name` (String) - Task name
- `duration` (BigDecimal) - Duration in days
- `delayToStart` (BigDecimal) - Delay in days before starting after parent task
- `startDate` (LocalDate) - Calculated start date
- `endDate` (LocalDate) - Calculated end date
- `dependOf` (OneToOne) - Parent task dependency
- `project` (ManyToOne) - Associated project

### Task Planning Algorithm

The `TaskPlanningService` implements:

1. **Forward Planning** (`computeDates`):
   - Validates project has start date and tasks
   - Detects circular dependencies using topological sort
   - Calculates task dates from project start date
   - Tasks without dependencies start at project start
   - Dependent tasks start after parent end date + delay
   - Updates project end date to latest task end

2. **Backward Planning** (`computeDatesBackward`) - BONUS:
   - Calculates dates from project end date backwards
   - Useful for deadline-driven scheduling
   - Computes earliest required start dates

3. **Topological Sort**:
   - Orders tasks respecting dependencies
   - Uses depth-first search (DFS)
   - Detects circular dependencies during traversal

### Controller Layer

`ProjectController` handles UI actions:
- `computeDates()` - Called by "Compute dates" button in Project view
- `computeDatesBackward()` - Optional retroplanning action
- Uses dependency injection for repositories and services
- Provides French error messages for validation failures

### View Layer

Views are defined in XML:
- `project-form` includes a "Compute dates" button that triggers `action-project-compute-dates`
- Action method references `ProjectController.computeDates`
- Task grid is editable inline for quick data entry
- Start/end dates are readonly (calculated fields)
- Domain constraint prevents self-reference in task dependencies

## Configuration

### Database Connection

Edit `axelor/src/main/resources/axelor-config.properties`:

```properties
db.default.url = jdbc:postgresql://localhost:5434/axenr-db
db.default.user = postgres
db.default.password = changeme
```

### Application Mode

Set development mode in axelor-config.properties:
```properties
application.mode = dev
```

## Axelor Framework Specifics

### Domain Model Generation

Domain entities are defined in XML (`modules/axenr/src/main/resources/domains/*.xml`) and generated to Java classes using:
```bash
./gradlew generateCode
```

Generated classes appear in `modules/axenr/build/src-gen/java/`.

### View Refresh

After modifying XML views, refresh them in the application:
- Navigate to: Administration → View Management → All Views
- Click "Restore all" in toolbar

### Action Methods

Action methods in controllers follow Axelor conventions:
- Method signature: `public void methodName(ActionRequest request, ActionResponse response)`
- Access context via `request.getContext()`
- Set responses via `response.setReload()`, `response.setNotify()`, `response.setError()`

### Transactions

Service methods that modify data must be annotated with `@Transactional` and use `JPA.save()` to persist changes.

## Development Notes

### Java Version

The project uses Java 11 (configured in root build.gradle).

### Gradle Version

Axelor plugin version: 7.4.3 (defined in settings.gradle)

### Module Dependencies

The axenr module has no external dependencies beyond the Axelor framework itself.

### Logging

Application logging is configured in axelor-config.properties:
- Root level: ERROR
- Axelor framework: DEBUG

### Test Data

The application imports demo data by default:
```properties
data.import.demo-data = true
```

## Common Issues

### Port Conflicts

If PostgreSQL port 5434 is in use, modify `compose.yml` ports mapping.

### View Not Updating

Always refresh views after XML changes: Administration → View Management → All Views → Restore all

### Generated Code Out of Sync

Run `./gradlew generateCode` after modifying domain XML files to regenerate entity classes.

## Documentation

Axelor ADK Documentation: https://docs.axelor.com/adk/7.4/index.html
