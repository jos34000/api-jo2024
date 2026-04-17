package dev.jos.back.exceptions.ticket;

public class TicketAlreadyScannedException extends RuntimeException {
    public TicketAlreadyScannedException(String message) {
        super(message);
    }
}
