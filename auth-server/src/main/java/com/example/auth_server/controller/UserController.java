package com.example.auth_server.controller;

import com.example.auth_server.dto.LoginRequest;
import com.example.auth_server.dto.LoginResponse;
import com.example.auth_server.dto.circuitbreaker.DelayResponse;
import com.example.auth_server.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final JwtUtil jwtUtil;

    @PostMapping("/auth/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return new LoginResponse(jwtUtil.generateToken(request.username()));
    }

    @GetMapping("/users/delay")
    public DelayResponse delay() throws InterruptedException {
        Thread.sleep(3000);
        return new DelayResponse("delay response", LocalDateTime.now().toString());
    }
}
