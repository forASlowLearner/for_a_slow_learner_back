package com.choikang.back.controller;


import com.choikang.back.security.util.JWTUtil;
import com.choikang.back.service.MemberService;
import com.choikang.back.service.OAuth2UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = {"http://localhost:3000/","http://localhost/"}) // 리액트에서 호출할 것이므로 리엑트의 url과 함께 작성
@RestController
@RequiredArgsConstructor
public class AuthController {
    private final OAuth2UserService oAuth2UserService;
    private final MemberService memberService;
    private final JWTUtil jwtUtil;

//    이건 백엔드를 리다이렉트 uri로 설정한 경우임
//    @GetMapping("/oauth2/kakao")
//    public ResponseEntity<String> kakaoCallback(@RequestParam("code") String code, HttpServletResponse response) throws Exception {
//
//        String jwtToken = oAuth2UserService.kakaoLogin(code);
//
//        // JWT를 쿠키에 저장
//        Cookie cookie = new Cookie("token",  jwtToken);
//        cookie.setHttpOnly(true);
//        cookie.setPath("/");
//        response.addCookie(cookie);
//
//        // JWT에서 회원 ID 추출
//        String subject = jwtUtil.getUserInfoFromToken(jwtToken);
//        Long memberId = Long.parseLong(subject.split(":")[0]); // 콜론 기준 첫 번째 요소
//
//        // JSON 문자열 생성
//        String jsonBody = String.format(
//                "{\"token\":\"%s\",\"memberId\":%d}",
//                jwtToken.replace("\"", "\\\""), // JSON 이스케이프 처리
//                memberId
//        );
//
//
//
//
//
//        // JWT를 URL 파라미터로 전달하면서 리다이렉트
//        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("http://localhost:3000/login-success")
//                .queryParam("token", jwtToken);
//
//        return ResponseEntity.ok()
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(jsonBody);
//    }


    //이것도 프론트로 리다이렉트 uri 쓰기 시작하는 순간 더이상 쓸일 없음
    @GetMapping("/oauth2/kakao")
    public ResponseEntity<String> kakaoCallback(@RequestParam("code") String code) {
        String responseBody = "카카오 인가 코드: " + code;

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("text/plain;charset=UTF-8"))  // charset 명시적 설정
                .body(responseBody);
    }


//    // 실제 로그인 처리 (프론트엔드에서 이쪽으로 인가코드담아서 요청예정)
//    @GetMapping("/oauth2/kakao/login")
//    public ResponseEntity<Map<String, Object>> processLogin(
//            @RequestParam("code") String code,
//            HttpServletResponse response
//    ) throws Exception {
//
//        // 1. JWT 토큰 생성
//        String jwtToken = oAuth2UserService.kakaoLogin(code);
//
//        // 2. 회원 ID 추출
//        String subject = jwtUtil.getUserInfoFromToken(jwtToken);
//        Long memberId = Long.parseLong(subject.split(":")[0]);
//
//        // 3. 응답 데이터 구성
//        Map<String, Object> responseBody = new HashMap<>();
//        responseBody.put("token", jwtToken);
//        responseBody.put("memberId", memberId);
//
//        // 4. (옵션) 쿠키 설정
//        ResponseCookie cookie = ResponseCookie.from("auth_token", jwtToken)
//                .httpOnly(true)
//                .path("/")
//                .maxAge(7 * 24 * 60 * 60)
//                .build();
//        response.addHeader("Set-Cookie", cookie.toString());
//
//        return ResponseEntity.ok()
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(responseBody);
//    }

    @GetMapping("/oauth2/kakao/login")
    public ResponseEntity<String> processLogin(
            @RequestParam("code") String code,
            HttpServletResponse response
    ) throws Exception {

        // 1. JWT 토큰 생성
        String jwtToken = oAuth2UserService.kakaoLogin(code);

        // 2. 헤더에 액세스 토큰 설정 (실전 표준)
        response.setHeader("Authorization", "Bearer " + jwtToken);

        // 3. (옵션) 쿠키 설정
        ResponseCookie cookie = ResponseCookie.from("auth_token", jwtToken)
                .httpOnly(true)
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        // 4. 최소한의 응답 본문
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("text/plain;charset=UTF-8"))
                .body("로그인이 완료되었습니다.");
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

    // 최초 가입 질문 저장 endpoint
    @PostMapping("/api/save-user-info")
    public ResponseEntity<Map<String, String>> saveUserInfo(@RequestBody Map<String, Object> request) {
        Map<String, String> response = new HashMap<>();
        try {
            // JSON 요청: {"member_id":1, "name":"백지완", "birthday":"1999-07-05"}
            int memberId = (Integer) request.get("member_id");
            String name = (String) request.get("name");
            String birthdayStr = (String) request.get("birthday");
            LocalDate birthday = LocalDate.parse(birthdayStr);

            memberService.saveUserInfo(memberId, name, birthday);
            response.put("message", "최초 질문 저장에 성공했습니다");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.put("message", "알 수 없는 서버 에러가 발생했습니다");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 최초 가입 질문 점수 저장 endpoint
    @PostMapping("/api/submit-answers")
    public ResponseEntity<Map<String, String>> submitAnswers(@RequestBody Map<String, Object> request) {
        Map<String, String> response = new HashMap<>();
        try {
            // JSON 요청: {"member_id":1, "score":6}
            int memberId = (Integer) request.get("member_id");
            int score = (Integer) request.get("score");

            memberService.submitAnswers(memberId, score);
            response.put("message", "최초 질문 제출에 성공했습니다");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.put("message", "알 수 없는 서버 에러가 발생했습니다");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}