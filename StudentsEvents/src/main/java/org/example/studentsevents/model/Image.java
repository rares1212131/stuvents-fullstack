package org.example.studentsevents.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "images")
@Getter
@Setter
@NoArgsConstructor
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // This will be the unique name we generate, e.g., "a1b2c3d4-e5f6.jpg"
    @Column(nullable = false, unique = true)
    private String fileName;

    public Image(String fileName) {
        this.fileName = fileName;
    }
}