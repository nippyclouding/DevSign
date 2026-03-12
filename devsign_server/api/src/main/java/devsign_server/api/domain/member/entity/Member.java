package devsign_server.api.domain.member.entity;

import devsign_server.api.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Section section;

    @Column(nullable = false)
    @Builder.Default
    private int reputationSum = 0;

    @Column(nullable = false)
    @Builder.Default
    private int reputationCount = 0;

    @Column(columnDefinition = "TEXT")
    private String profileData;

    public void updateProfile(String name, String profileData) {
        if (name != null) this.name = name;
        if (profileData != null) this.profileData = profileData;
    }

    public void addReputation(int score) {
        this.reputationSum += score;
        this.reputationCount += 1;
    }

    /** 평균 평점 (리뷰 없으면 0.0) */
    public double getReputation() {
        return reputationCount > 0 ? (double) reputationSum / reputationCount : 0.0;
    }
}
