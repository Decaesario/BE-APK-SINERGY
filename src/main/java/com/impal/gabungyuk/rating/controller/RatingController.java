package com.impal.gabungyuk.rating.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.impal.gabungyuk.core.model.SuccessResponse;
import com.impal.gabungyuk.rating.model.request.RatingRequest;
import com.impal.gabungyuk.rating.model.response.AverageRatingResponse;
import com.impal.gabungyuk.rating.model.response.RatingResponse;
import com.impal.gabungyuk.rating.model.response.UserRatingSummaryResponse;
import com.impal.gabungyuk.rating.service.RatingService;

@RestController
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping(
            value = "/api/v1/ratings",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SuccessResponse<RatingResponse> createRating(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody RatingRequest request
    ) {
        RatingResponse response = ratingService.createRating(
                authorizationHeader,
                request
        );

        return SuccessResponse.<RatingResponse>builder()
                .status(200)
                .message("Rating created successfully")
                .data(response)
                .build();
    }

    @GetMapping(
            value = "/api/v1/ratings/users/{userId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SuccessResponse<UserRatingSummaryResponse> getUserRatings(
            @PathVariable Integer userId
    ) {
        UserRatingSummaryResponse response = ratingService.getUserRatings(userId);

        return SuccessResponse.<UserRatingSummaryResponse>builder()
                .status(200)
                .message("User ratings retrieved successfully")
                .data(response)
                .build();
    }

    @GetMapping(
            value = "/api/v1/ratings/users/{userId}/average",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SuccessResponse<AverageRatingResponse> getUserAverageRating(
            @PathVariable Integer userId
    ) {
        AverageRatingResponse response = ratingService.getUserAverageRating(userId);

        return SuccessResponse.<AverageRatingResponse>builder()
                .status(200)
                .message("Average rating retrieved successfully")
                .data(response)
                .build();
    }

    @GetMapping(
            value = "/api/v1/ratings/projects/{projectId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public SuccessResponse<List<RatingResponse>> getProjectRatings(
            @PathVariable Integer projectId
    ) {
        List<RatingResponse> response = ratingService.getProjectRatings(projectId);

        return SuccessResponse.<List<RatingResponse>>builder()
                .status(200)
                .message("Project ratings retrieved successfully")
                .data(response)
                .build();
    }
}