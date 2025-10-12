package org.example.studentsevents.DTOResponse;


import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class JWTResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String email;
    private Set<String> roles;

    public JWTResponse(String token, Long id, String email, Set<String> roles) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.roles = roles;
    }
}