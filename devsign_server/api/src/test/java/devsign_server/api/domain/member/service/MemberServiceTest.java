package devsign_server.api.domain.member.service;

import devsign_server.api.domain.member.dto.MemberResponse;
import devsign_server.api.domain.member.dto.UpdateProfileRequest;
import devsign_server.api.domain.member.entity.Member;
import devsign_server.api.domain.member.repository.MemberRepository;
import devsign_server.api.fixture.MemberFixture;
import devsign_server.api.global.exception.CustomException;
import devsign_server.api.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("MemberService 단위 테스트")
@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private MemberRepository memberRepository;

    @Test
    @DisplayName("memberId로 프로필 조회 성공 시 MemberResponse를 반환한다")
    void getProfile_success() {
        // given
        Member member = MemberFixture.developer();
        given(memberRepository.findById(member.getMemberId())).willReturn(Optional.of(member));

        // when
        MemberResponse response = memberService.getProfile(member.getMemberId());

        // then
        assertThat(response.id()).isEqualTo(member.getMemberId());
        assertThat(response.email()).isEqualTo(member.getEmail());
    }

    @Test
    @DisplayName("존재하지 않는 memberId 조회 시 MEMBER_NOT_FOUND 예외를 던진다")
    void getProfile_notFound_throwsException() {
        // given
        given(memberRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.getProfile(999L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.MEMBER_NOT_FOUND));
    }

    @Test
    @DisplayName("프로필 수정 성공 시 변경된 값이 반영된 MemberResponse를 반환한다")
    void updateProfile_success() {
        // given
        Member member = MemberFixture.developer();
        UpdateProfileRequest request = new UpdateProfileRequest("수정된이름", "새 프로필 데이터");
        given(memberRepository.findById(member.getMemberId())).willReturn(Optional.of(member));

        // when
        MemberResponse response = memberService.updateProfile(member.getMemberId(), request);

        // then
        assertThat(response.name()).isEqualTo("수정된이름");
        assertThat(response.profileData()).isEqualTo("새 프로필 데이터");
    }

    @Test
    @DisplayName("프로필 수정 시 존재하지 않는 회원이면 MEMBER_NOT_FOUND 예외를 던진다")
    void updateProfile_notFound_throwsException() {
        // given
        UpdateProfileRequest request = new UpdateProfileRequest("이름", null);
        given(memberRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.updateProfile(999L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.MEMBER_NOT_FOUND));
    }

    @Test
    @DisplayName("회원 탈퇴 성공 시 delete를 호출한다")
    void deleteAccount_success() {
        // given
        Member member = MemberFixture.developer();
        given(memberRepository.findById(member.getMemberId())).willReturn(Optional.of(member));

        // when
        memberService.deleteAccount(member.getMemberId());

        // then
        then(memberRepository).should().delete(member);
    }

    @Test
    @DisplayName("존재하지 않는 회원 탈퇴 시 MEMBER_NOT_FOUND 예외를 던진다")
    void deleteAccount_notFound_throwsException() {
        // given
        given(memberRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.deleteAccount(999L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.MEMBER_NOT_FOUND));
    }
}
