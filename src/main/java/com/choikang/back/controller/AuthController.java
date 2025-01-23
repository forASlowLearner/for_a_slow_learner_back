package com.choikang.back.controller;


import com.choikang.back.service.OAuth2UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@CrossOrigin(origins = {"http://localhost:3000/","http://localhost/"}) // 리액트에서 호출할 것이므로 리엑트의 url과 함께 작성
@RestController
@RequiredArgsConstructor
public class AuthController {
    private final OAuth2UserService oAuth2UserService;

    @GetMapping("/oauth2/kakao")
    public ResponseEntity<String> kakaoCallback(@RequestParam("code") String code, HttpServletResponse response) throws Exception {
        String jwtToken = oAuth2UserService.kakaoLogin(code);

        // JWT를 쿠키에 저장
        Cookie cookie = new Cookie("token",  jwtToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);

        // JWT를 URL 파라미터로 전달하면서 리다이렉트
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("http://localhost:3000/login-success")
                .queryParam("token", jwtToken);

        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", uriBuilder.toUriString())
                .build();
    }


    @GetMapping("/logout")
    public ResponseEntity<Object> logout(HttpServletRequest request, HttpServletResponse response) throws Exception{
        String token = oAuth2UserService.getJWTFromCookies(request);

        if(token != null){
            oAuth2UserService.kakaoLogout(token);
            response.addCookie(oAuth2UserService.deleteJWTFromCookie());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @GetMapping("/validate")
    public ResponseEntity<Object> validateToken(HttpServletRequest request, HttpServletResponse response) throws Exception {
        try {
            String jwtToken = oAuth2UserService.validateTokenAndRegenerate(request);

            // JWT를 쿠키에 저장
            Cookie cookie = new Cookie("token", jwtToken);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            response.addCookie(cookie);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰 검증 실패" + e.getMessage());
        }
    }
}