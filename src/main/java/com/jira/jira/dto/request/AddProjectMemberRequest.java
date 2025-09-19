package com.jira.jira.dto.request;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddProjectMemberRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @JsonProperty("email")
    private String email;

    @JsonProperty("role")
    private String role = "MEMBER"; // Default role
}
