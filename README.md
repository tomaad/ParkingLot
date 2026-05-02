# Parking Lot Service

A **production-grade, multi-level parking lot management service** built with Domain-Driven Design, Spring Boot, and full observability stack.

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                   Interface Layer                         │
│   ┌─────────────┐  ┌─────────────┐  ┌──────────────┐   │
│   │  REST API   │  │  CLI Adapter │  │  (Future:    │   │
│   │  /api/v1/   │  │             │  │   gRPC/Gate) │   │
│   └──────┬──────┘  └──────┬──────┘  └──────────────┘   │
├──────────┼─────────────────┼────────────────────────────┤
│          │  Application Layer (Use Cases)                 │
│   ┌──────┴──────────────────┴──────┐                    │
│   │  ParkVehicle │ Unpark │ Query  │                    │
│   └──────────────┬─────────────────┘                    │
├──────────────────┼──────────────────────────────────────┤
│                  │  Domain Layer                          │
│   ┌──────────────┴─────────────────────────────────┐    │
│   │  ParkingLot → ParkingLevel → ParkingSlot       │    │
│   │  Vehicle, ParkingTicket, LicensePlate           │    │
│   │  Strategies: Nearest, LevelBalanced             │    │
│   │  Events: Parked, Unparked, CapacityAlert        │    │
│   └────────────────────────────────────────────────┘    │
├─────────────────────────────────────────────────────────┤
│                   Infrastructure Layer                    │
│   ┌────────────┐  ┌────────────┐  ┌─────────────────┐  │
│   │ Repositories│  │  Metrics   │  │ Event Publisher  │  │
│   │ (In-Memory) │  │ Prometheus │  │  + Logging      │  │
│   └────────────┘  └────────────┘  └─────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

### Design Patterns Used

| Pattern | Where | Purpose |
|---------|-------|---------|
| **Strategy** | Slot allocation (Nearest, LevelBalanced) | Pluggable allocation policies |
| **Repository** | ParkingLotRepository, ParkingTicketRepository | Decouple domain from persistence |
| **Domain Events** | VehicleParked, VehicleUnparked, LotCapacity | Decouple side effects from core logic |
| **State Machine** | ParkingSlot (Available→Reserved→Occupied) | Enforce valid state transitions |
| **CQRS-lite** | Separate command use cases from query service | Independent scaling of reads/writes |
| **Factory** | Spring Configuration | Complex object wiring |

## System Requirements

- **Java 17** (JDK)
- **Maven 3.9+**
- **Docker & Docker Compose** (for containerized deployment)

## Quick Start

### Option 1: Run with Docker (Recommended)

```bash
git clone https://github.com/tomaad/ParkingLot.git
cd ParkingLot
docker compose up --build
```

This starts:
- **Parking Lot Service** → http://localhost:8080
- **Prometheus** → http://localhost:9090
- **Grafana** → http://localhost:3000 (admin/admin)

### Option 2: Run Locally

```bash
git clone https://github.com/tomaad/ParkingLot.git
cd ParkingLot
mvn clean package
java -jar target/parking-lot-service-1.0.0-SNAPSHOT.jar
```

### Option 3: Run Tests

```bash
mvn clean test
# Coverage report at: target/site/jacoco/index.html
```

## REST API

### Park a Vehicle
```bash
curl -X POST http://localhost:8080/api/v1/parking/park \
  -H "Content-Type: application/json" \
  -d '{"licensePlate": "KA-01-HH-1234", "vehicleType": "CAR", "driverAge": 21}'
```
**Response:**
```json
{
  "ticketId": "uuid",
  "slotNumber": "F1-S01",
  "levelNumber": 1,
  "entryTime": "2026-05-02T07:30:00Z"
}
```

### Unpark a Vehicle
```bash
curl -X POST http://localhost:8080/api/v1/parking/unpark \
  -H "Content-Type: application/json" \
  -d '{"ticketId": "uuid-from-park-response"}'
```

### Check Availability
```bash
curl http://localhost:8080/api/v1/parking/availability
```

### Find Vehicle
```bash
curl http://localhost:8080/api/v1/parking/vehicles/KA-01-HH-1234
```

## Observability

### Health Check
```bash
curl http://localhost:8080/actuator/health
```
Returns parking lot specific health: capacity, available slots, occupancy rate.

### Prometheus Metrics
```bash
curl http://localhost:8080/actuator/prometheus
```

**Custom Metrics:**
| Metric | Type | Description |
|--------|------|-------------|
| `parking_vehicles_parked_total` | Counter | Total vehicles parked |
| `parking_vehicles_unparked_total` | Counter | Total vehicles unparked |
| `parking_occupancy_current` | Gauge | Current occupancy ratio (0-1) |
| `parking_slots_available` | Gauge | Available slot count |
| `parking_park_duration_seconds` | Timer | Park operation latency |
| `parking_unpark_duration_seconds` | Timer | Unpark operation latency |
| `parking_errors_total` | Counter | Errors by type |

### Grafana Dashboard
Pre-configured dashboard available at http://localhost:3000 with panels for:
- Occupancy rate gauge
- Park/Unpark rates over time
- Operation latencies
- Error rates
- JVM memory usage
- HTTP request rates

### Structured Logging
JSON-formatted logs in production (human-readable with `SPRING_PROFILES_ACTIVE=local`):
```json
{"timestamp":"2026-05-02T07:30:00","level":"INFO","logger":"ParkingController","message":"Vehicle parked","service":"parking-lot-service","ticketId":"...","slot":"F1-S01"}
```

## Configuration

Key settings in `application.yml`:

| Property | Default | Description |
|----------|---------|-------------|
| `parking-lot.name` | Downtown Parking Plaza | Lot name |
| `parking-lot.levels` | 3 | Number of floors |
| `parking-lot.slots-per-level` | 20 | Slots per floor |
| `parking-lot.allocation-strategy` | nearest | `nearest` or `balanced` |
| `parking-lot.capacity-alert-threshold` | 0.90 | Alert when occupancy exceeds this |

## Project Structure

```
src/main/java/com/parkinglot/
├── domain/
│   ├── model/          # Entities, Value Objects, Enums
│   ├── policy/         # Allocation Strategies
│   ├── event/          # Domain Events
│   ├── exception/      # Business Exceptions
│   └── repository/     # Repository Interfaces
├── application/
│   ├── service/        # Use Cases (Park, Unpark, Query)
│   ├── dto/            # Request/Response DTOs
│   └── event/          # Event Publisher Interface
├── infrastructure/
│   ├── persistence/    # In-Memory Repository Implementations
│   ├── event/          # Event Publisher Implementation
│   ├── observability/  # Metrics, Health Indicators
│   └── config/         # Configuration
├── interfaces/
│   ├── rest/           # REST Controllers, Exception Handlers
│   └── cli/            # CLI Adapter
└── ParkingLotApplication.java
```

## Docker

### Build Image
```bash
docker build -t parking-lot-service:latest .
```

### Run Full Stack
```bash
docker compose up -d
```

### Stop
```bash
docker compose down
```

## Testing

The project includes comprehensive JUnit 5 tests:

- **Domain Model Tests** — ParkingSlot state machine, ParkingLot aggregate, LicensePlate validation, ParkingTicket lifecycle
- **Strategy Tests** — NearestSlotStrategy, LevelBalancedStrategy
- **Use Case Tests** — ParkVehicleUseCase, UnparkVehicleUseCase with mocked dependencies
- **Query Service Tests** — ParkingQueryService
- **Repository Tests** — InMemoryParkingLotRepository, InMemoryParkingTicketRepository

Coverage report generated by JaCoCo at `target/site/jacoco/index.html`.

---

## Legacy CLI Mode

The original CLI interface is preserved under `src/main/java/services/`. To run it:
```bash
java -cp target/classes services.Application [input-file]
```
