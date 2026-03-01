package devsign_server.api.domain.applicant.entity;

import devsign_server.api.domain.member.entity.Member;
import devsign_server.api.domain.project.entity.Project;
import devsign_server.api.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "applicant",
        uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "member_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Applicant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long applicantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ApplicantStatus status = ApplicantStatus.PENDING;

    public void updateStatus(ApplicantStatus status) {
        this.status = status;
    }

    public boolean isApproved() {
        return this.status == ApplicantStatus.APPROVED;
    }
}
