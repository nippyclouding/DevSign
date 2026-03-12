package devsign_server.api.fixture;

import devsign_server.api.domain.member.entity.Member;
import devsign_server.api.domain.member.entity.Section;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 테스트에서 공통으로 사용하는 Member 객체 생성 도우미.
 * 빌더를 직접 사용하는 것보다 일관된 테스트 데이터를 보장한다.
 */
public final class MemberFixture {

    private MemberFixture() {}

    public static Member developer() {
        return developer(1L);
    }

    public static Member developer(Long memberId) {
        Member member = Member.builder()
                .email("dev@test.com")
                .password("$2a$10$encodedPasswordHash")
                .name("김개발")
                .section(Section.DEVELOPER)
                .build();
        ReflectionTestUtils.setField(member, "memberId", memberId);
        return member;
    }

    public static Member designer() {
        return designer(2L);
    }

    public static Member designer(Long memberId) {
        Member member = Member.builder()
                .email("design@test.com")
                .password("$2a$10$encodedPasswordHash")
                .name("이디자인")
                .section(Section.DESIGNER)
                .build();
        ReflectionTestUtils.setField(member, "memberId", memberId);
        return member;
    }
}
