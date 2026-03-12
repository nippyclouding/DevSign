package devsign_server.api.global.auth;

import devsign_server.api.domain.member.dto.MemberResponse;
import devsign_server.api.domain.member.entity.Member;
import devsign_server.api.domain.member.entity.Section;
import devsign_server.api.domain.member.repository.MemberRepository;
import devsign_server.api.fixture.MemberFixture;
import devsign_server.api.global.auth.dto.LoginRequest;
import devsign_server.api.global.auth.dto.LoginResponse;
import devsign_server.api.global.auth.dto.SignupRequest;
import devsign_server.api.global.auth.jwt.JwtProvider;
import devsign_server.api.global.exception.CustomException;
import devsign_server.api.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@DisplayName("AuthService 단위 테스트")
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    // ================================
    // signup
    // ================================

    @Test
    @DisplayName("회원가입 성공 시 Member를 저장한다")
    void signup_success() {
        // given
        SignupRequest request = new SignupRequest("김개발", Section.DEVELOPER, "dev@test.com", "password123");
        given(memberRepository.existsByEmail(request.email())).willReturn(false);
        given(passwordEncoder.encode(request.password())).willReturn("encodedPassword");

        // when
        authService.signup(request);

        // then
        then(memberRepository).should().save(any(Member.class));
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 회원가입 시 DUPLICATE_EMAIL 예외를 던진다")
    void signup_duplicateEmail_throwsException() {
        // given
        SignupRequest request = new SignupRequest("김개발", Section.DEVELOPER, "dev@test.com", "password123");
        given(memberRepository.existsByEmail(request.email())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.DUPLICATE_EMAIL));

        then(memberRepository).should(never()).save(any());
    }

    // ================================
    // login
    // ================================

    @Test
    @DisplayName("로그인 성공 시 JWT 토큰이 포함된 LoginResponse를 반환한다")
    void login_success() {
        // given
        Member member = MemberFixture.developer();
        LoginRequest request = new LoginRequest("dev@test.com", "rawPassword");

        given(memberRepository.findByEmail(request.email())).willReturn(Optional.of(member));
        given(passwordEncoder.matches(request.password(), member.getPassword())).willReturn(true);
        given(jwtProvider.generateToken(member.getMemberId(), member.getEmail())).willReturn("jwt-token");

        // when
        LoginResponse response = authService.login(request);

        // then
        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.email()).isEqualTo(member.getEmail());
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시 INVALID_PASSWORD 예외를 던진다")
    void login_emailNotFound_throwsException() {
        // given
        LoginRequest request = new LoginRequest("unknown@test.com", "password");
        given(memberRepository.findByEmail(request.email())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_PASSWORD));
    }

    @Test
    @DisplayName("비밀번호가 틀리면 INVALID_PASSWORD 예외를 던진다")
    void login_wrongPassword_throwsException() {
        // given
        Member member = MemberFixture.developer();
        LoginRequest request = new LoginRequest("dev@test.com", "wrongPassword");

        given(memberRepository.findByEmail(request.email())).willReturn(Optional.of(member));
        given(passwordEncoder.matches(request.password(), member.getPassword())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_PASSWORD));
    }

    // ================================
    // getMe
    // ================================

    @Test
    @DisplayName("내 정보 조회 성공 시 MemberResponse를 반환한다")
    void getMe_success() {
        // given
        Member member = MemberFixture.developer();
        given(memberRepository.findById(member.getMemberId())).willReturn(Optional.of(member));

        // when
        MemberResponse response = authService.getMe(member.getMemberId());

        // then
        assertThat(response.id()).isEqualTo(member.getMemberId());
        assertThat(response.email()).isEqualTo(member.getEmail());
        assertThat(response.name()).isEqualTo(member.getName());
    }

    @Test
    @DisplayName("존재하지 않는 memberId로 getMe 시 MEMBER_NOT_FOUND 예외를 던진다")
    void getMe_memberNotFound_throwsException() {
        // given
        given(memberRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.getMe(999L))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.MEMBER_NOT_FOUND));
    }
}
