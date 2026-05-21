package com.impal.gabungyuk.search.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchUserResponse {

    private Integer userId;
    private String namaLengkap;
    private String profilePicture;
    private String bio;
    private String institusi;
}