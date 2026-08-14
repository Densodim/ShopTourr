package com.shoptourr.api.web;

import com.shoptourr.api.v1.dto.user.UserDtos.ClientRemoteConfigDto;
import com.shoptourr.api.v1.dto.user.UserDtos.UpdatePreferencesRequest;
import com.shoptourr.api.v1.dto.user.UserDtos.UpdateProfileRequest;
import com.shoptourr.api.v1.dto.user.UserDtos.UserDto;
import com.shoptourr.api.v1.dto.user.UserDtos.UserPreferencesDto;
import com.shoptourr.application.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/me", version = "1")
public class UserController {

    private final UserService users;

    public UserController(UserService users) {
        this.users = users;
    }

    @GetMapping
    UserDto me(Authentication authentication) {
        return users.me(CurrentUser.id(authentication));
    }

    @PatchMapping
    UserDto update(@Valid @RequestBody UpdateProfileRequest request, Authentication authentication) {
        return users.updateProfile(CurrentUser.id(authentication), request);
    }

    @GetMapping("/preferences")
    UserPreferencesDto preferences(Authentication authentication) {
        return users.preferences(CurrentUser.id(authentication));
    }

    @PatchMapping("/preferences")
    UserPreferencesDto updatePreferences(
            @Valid @RequestBody UpdatePreferencesRequest request,
            Authentication authentication
    ) {
        return users.updatePreferences(CurrentUser.id(authentication), request);
    }

    @GetMapping("/app-config")
    ClientRemoteConfigDto appConfig() {
        return users.appConfig();
    }
}
