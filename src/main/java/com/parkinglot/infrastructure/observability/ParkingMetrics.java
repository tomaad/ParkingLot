package com.parkinglot.infrastructure.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ParkingMetrics {
    private final MeterRegistry meterRegistry;
    private final Counter parkedCounter;
    private final Counter unparkedCounter;
    private final Timer parkDurationTimer;
    private final Timer unparkDurationTimer;
    private final AtomicInteger occupiedSlots = new AtomicInteger();
    private final AtomicInteger totalSlots = new AtomicInteger();

    public ParkingMetrics(final MeterRegistry meterRegistry) {
        this.meterRegistry = Objects.requireNonNull(meterRegistry, "meterRegistry must not be null");
        this.parkedCounter = Counter.builder("parking.vehicles.parked")
                .description("Total number of successfully parked vehicles")
                .register(meterRegistry);
        this.unparkedCounter = Counter.builder("parking.vehicles.unparked")
                .description("Total number of successfully unparked vehicles")
                .register(meterRegistry);
        this.parkDurationTimer = Timer.builder("parking.park.duration")
                .description("Execution duration for park operations")
                .register(meterRegistry);
        this.unparkDurationTimer = Timer.builder("parking.unpark.duration")
                .description("Execution duration for unpark operations")
                .register(meterRegistry);

        Gauge.builder("parking.occupancy.current", occupiedSlots, occupied -> {
                    final int total = totalSlots.get();
                    return total == 0 ? 0.0d : occupied.doubleValue() / total;
                })
                .description("Current parking occupancy ratio")
                .register(meterRegistry);
        Gauge.builder("parking.slots.available", occupiedSlots, occupied -> Math.max(0.0d, totalSlots.get() - occupied.doubleValue()))
                .description("Current number of available parking slots")
                .register(meterRegistry);
    }

    public void recordPark() {
        parkedCounter.increment();
    }

    public void recordPark(final Duration duration) {
        recordPark();
        parkDurationTimer.record(Objects.requireNonNull(duration, "duration must not be null"));
    }

    public void recordUnpark() {
        unparkedCounter.increment();
    }

    public void recordUnpark(final Duration duration) {
        recordUnpark();
        unparkDurationTimer.record(Objects.requireNonNull(duration, "duration must not be null"));
    }

    public void recordError(final String type) {
        Counter.builder("parking.errors")
                .description("Parking workflow errors grouped by type")
                .tag("type", normalizeTag(type))
                .register(meterRegistry)
                .increment();
    }

    public void updateOccupancy(final int occupied, final int total) {
        if (occupied < 0) {
            throw new IllegalArgumentException("occupied must be greater than or equal to zero");
        }
        if (total < 0) {
            throw new IllegalArgumentException("total must be greater than or equal to zero");
        }
        if (occupied > total) {
            throw new IllegalArgumentException("occupied must be less than or equal to total");
        }
        occupiedSlots.set(occupied);
        totalSlots.set(total);
    }

    private String normalizeTag(final String type) {
        final String normalized = Objects.requireNonNull(type, "type must not be null").trim();
        if (normalized.isEmpty()) {
            return "unknown";
        }
        return normalized.toLowerCase(Locale.ROOT)
                .replace(' ', '-')
                .replace('_', '-');
    }
}
