package com.parkinglot.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("LicensePlate")
class LicensePlateTest {

    @Nested
    @DisplayName("construction")
    class ConstructionTests {

        @Test
        @DisplayName("accepts valid formats")
        void validLicensePlateFormatsAccepted() {
            assertEquals("AB", new LicensePlate("ab").getValue());
            assertEquals("ABC-123", new LicensePlate("abc-123").getValue());
            assertEquals("ZX9-88", new LicensePlate("ZX9-88").getValue());
        }

        @Test
        @DisplayName("rejects null empty and blank values")
        void nullEmptyBlankRejected() {
            assertThrows(NullPointerException.class, () -> new LicensePlate(null));
            assertThrows(IllegalArgumentException.class, () -> new LicensePlate(""));
            assertThrows(IllegalArgumentException.class, () -> new LicensePlate("   "));
        }

        @Test
        @DisplayName("rejects invalid formats")
        void invalidFormatRejected() {
            assertThrows(IllegalArgumentException.class, () -> new LicensePlate("A"));
            assertThrows(IllegalArgumentException.class, () -> new LicensePlate("plate_with_underscore"));
            assertThrows(IllegalArgumentException.class, () -> new LicensePlate("BAD SPACE"));
        }
    }

    @Nested
    @DisplayName("value semantics")
    class ValueSemanticsTests {

        @Test
        @DisplayName("supports equals and hashCode")
        void equalsAndHashCodeWorkCorrectly() {
            LicensePlate first = new LicensePlate("abc-123");
            LicensePlate second = new LicensePlate("ABC-123");
            LicensePlate third = new LicensePlate("XYZ-999");

            assertEquals(first, second);
            assertEquals(first.hashCode(), second.hashCode());
            assertNotEquals(first, third);
        }

        @Test
        @DisplayName("returns normalized value")
        void getValueReturnsTheValue() {
            LicensePlate licensePlate = new LicensePlate(" ab-123 ");

            assertEquals("AB-123", licensePlate.getValue());
        }
    }
}
