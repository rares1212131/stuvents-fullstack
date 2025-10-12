package org.example.studentsevents.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "organizer_applications")
@Getter
@Setter
@NoArgsConstructor
public class OrganizerApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // This links the application directly to the user who submitted it.
    // A user can only have one application, so this is a unique constraint.
    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // The reason the user provided in the form.
    @Column(length = 2000, nullable = false)
    private String reason;

    // The current status of the application.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    // Standard audit fields.
    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}