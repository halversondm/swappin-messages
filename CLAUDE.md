# CLAUDE.md

Spring Boot (Java 25, Maven) demo that swaps messages over Kafka between two running containers until a stop condition is met. Messages are persisted to Postgres via JPA (hypersistence-utils).

## Commands

- Build and test: `mvn -B clean verify` (tests use Testcontainers for Postgres and Kafka, so Docker must be running)
- Build without tests: `mvn -B clean verify -DskipTests`
- Docker image: `./build.sh`; compose setup in `docker-compose.yml`

## Layout

- `src/main/java/com/halverson/swappin_messages/` - application, `StartSwappin`, `MessageSwap`, `SwappinMessagesService`
- `entity/`, `dao/` - JPA entity and repository
- `src/main/resources/application-{db,one,two}.properties` - Spring profiles for DB and the two swapping instances
- `src/test/` - Testcontainers-based integration test

## Conventions

- Lombok is used (annotation processor configured in `pom.xml`).
- Spring Boot parent manages most versions; only use stable releases when bumping.
