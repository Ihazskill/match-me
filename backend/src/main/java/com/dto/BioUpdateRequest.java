package com.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BioUpdateRequest {
    @NotEmpty
    private Set<String> interests;
    @NotEmpty
    private Set<String> hobbies;
    @NotBlank
    private String musicTaste;
    @NotBlank
    private String foodPreference;
    @NotBlank
    private String travelStyle;
    @NotBlank
    private String lifestyle;
    @NotBlank
    private String personality;
    @NotNull
    private Long locationId;
    @DecimalMin("-90.0")
    @DecimalMax("90.0")
    private Double latitude;
    @DecimalMin("-180.0")
    @DecimalMax("180.0")
    private Double longitude;
    @DecimalMin("1.0")
    @DecimalMax("500.0")
    private Double maxRadiusKm;
    @Size(max = 2000)
    private String lookingFor;
    private Set<String> seekingInterests;
    private Long seekingLocationId;
}