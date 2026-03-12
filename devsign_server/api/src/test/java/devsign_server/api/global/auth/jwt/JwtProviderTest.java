package devsign_server.api.global.auth.jwt;

import devsign_server.api.global.exception.CustomException;
import devsign_server.api.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtProvider 단위 테스트")
class JwtProviderTest {

    private static final String SECRET = "test-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm";
    private static final long EXPIRATION = 86_400_000L; // 24h

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(SECRET, EXPIRATION);
    }

    @Test
    @DisplayName("토큰 생성 후 memberId를 정상적으로 파싱한다")
    void generateToken_thenParseMemberId() {
        // given
        Long memberId = 1L;
        String email = "test@test.com";

        // when
        String token = jwtProvider.generateToken(memberId, email);

        // then
        assertThat(jwtProvider.getMemberId(token)).isEqualTo(memberId);
    }

    @Test
    @DisplayName("유효한 토큰은 validateToken이 true를 반환한다")
    void validateToken_withValidToken_returnsTrue() {
        // given
        String token = jwtProvider.generateToken(1L, "test@test.com");

        // when & then
        assertThat(jwtProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("변조된 토큰은 validateToken이 false를 반환한다")
    void validateToken_withTamperedToken_returnsFalse() {
        // given
        String token = jwtProvider.generateToken(1L, "test@test.com");
        String tamperedToken = token + "tampered";

        // when & then
        assertThat(jwtProvider.validateToken(tamperedToken)).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰은 parseClaims 시 EXPIRED_TOKEN 예외를 던진다")
    void parseClaims_withExpiredToken_throwsExpiredTokenException() {
        // given — expiration을 0ms로 설정해 즉시 만료
        JwtProvider expiredJwtProvider = new JwtProvider(SECRET, 0L);
        String expiredToken = expiredJwtProvider.generateToken(1L, "test@test.com");

        // when & then
        assertThatThrownBy(() -> expiredJwtProvider.parseClaims(expiredToken))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.EXPIRED_TOKEN));
    }

    @Test
    @DisplayName("잘못된 형식의 토큰은 parseClaims 시 INVALID_TOKEN 예외를 던진다")
    void parseClaims_withMalformedToken_throwsInvalidTokenException() {
        // given
        String invalidToken = "this.is.not.a.valid.jwt";

        // when & then
        assertThatThrownBy(() -> jwtProvider.parseClaims(invalidToken))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.INVALID_TOKEN));
    }

    @Test
    @DisplayName("토큰에서 email claim을 정상적으로 파싱한다")
    void parseClaims_extractsEmailClaim() {
        // given
        String email = "user@example.com";
        String token = jwtProvider.generateToken(42L, email);

        // when
        String parsedEmail = (String) jwtProvider.parseClaims(token).get("email");

        // then
        assertThat(parsedEmail).isEqualTo(email);
    }
}
