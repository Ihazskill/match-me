package com.dto;

import lombok.*;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserBioDTO {
    private Long id;
    private Set<String> interests;
    private Set<String> hobbies;
    private String musicTaste;
    private String foodPreference;
    private String travelStyle;
    private String lifestyle;
    private String personality;
    private String location;
    private String lookingFor;
    private Set<String> seekingInterests;
    private String seekingLocation;
}