package devsign_server.api.domain.project.entity;

import devsign_server.api.domain.member.entity.Member;
import devsign_server.api.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "project")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Project extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long projectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false, length = 100)
    private String mainTitle;

    @Column(length = 200)
    private String subtitle;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ProjectStatus status = ProjectStatus.RECRUITING;

    @Column(nullable = false)
    @Builder.Default
    private int neededDevelopers = 0;

    @Column(nullable = false)
    @Builder.Default
    private int neededDesigners = 0;

    public void updateStatus(ProjectStatus status) {
        this.status = status;
    }

    public void update(String mainTitle, String subtitle, String content,
                       java.time.LocalDate startDate, java.time.LocalDate endDate,
                       Integer neededDevelopers, Integer neededDesigners) {
        if (mainTitle != null) this.mainTitle = mainTitle;
        if (subtitle != null) this.subtitle = subtitle;
        if (content != null) this.content = content;
        if (startDate != null) this.startDate = startDate;
        if (endDate != null) this.endDate = endDate;
        if (neededDevelopers != null) this.neededDevelopers = neededDevelopers;
        if (neededDesigners != null) this.neededDesigners = neededDesigners;
    }

    public boolean isAuthor(Long memberId) {
        return this.member.getMemberId().equals(memberId);
    }
}
