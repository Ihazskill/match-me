package com.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateRequest {
    @NotBlank
    @Size(max = 80)
    private String firstName;
    @NotBlank
    @Size(max = 80)
    private String lastName;
    @Size(max = 2000)
    private String aboutMe;
    @NotNull
    @Min(18)
    @Max(120)
    private Integer age;
    private String profilePictureUrl;
}