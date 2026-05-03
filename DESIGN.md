# Design Diagrams for ParkingLot Service

## Class Diagram

The following class diagram illustrates the structure of the ParkingLot application, showing the relationships between the main components such as `Application`, `CommandProcessor`, `CommandParser`, and `ParkingLotService`.

```mermaid
classDiagram
    class Application {
        +main(String[] args)
    }

    class CommandProcessor {
        -CommandParser commandParser
        -FileCommandReader fileCommandReader
        -TerminalCommandReader commandReader
        -boolean isParkingLotInitialized
        +run()
        -execute(String cmd)
    }

    class CommandParser {
        -Map~CommandType, Command~ commandMap
        +static IParkingLotService parkingService
        +init()
        +getCommandForCommandID(String id) : Command
    }

    class Command {
        <<interface>>
        +execute(IParkingLotService parkingLotService, String[] args)
    }

    class IParkingLotService {
        <<interface>>
        +park(String vehicleRegId, int driverAge)
        +unpark(int slotId)
        +getSlotsForDriverWithAge(int age) : List~String~
        +getVehicleRegIDsForDriverWithAge(int age) : List~String~
        +getSlotForVehicleWithRegID(String regId) : int
    }

    class ParkingLotService {
        -int maxSlots
        -int size
        -Map~String, Integer~ vehicleIdToDriverAgeMap
        -Map~String, Integer~ vehicleIdToSlotMap
        -Map~Integer, String~ slotToVehicleIdMap
        -Queue~Integer~ vacantSlotsQueue
        -ParkingLotService(int maxSlots)
        +static getInstance(int maxSlots) : ParkingLotService
        +park(String vehicleRegId, int driverAge)
        +unpark(int slotId)
        +getSlotsForDriverWithAge(int age) : List~String~
        +getVehicleRegIDsForDriverWithAge(int age) : List~String~
        +getSlotForVehicleWithRegID(String regId) : int
    }

    class CommandType {
        <<enumeration>>
        Create_parking_lot
        Park
        Slot_numbers_for_driver_of_age
        Slot_number_for_car_with_number
        Leave
        Vehicle_registration_number_for_driver_of_age
        +getID() : String
        +static commandTypeForCommandID(String id) : CommandType
    }

    class AppLogger {
        -Logger logger
        +debug(String msg)
        +error(String msg)
    }

    Application ..> CommandProcessor : creates
    CommandProcessor --> CommandParser : uses
    CommandProcessor ..> Command : executes
    CommandParser o-- Command : contains
    CommandParser --> CommandType : uses
    CommandParser --> IParkingLotService : holds reference
    ParkingLotService ..|> IParkingLotService : implements
    Command ..> IParkingLotService : operates on
    ParkingLotService --> AppLogger : uses

```

## Sequence Diagram: Create Parking Lot

This sequence diagram depicts the flow when the `Create_parking_lot` command is issued. This is a special command that initializes the `ParkingLotService`.

```mermaid
sequenceDiagram
    actor User
    participant CP as CommandProcessor
    participant Parser as CommandParser
    participant Cmd as Command (Lambda)
    participant PLS as ParkingLotService

    User->>CP: "Create_parking_lot 6"
    CP->>CP: execute("Create_parking_lot 6")
    CP->>Parser: getCommandForCommandID("Create_parking_lot")
    Parser->>CP: returns Command (Lambda)

    note right of CP: Checks if initialized (allows if not)

    CP->>Cmd: execute(null, ["Create_parking_lot", "6"])

    note right of Cmd: Validates args

    Cmd->>PLS: getInstance(6)
    PLS-->>Cmd: returns new ParkingLotService instance
    Cmd-->>Parser: Sets Parser.parkingService = instance

    CP->>CP: isParkingLotInitialized = true
```

## Sequence Diagram: Park Vehicle

This sequence diagram shows the process of parking a vehicle. It assumes the parking lot has already been initialized.

```mermaid
sequenceDiagram
    actor User
    participant CP as CommandProcessor
    participant Parser as CommandParser
    participant Cmd as Command (Lambda)
    participant PLS as ParkingLotService

    User->>CP: "Park KA-01-HH-1234 driver_age 21"
    CP->>CP: execute("Park KA-01-HH-1234 driver_age 21")
    CP->>Parser: getCommandForCommandID("Park")
    Parser->>CP: returns Command (Lambda)

    note right of CP: Checks if initialized (must be true)

    CP->>Cmd: execute(parkingService, args)

    note right of Cmd: Validates args and format

    Cmd->>PLS: park("KA-01-HH-1234", 21)

    alt Parking Lot Full
        PLS-->>Cmd: throws ParkingLotFullException
        Cmd-->>CP: propagates exception
        CP->>User: "Parking Lot is full!"
    else Duplicate Registration
        PLS-->>Cmd: throws DuplicateRegistrationIDException
        Cmd-->>CP: propagates exception
        CP->>User: Error message (Duplicate ID)
    else Success
        PLS->>PLS: Check vacant slots
        PLS->>PLS: Assign slot
        PLS->>PLS: Update maps
        PLS->>User: "Car with vehicle registration number ... parked at slot ..."
    end
```
