package org.example.studentsevents.Repository;

import org.example.studentsevents.model.Event;
import org.example.studentsevents.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    Page<Event> findByNameContainingIgnoreCase(String eventName, Pageable pageable);
    Page<Event> findByCategoryNameIgnoreCase(String categoryName, Pageable pageable);
    Page<Event> findByCityNameIgnoreCase(String cityName, Pageable pageable);
    Page<Event> findByCategoryNameIgnoreCaseAndCityNameIgnoreCase(String categoryName, String cityName, Pageable pageable);
    Page<Event> findByOrganizer(User organizer, Pageable pageable);
    boolean existsByCategoryId(Long categoryId);
    boolean existsByCityId(Long cityId);
}