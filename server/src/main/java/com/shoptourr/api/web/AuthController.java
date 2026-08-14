package com.shoptourr.api.web;

import com.shoptourr.api.v1.dto.auth.AuthDtos.AuthTokensResponse;
import com.shoptourr.api.v1.dto.auth.AuthDtos.ForgotPasswordRequest;
import com.shoptourr.api.v1.dto.auth.AuthDtos.LoginRequest;
import com.shoptourr.api.v1.dto.auth.AuthDtos.LogoutRequest;
import com.shoptourr.api.v1.dto.auth.AuthDtos.RefreshTokenRequest;
import com.shoptourr.api.v1.dto.auth.AuthDtos.RegisterRequest;
import com.shoptourr.application.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/auth", version = "1")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    AuthTokensResponse register(@Valid @RequestBody RegisterRequest request) {
        return auth.register(request);
    }

    @PostMapping("/login")
    AuthTokensResponse login(@Valid @RequestBody LoginRequest request) {
        return auth.login(request);
    }

    @PostMapping("/refresh")
    AuthTokensResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return auth.refresh(request);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void logout(@RequestBody(required = false) LogoutRequest request) {
        auth.logout(request == null ? new LogoutRequest(null, false) : request);
    }

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void forgot(@Valid @RequestBody ForgotPasswordRequest request) {
        auth.forgotPassword(request.email());
    }
}
