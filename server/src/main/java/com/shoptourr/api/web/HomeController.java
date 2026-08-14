package com.shoptourr.api.web;

import com.shoptourr.api.v1.dto.home.HomeDtos.HomeResponse;
import com.shoptourr.application.HomeService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/home", version = "1")
public class HomeController {

    private final HomeService home;

    public HomeController(HomeService home) {
        this.home = home;
    }

    @GetMapping
    HomeResponse home(Authentication authentication) {
        return home.home(CurrentUser.id(authentication));
    }
}
