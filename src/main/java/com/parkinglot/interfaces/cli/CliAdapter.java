package com.parkinglot.interfaces.cli;

import com.parkinglot.application.dto.AvailabilityResponse;
import com.parkinglot.application.dto.ParkRequest;
import com.parkinglot.application.dto.ParkResponse;
import com.parkinglot.application.dto.UnparkRequest;
import com.parkinglot.application.dto.UnparkResponse;
import com.parkinglot.application.service.ParkVehicleUseCase;
import com.parkinglot.application.service.ParkingQueryService;
import com.parkinglot.application.service.UnparkVehicleUseCase;
import com.parkinglot.domain.model.ParkingTicket;
import com.parkinglot.domain.model.VehicleType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.PrintStream;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Thin CLI adapter that converts terminal input into application use-case calls.
 */
public final class CliAdapter {
    private final ParkVehicleUseCase parkVehicleUseCase;
    private final UnparkVehicleUseCase unparkVehicleUseCase;
    private final ParkingQueryService parkingQueryService;
    private final BufferedReader reader;
    private final PrintStream output;
    private final String lotId;

    /**
     * Creates the CLI adapter.
     *
     * @param parkVehicleUseCase parking use case
     * @param unparkVehicleUseCase unparking use case
     * @param parkingQueryService query service
     * @param reader source reader
     * @param output output stream
     * @param lotId lot identifier
     */
    public CliAdapter(final ParkVehicleUseCase parkVehicleUseCase,
                      final UnparkVehicleUseCase unparkVehicleUseCase,
                      final ParkingQueryService parkingQueryService,
                      final Reader reader,
                      final PrintStream output,
                      final String lotId) {
        this.parkVehicleUseCase = Objects.requireNonNull(parkVehicleUseCase, "parkVehicleUseCase must not be null");
        this.unparkVehicleUseCase = Objects.requireNonNull(unparkVehicleUseCase, "unparkVehicleUseCase must not be null");
        this.parkingQueryService = Objects.requireNonNull(parkingQueryService, "parkingQueryService must not be null");
        this.reader = new BufferedReader(Objects.requireNonNull(reader, "reader must not be null"));
        this.output = Objects.requireNonNull(output, "output must not be null");
        this.lotId = Objects.requireNonNull(lotId, "lotId must not be null");
    }

    /**
     * Starts reading commands until the input is exhausted or the user exits.
     *
     * @throws IOException when input cannot be read
     */
    public void start() throws IOException {
        output.println("Parking lot CLI ready. Type 'help' for commands.");
        String line;
        while ((line = reader.readLine()) != null) {
            final String commandLine = line.trim();
            if (commandLine.isEmpty()) {
                continue;
            }
            if ("quit".equalsIgnoreCase(commandLine) || "exit".equalsIgnoreCase(commandLine)) {
                output.println("Shutting down CLI.");
                return;
            }
            handle(commandLine);
        }
    }

    private void handle(final String commandLine) {
        final String[] parts = commandLine.split("\\s+");
        final String command = parts[0].toLowerCase();
        try {
            switch (command) {
                case "park":
                    ensureLength(parts, 4, "Usage: park <licensePlate> <vehicleType> <driverAge>");
                    final ParkResponse parkResponse = parkVehicleUseCase.execute(new ParkRequest(
                            parts[1],
                            VehicleType.valueOf(parts[2].toUpperCase()),
                            Integer.parseInt(parts[3])));
                    output.println(String.format("PARKED ticket=%s slot=%s level=%d at=%s",
                            parkResponse.getTicketId(),
                            parkResponse.getSlotNumber(),
                            parkResponse.getLevelNumber(),
                            parkResponse.getEntryTime()));
                    break;
                case "unpark":
                    ensureLength(parts, 2, "Usage: unpark <ticketId>");
                    final UnparkResponse unparkResponse = unparkVehicleUseCase.execute(new UnparkRequest(parts[1]));
                    output.println(String.format("UNPARKED ticket=%s duration=%s exit=%s",
                            unparkResponse.getTicketId(),
                            unparkResponse.getDuration(),
                            unparkResponse.getExitTime()));
                    break;
                case "availability":
                    final AvailabilityResponse availabilityResponse = parkingQueryService.getAvailability(lotId);
                    output.println(availabilityResponse.getLevelAvailability());
                    output.println(String.format("AVAILABLE %d / %d occupancy=%.2f",
                            availabilityResponse.getAvailableSlots(),
                            availabilityResponse.getTotalSlots(),
                            availabilityResponse.getOccupancyRate()));
                    break;
                case "vehicle":
                    ensureLength(parts, 2, "Usage: vehicle <licensePlate>");
                    final Optional<ParkingTicket> ticket = parkingQueryService.findVehicle(parts[1]);
                    output.println(ticket.map(value -> String.format("FOUND ticket=%s slot=%s level=%s",
                                    value.getTicketId(), value.getSlotId(), value.getLevelId()))
                            .orElse("NOT_FOUND"));
                    break;
                case "active_tickets":
                    final List<String> tickets = parkingQueryService.getActiveTickets().stream()
                            .map(value -> value.getTicketId() + ":" + value.getVehicle().getLicensePlate())
                            .collect(Collectors.toList());
                    output.println(tickets);
                    break;
                case "help":
                    output.println("Commands: park, unpark, availability, vehicle, active_tickets, help, quit");
                    break;
                default:
                    output.println("Unknown command: " + command);
                    break;
            }
        } catch (RuntimeException ex) {
            output.println("ERROR " + ex.getMessage());
        }
    }

    private void ensureLength(final String[] parts, final int expected, final String message) {
        if (parts.length != expected) {
            throw new IllegalArgumentException(message);
        }
    }
}
