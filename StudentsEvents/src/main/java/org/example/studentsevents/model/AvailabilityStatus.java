package org.example.studentsevents.model;

/**
 * Represents the public-facing sales status of a TicketType.
 * This is sent to the frontend to control the UI without revealing exact numbers.
 */
public enum AvailabilityStatus {

    AVAILABLE,
    SELLING_FAST,
    LIMITED,
    SOLD_OUT
}