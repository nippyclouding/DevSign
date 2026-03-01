package devsign_server.api.global.auth;

import devsign_server.api.domain.member.dto.MemberResponse;
import devsign_server.api.global.auth.dto.LoginRequest;
import devsign_server.api.global.auth.dto.LoginResponse;
import devsign_server.api.global.auth.dto.SignupRequest;
import devsign_server.api.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ApiResponse.ok();
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<MemberResponse> getMe(@AuthenticationPrincipal MemberDetails memberDetails) {
        return ApiResponse.ok(authService.getMe(memberDetails.getMemberId()));
    }
}
