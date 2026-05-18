package com.impal.gabungyuk.rating.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingRequest {

    private Integer projectId;
    private Integer ratedUserId;
    private Integer ratingValue;
    private String review;
}