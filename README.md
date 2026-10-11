# HomeRedi

HomeRedi Spring Boot application.

See [HomeRedi_Current_State.md](HomeRedi_Current_State.md) for the single source of truth for product and architecture decisions, including V1 and Slice 1. [PROJECT_PROGRESS.md](PROJECT_PROGRESS.md) tracks implementation progress. The long product evolution document is a roadmap hypothesis, not the implementation specification.

See [PROJECT_BUILD_PROCESS.md](PROJECT_BUILD_PROCESS.md) for the working process from idea research through specification, code planning, TDD, and feedback.

## Requirements

- Java 21
- Maven 3.6.3 or newer

## Run

```bash
mvn spring-boot:run
```

The application starts on `http://localhost:8080`. The health endpoint is available at `/actuator/health`.

## Build

```bash
mvn clean package
```
# HomeRedi
