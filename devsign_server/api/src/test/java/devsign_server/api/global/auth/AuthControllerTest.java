package devsign_server.api.global.auth;

import devsign_server.api.domain.member.entity.Member;
import devsign_server.api.domain.member.entity.Section;
import devsign_server.api.fixture.MemberFixture;
import devsign_server.api.global.auth.dto.LoginRequest;
import devsign_server.api.global.auth.dto.LoginResponse;
import devsign_server.api.global.auth.dto.SignupRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AuthController 통합 테스트.
 * Spring Boot 4.x에서는 @WebMvcTest 슬라이스가 제거되어
 * @SpringBootTest + MockMvc 방식을 사용한다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@DisplayName("AuthController 통합 테스트")
class AuthControllerTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(springSecurity())
                .build();
    }

    // ================================
    // POST /api/auth/signup
    // ================================

    @Test
    @DisplayName("회원가입 성공 시 201 Created를 반환한다")
    void signup_success_returns201() throws Exception {
        // given
        SignupRequest request = new SignupRequest("김개발", Section.DEVELOPER, "dev@test.com", "password123");
        willDoNothing().given(authService).signup(any(SignupRequest.class));

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("이메일 형식이 잘못된 경우 400 Bad Request를 반환한다")
    void signup_invalidEmail_returns400() throws Exception {
        // given
        SignupRequest request = new SignupRequest("김개발", Section.DEVELOPER, "not-an-email", "password123");

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("비밀번호가 8자 미만인 경우 400 Bad Request를 반환한다")
    void signup_shortPassword_returns400() throws Exception {
        // given
        SignupRequest request = new SignupRequest("김개발", Section.DEVELOPER, "dev@test.com", "pass");

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("이름이 공백인 경우 400 Bad Request를 반환한다")
    void signup_blankName_returns400() throws Exception {
        // given
        SignupRequest request = new SignupRequest("  ", Section.DEVELOPER, "dev@test.com", "password123");

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ================================
    // POST /api/auth/login
    // ================================

    @Test
    @DisplayName("로그인 성공 시 200 OK와 JWT 토큰을 반환한다")
    void login_success_returns200() throws Exception {
        // given
        Member member = MemberFixture.developer();
        LoginRequest request = new LoginRequest("dev@test.com", "password123");
        LoginResponse loginResponse = LoginResponse.of(member, "jwt-access-token");

        given(authService.login(any(LoginRequest.class))).willReturn(loginResponse);

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt-access-token"))
                .andExpect(jsonPath("$.data.email").value(member.getEmail()));
    }

    // ================================
    // GET /api/auth/me
    // ================================

    @Test
    @DisplayName("인증된 사용자가 /me 조회 시 200 OK를 반환한다")
    void getMe_authenticated_returns200() throws Exception {
        // given
        Member member = MemberFixture.developer();
        MemberDetails memberDetails = new MemberDetails(member);
        var auth = new UsernamePasswordAuthenticationToken(
                memberDetails, null, memberDetails.getAuthorities());

        given(authService.getMe(member.getMemberId()))
                .willReturn(devsign_server.api.domain.member.dto.MemberResponse.from(member));

        // when & then
        mockMvc.perform(get("/api/auth/me")
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(member.getEmail()));
    }

    @Test
    @DisplayName("인증되지 않은 사용자가 /me 조회 시 401 Unauthorized를 반환한다")
    void getMe_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
