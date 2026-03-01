package devsign_server.api.domain.review.controller;

import devsign_server.api.domain.review.dto.CreateReviewRequest;
import devsign_server.api.domain.review.dto.ReviewResponse;
import devsign_server.api.domain.review.service.ReviewService;
import devsign_server.api.global.auth.MemberDetails;
import devsign_server.api.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReviewResponse> createReview(
            @AuthenticationPrincipal MemberDetails memberDetails,
            @Valid @RequestBody CreateReviewRequest request
    ) {
        return ApiResponse.ok(reviewService.createReview(memberDetails.getMemberId(), request));
    }
}
