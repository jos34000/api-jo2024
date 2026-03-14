package dev.jos.back.exceptions.event;

public class EventSoldOutException extends RuntimeException {
    public EventSoldOutException(String message) {
        super(message);
    }
}
