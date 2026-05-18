package com.impal.gabungyuk.rating.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AverageRatingResponse {

    private Integer userId;
    private String namaLengkap;
    private String profilePicture;

    private Double averageRating;
    private Long totalRatings;
}