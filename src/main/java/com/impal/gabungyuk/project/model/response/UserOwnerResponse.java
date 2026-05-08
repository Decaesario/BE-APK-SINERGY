package com.impal.gabungyuk.project.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserOwnerResponse {
    private Integer id;
    private String fullName;
    private String email;
    private String profilePicture;
}
