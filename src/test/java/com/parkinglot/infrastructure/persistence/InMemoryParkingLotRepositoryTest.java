package com.parkinglot.infrastructure.persistence;

import com.parkinglot.domain.model.ParkingLevel;
import com.parkinglot.domain.model.ParkingLot;
import com.parkinglot.domain.model.ParkingSlot;
import com.parkinglot.domain.model.SlotStatus;
import com.parkinglot.domain.model.SlotType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("InMemoryParkingLotRepository")
class InMemoryParkingLotRepositoryTest {

    private InMemoryParkingLotRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryParkingLotRepository();
    }

    @Test
    @DisplayName("saves and finds by id")
    void saveAndFindById() {
        ParkingLot parkingLot = createParkingLot("lot-1", "Original Lot");

        repository.save(parkingLot);

        assertTrue(repository.findById("lot-1").isPresent());
        assertSame(parkingLot, repository.findById("lot-1").orElseThrow());
    }

    @Test
    @DisplayName("findAll returns all saved lots")
    void findAllReturnsAllSaved() {
        ParkingLot first = createParkingLot("lot-1", "Lot One");
        ParkingLot second = createParkingLot("lot-2", "Lot Two");
        repository.save(first);
        repository.save(second);

        assertEquals(2, repository.findAll().size());
    }

    @Test
    @DisplayName("save updates existing lot")
    void saveUpdatesExisting() {
        repository.save(createParkingLot("lot-1", "Lot One"));
        ParkingLot updated = createParkingLot("lot-1", "Updated Lot");

        repository.save(updated);

        assertEquals(1, repository.findAll().size());
        assertEquals("Updated Lot", repository.findById("lot-1").orElseThrow().getName());
    }

    private ParkingLot createParkingLot(String lotId, String name) {
        ParkingLevel level = new ParkingLevel("level-1", 1, List.of(
                new ParkingSlot("slot-1", "A1", SlotType.REGULAR, SlotStatus.AVAILABLE, "level-1")));
        return new ParkingLot(lotId, name, "123 Main St", List.of(level));
    }
}
