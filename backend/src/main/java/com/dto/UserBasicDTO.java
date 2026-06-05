package com.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class UserBasicDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String profilePictureUrl;
    private Integer age;
}