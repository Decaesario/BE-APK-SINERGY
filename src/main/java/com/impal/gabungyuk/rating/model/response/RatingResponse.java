package com.impal.gabungyuk.rating.model.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatingResponse {

    private Long ratingId;

    private Integer projectId;
    private String projectTitle;

    private Integer ratedUserId;
    private String ratedUserName;
    private String ratedUserProfilePicture;

    private Integer ownerUserId;
    private String ownerName;
    private String ownerProfilePicture;

    private Integer ratingValue;
    private String review;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}