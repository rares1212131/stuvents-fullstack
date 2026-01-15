package org.example.studentsevents.Repository;

import jakarta.persistence.LockModeType; // <-- Import this
import org.example.studentsevents.model.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock; // <-- Import this
import org.springframework.stereotype.Repository;

import java.util.Optional; // <-- Make sure this is imported

@Repository
public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<TicketType> findById(Long id);
}