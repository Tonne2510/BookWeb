package com.bookweb.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorDTO {
    private String id;
    private String name;
    private String slug;
    private String bio;
    private String imageUrl;
    private String dateOfBirth;
    private String nationality;
    private String status;
}
