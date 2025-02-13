package com.example.bookexchange.model.dto.request;

        import jakarta.validation.constraints.NotBlank;
        import jakarta.validation.constraints.Size;
        import lombok.Data;

@Data
public class UserRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Country is required")
    private String country;

    @NotBlank(message = "City is required")
    private String city;

    private String profilePicture;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
}