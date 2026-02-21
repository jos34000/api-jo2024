package dev.jos.back.repository;

import dev.jos.back.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByIsActiveTrue();

    @Query("SELECT e FROM Event e WHERE e.isActive = true AND e.availableSlots > 0")
    List<Event> findAvailableEvents();

    Optional<Event> findByNameAndEventDate(String name, LocalDateTime eventDate);
}
