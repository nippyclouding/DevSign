package devsign_server.api.global.auth;

import devsign_server.api.domain.member.dto.MemberResponse;
import devsign_server.api.global.auth.dto.LoginRequest;
import devsign_server.api.global.auth.dto.LoginResponse;
import devsign_server.api.global.auth.dto.SignupRequest;
import devsign_server.api.global.common.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${app.cookie.secure}")
    private boolean cookieSecure;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ApiResponse.ok();
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                            HttpServletResponse response) {
        LoginResponse loginResponse = authService.login(request);
        ResponseCookie cookie = ResponseCookie.from("jwt", loginResponse.token())
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .maxAge(Duration.ofMillis(jwtExpiration))
                .path("/")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ApiResponse.ok(loginResponse);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .maxAge(0)
                .path("/")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ApiResponse.ok();
    }

    @GetMapping("/me")
    public ApiResponse<MemberResponse> getMe(@AuthenticationPrincipal MemberDetails memberDetails) {
        return ApiResponse.ok(authService.getMe(memberDetails.getMemberId()));
    }
}
