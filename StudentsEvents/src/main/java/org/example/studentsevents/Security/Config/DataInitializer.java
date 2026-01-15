package org.example.studentsevents.Security.Config;

import lombok.RequiredArgsConstructor;
import org.example.studentsevents.Repository.CategoryRepository; // <<< IMPORT
import org.example.studentsevents.Repository.CityRepository;       // <<< IMPORT
import org.example.studentsevents.Repository.RoleRepository;
import org.example.studentsevents.Repository.UserRepository;
import org.example.studentsevents.model.Category; // <<< IMPORT
import org.example.studentsevents.model.City;       // <<< IMPORT
import org.example.studentsevents.model.Role;
import org.example.studentsevents.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CategoryRepository categoryRepository;
    private final CityRepository cityRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        Role userRole = createRoleIfNotFound("ROLE_USER");
        Role adminRole = createRoleIfNotFound("ROLE_ADMIN");
        Role organizerRole = createRoleIfNotFound("ROLE_ORGANIZER");

        createCategoriesIfNotFound();
        createCitiesIfNotFound();

        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = new User();
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setVerified(true);
            admin.setRoles(Set.of(adminRole, userRole, organizerRole));
            userRepository.save(admin);
            System.out.println("Admin user created successfully!");
        }
    }

    private Role createRoleIfNotFound(String name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(new Role(name)));
    }

    private void createCategoriesIfNotFound() {
        if (categoryRepository.count() == 0) {
            List<Category> categories = List.of(
                    new Category("Music"),
                    new Category("Technology"),
                    new Category("Sports"),
                    new Category("Arts & Culture"),
                    new Category("Workshops")
            );
            categoryRepository.saveAll(categories);
            System.out.println("Default categories created.");
        }
    }

    private void createCitiesIfNotFound() {
        if (cityRepository.count() == 0) {
            List<City> cities = List.of(
                    new City("New York"),
                    new City("London"),
                    new City("Tokyo"),
                    new City("Paris"),
                    new City("Sydney")
            );
            cityRepository.saveAll(cities);
            System.out.println("Default cities created.");
        }
    }
}