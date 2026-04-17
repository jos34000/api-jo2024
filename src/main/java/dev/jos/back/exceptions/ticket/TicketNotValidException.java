package dev.jos.back.exceptions.ticket;

public class TicketNotValidException extends RuntimeException {
    public TicketNotValidException(String message) {
        super(message);
    }
}
