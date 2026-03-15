package dev.jos.back.config;

import dev.jos.back.dto.ErrorResponseDTO;
import dev.jos.back.exceptions.cart.CartItemNotFoundException;
import dev.jos.back.exceptions.cart.CartNotFoundException;
import dev.jos.back.exceptions.email.EmailNotSentException;
import dev.jos.back.exceptions.event.EventAlreadyExistsException;
import dev.jos.back.exceptions.event.EventNotFoundException;
import dev.jos.back.exceptions.event.EventSoldOutException;
import dev.jos.back.exceptions.offertype.OfferNotFoundException;
import dev.jos.back.exceptions.offertype.OfferTypeAlreadyExistsException;
import dev.jos.back.exceptions.sport.SportNotFoundException;
import dev.jos.back.exceptions.twofactor.BadTwoFactorCodeException;
import dev.jos.back.exceptions.twofactor.TwoFactorCodeNotFoundException;
import dev.jos.back.exceptions.twofactor.TwoFactorMaxAttemptsException;
import dev.jos.back.exceptions.user.InvalidPasswordException;
import dev.jos.back.exceptions.user.UserAlreadyExistsException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionConfig {

    private static final Map<Class<? extends Exception>, HttpStatus> EXCEPTION_STATUS_MAP = Map.ofEntries(
            Map.entry(UserAlreadyExistsException.class, HttpStatus.CONFLICT),
            Map.entry(EventAlreadyExistsException.class, HttpStatus.CONFLICT),
            Map.entry(UsernameNotFoundException.class, HttpStatus.NOT_FOUND),
            Map.entry(EventNotFoundException.class, HttpStatus.NOT_FOUND),
            Map.entry(BadCredentialsException.class, HttpStatus.UNAUTHORIZED),
            Map.entry(ExpiredJwtException.class, HttpStatus.UNAUTHORIZED),
            Map.entry(JwtException.class, HttpStatus.UNAUTHORIZED),
            Map.entry(TwoFactorCodeNotFoundException.class, HttpStatus.BAD_REQUEST),
            Map.entry(TwoFactorMaxAttemptsException.class, HttpStatus.TOO_MANY_REQUESTS),
            Map.entry(BadTwoFactorCodeException.class, HttpStatus.BAD_REQUEST),
            Map.entry(InvalidPasswordException.class, HttpStatus.BAD_REQUEST),
            Map.entry(EmailNotSentException.class, HttpStatus.INTERNAL_SERVER_ERROR),
            Map.entry(OfferTypeAlreadyExistsException.class, HttpStatus.CONFLICT),
            Map.entry(SportNotFoundException.class, HttpStatus.NOT_FOUND),
            Map.entry(CartNotFoundException.class, HttpStatus.NOT_FOUND),
            Map.entry(CartItemNotFoundException.class, HttpStatus.NOT_FOUND),
            Map.entry(OfferNotFoundException.class, HttpStatus.NOT_FOUND),
            Map.entry(EventSoldOutException.class, HttpStatus.BAD_REQUEST)
    );

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleAll(Exception ex) {
        HttpStatus status = EXCEPTION_STATUS_MAP.entrySet().stream()
                .filter(entry -> entry.getKey().isInstance(ex))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(HttpStatus.INTERNAL_SERVER_ERROR);

        String message = status == HttpStatus.INTERNAL_SERVER_ERROR
                ? "Une erreur est survenue"
                : ex.getMessage();
        log.error("Exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(status).body(ErrorResponseDTO.of(status.value(), message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            errors.put(fieldName, error.getDefaultMessage());
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}