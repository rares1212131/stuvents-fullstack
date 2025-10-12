package org.example.studentsevents.Repository;

import org.example.studentsevents.model.ApplicationStatus;
import org.example.studentsevents.model.OrganizerApplication;
import org.example.studentsevents.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizerApplicationRepository extends JpaRepository<OrganizerApplication, Long> {


    Optional<OrganizerApplication> findByUser(User user);
    List<OrganizerApplication> findByStatus(ApplicationStatus status);
}