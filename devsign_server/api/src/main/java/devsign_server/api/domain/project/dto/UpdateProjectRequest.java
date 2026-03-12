package devsign_server.api.domain.project.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateProjectRequest(

        @Size(max = 100, message = "제목은 100자 이하로 입력해주세요.")
        String mainTitle,

        @Size(max = 200, message = "부제목은 200자 이하로 입력해주세요.")
        String subtitle,

        @Size(max = 5000, message = "내용은 5000자 이하로 입력해주세요.")
        String content,

        LocalDate startDate,
        LocalDate endDate,
        Integer neededDevelopers,
        Integer neededDesigners
) {}
