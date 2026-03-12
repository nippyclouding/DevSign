package devsign_server.api.domain.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateProjectRequest(

        @NotBlank(message = "제목을 입력해주세요.")
        @Size(max = 100, message = "제목은 100자 이하로 입력해주세요.")
        String mainTitle,

        @Size(max = 200, message = "부제목은 200자 이하로 입력해주세요.")
        String subtitle,

        @NotBlank(message = "내용을 입력해주세요.")
        @Size(max = 5000, message = "내용은 5000자 이하로 입력해주세요.")
        String content,

        @NotNull(message = "시작일을 입력해주세요.")
        LocalDate startDate,

        @NotNull(message = "종료일을 입력해주세요.")
        LocalDate endDate,

        @PositiveOrZero
        int neededDevelopers,

        @PositiveOrZero
        int neededDesigners
) {}
