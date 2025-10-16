package org.example.studentsevents.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.NaturalId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    @NaturalId
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @JsonIgnore
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Booking> bookings = new ArrayList<>();

    @Column(length = 512) // Use a longer column for full URLs
    private String profilePictureUrl;

    @JsonIgnore
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private OrganizerApplication organizerApplication;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean verified = false;

    /**
     * A unique, randomly generated token sent to the user's email for verification.
     * This will be nullable because it should be cleared after verification.
     */
    @Column(unique = true)
    private String verificationToken;

    /**
     * The timestamp indicating when the verification token expires.
     */
    private LocalDateTime verificationTokenExpiryDate;
    @Column(unique = true)
    private String passwordResetToken;

    /**
     * The timestamp indicating when the password reset token expires (e.g., 1 hour).
     */
    private LocalDateTime resetTokenExpiryDate;

}
