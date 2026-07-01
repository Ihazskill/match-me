package com.dto;

import lombok.*;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BioUpdateRequest {
    private Set<String> interests;
    private Set<String> hobbies;
    private String musicTaste;
    private String foodPreference;
    private String travelStyle;
    private String lifestyle;
    private String personality;
    private Long locationId;
    private String lookingFor;
    private Set<String> seekingInterests;
    private Long seekingLocationId;
}