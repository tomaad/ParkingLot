package com.parkinglot.domain.model;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Immutable license plate value object with format validation.
 */
public final class LicensePlate {
    private static final Pattern VALID_PATTERN = Pattern.compile("^[A-Z0-9-]{2,15}$");

    private final String value;

    /**
     * Creates a license plate value object.
     *
     * @param value the raw license plate text
     */
    public LicensePlate(final String value) {
        final String normalized = Objects.requireNonNull(value, "value must not be null").trim().toUpperCase();
        if (!VALID_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException("Invalid license plate format: " + value);
        }
        this.value = normalized;
    }

    /**
     * Returns the normalized plate value.
     *
     * @return normalized license plate value
     */
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof LicensePlate)) {
            return false;
        }
        final LicensePlate that = (LicensePlate) other;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
