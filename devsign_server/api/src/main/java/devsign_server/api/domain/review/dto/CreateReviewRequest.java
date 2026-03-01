package devsign_server.api.domain.review.dto;

import jakarta.validation.constraints.*;

public record CreateReviewRequest(

        @NotNull(message = "프로젝트 ID를 입력해주세요.")
        Long postId,

        @NotNull(message = "리뷰 대상 회원 ID를 입력해주세요.")
        Long revieweeId,

        @NotBlank(message = "리뷰 내용을 입력해주세요.")
        String content,

        @NotNull
        @Min(value = 1, message = "별점은 1점 이상이어야 합니다.")
        @Max(value = 5, message = "별점은 5점 이하이어야 합니다.")
        Integer rating
) {}
