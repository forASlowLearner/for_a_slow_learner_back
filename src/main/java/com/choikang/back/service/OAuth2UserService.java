package com.choikang.back.service;

import com.choikang.back.dto.KakaoUserDTO;
import com.choikang.back.entity.Member;
import com.choikang.back.repository.MemberRepository;
import com.choikang.back.security.util.JWTUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Log4j2
@Service
@RequiredArgsConstructor
@Lazy
public class OAuth2UserService extends DefaultOAuth2UserService {
    private final MemberRepository memberRepository;
    private final JWTUtil jwtUtil;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String kakaoClientID;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String redirectURL;

    @Value("${spring.security.oauth2.client.registration.kakao.client-secret}")
    private String kakaoClientSecret;


    // 카카오 로그인, JWT 생성 후 return
    public String kakaoLogin(String code) throws Exception {
        String accessToken = getKakaoAccessToken(code);
        KakaoUserDTO kakaoUserDTO = getKakaoUserInfo(accessToken);

        // 사용자 정보를 바탕으로 User 엔티티 저장 또는 업데이트
        int member = saveMember(kakaoUserDTO);

        // user id를 통해서 JWT 토큰 생성
        return jwtUtil.generateToken(member + ":" + accessToken);
    }

//    // 카카오 개발자 도구에 POST 요청하여 사용자 인증 수단 (액세스 토큰)가져옴
//    public String getKakaoAccessToken(String code) {
//        RestTemplate restTemplate = new RestTemplate();
//
//        // 요청 파라미터 설정
//        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
//        parameters.add("grant_type", "authorization_code");
//        parameters.add("client_id", kakaoClientID);
//        parameters.add("client_secret", kakaoClientSecret);
//        parameters.add("redirect_uri", redirectURL);
//        parameters.add("code", code);
//
//        // 요청 헤더 설정
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//        headers.add("charset", "utf-8");
//
//        // 클라이언트 자격증명을 Basic Auth 헤더에 추가
//        String credentials = kakaoClientID + ":" + kakaoClientSecret;
//        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
//        headers.add("Authorization", "Basic " + encodedCredentials);
//
//        // 요청 엔티티 생성
//        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(parameters, headers);
//
//        try {
//            // POST 요청을 보내 액세스 토큰을 얻음
//            ResponseEntity<String> response = restTemplate.postForEntity("https://kauth.kakao.com/oauth/token", request,
//                    String.class);
//            System.out.println("응답은 말이지 " + response);
//
//            if (response.getStatusCode() == HttpStatus.OK) {
//                String responseBody = response.getBody();
//                if (responseBody != null) {
//                    // JSON 응답 파싱
//                    ObjectMapper objectMapper = new ObjectMapper();
//                    JsonNode root = objectMapper.readTree(responseBody);
//                    JsonNode accessTokenNode = root.path("access_token");
//                    if (!accessTokenNode.isMissingNode()) {
//                        return accessTokenNode.asText();
//                    } else {
//                        log.error("액세스 토큰 필드가 응답에 없습니다.");
//                    }
//                } else {
//                    log.error("응답 본문이 null입니다.");
//                }
//            } else {
//                log.error("액세스 토큰을 가져오는 중 오류 발생1: " + response.getStatusCode());
//            }
//        } catch (Exception e) {
//            log.error("액세스 토큰을 가져오는 중 오류 발생2: " + e.getMessage());
//        }
//        return "Error";
//    }
public String getKakaoAccessToken(String code) {
    RestTemplate restTemplate = new RestTemplate();

    // 요청 파라미터 설정
    MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
    parameters.add("grant_type", "authorization_code");
    parameters.add("client_id", kakaoClientID);
    parameters.add("client_secret", kakaoClientSecret);
    parameters.add("redirect_uri", redirectURL);
    parameters.add("code", code);

    // 요청 헤더 설정
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    headers.add("charset", "utf-8");

    // 요청 엔티티 생성
    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(parameters, headers);

    try {
        // POST 요청을 보내 액세스 토큰을 얻음
        ResponseEntity<String> response = restTemplate.postForEntity("https://kauth.kakao.com/oauth/token", request, String.class);

        // 응답 상태 코드 확인
        if (response.getStatusCode() == HttpStatus.OK) {
            String responseBody = response.getBody();
            if (responseBody != null) {
                // JSON 응답 파싱
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode root = objectMapper.readTree(responseBody);
                JsonNode accessTokenNode = root.path("access_token");
                if (!accessTokenNode.isMissingNode()) {
                    return accessTokenNode.asText();
                } else {
                    log.error("액세스 토큰 필드가 응답에 없습니다.");
                }
            } else {
                log.error("응답 본문이 null입니다.");
            }
        } else {
            log.error("액세스 토큰을 가져오는 중 오류 발생: " + response.getStatusCode() + " - " + response.getBody());
            throw new RuntimeException("액세스 토큰 요청 실패: " + response.getStatusCode());
        }
    } catch (Exception e) {
        log.error("액세스 토큰을 가져오는 중 오류 발생: {}", e.getMessage());
        throw new RuntimeException("액세스 토큰을 가져오는 중 오류 발생", e);
    }
    return "Error";
}

    // 카카오에서 엑세스 토큰을 통해 유저 정보 가져옴
    public KakaoUserDTO getKakaoUserInfo(String accessToken) {
        String reqURL = "https://kapi.kakao.com/v2/user/me";

        RestTemplate restTemplate = new RestTemplate();

        // HttpHeaders 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        // 요청 헤더를 포함한 HttpEntity 생성
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            // GET 요청 보내기
            ResponseEntity<Map> response = restTemplate.exchange(reqURL, HttpMethod.GET, entity, Map.class);

            // 응답 처리
            Map<String, Object> body = response.getBody();
            log.info("Kakao API Response: {}", body); // 응답 로그 추가

            if (body != null) {
                Map<String, Object> account = (Map<String, Object>) body.get("kakao_account");
                if (account == null) {
                    throw new RuntimeException("Kakao account 정보가 응답에 없습니다."); // 추가된 예외 처리
                }
                String email = (String) account.get("email"); // 이메일만 가져옴
                return new KakaoUserDTO(email); // 이메일을 포함한 DTO 반환
            } else {
                throw new RuntimeException("Kakao API 응답 본문이 null입니다.");
            }
        } catch (Exception e) {
            log.error("Kakao API 호출 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("Kakao API 호출 중 오류 발생", e);
        }
    }


    private int saveMember(KakaoUserDTO kakaoUserDTO) {
        Member existingMember = memberRepository.findByEmail(kakaoUserDTO.getEmail()); //이미 있는 회원인지를 찾기

        if (existingMember == null) {
            Member member = Member.builder()
                    .email(kakaoUserDTO.getEmail())
                    .nickName(null) // 닉네임 설정
                    .birthday(null) // 생일 정보가 있다면 추가
                    .isSlower(false) // 기본값 설정
                    .score(0) // 기본값 설정
                    .signUpDate(LocalDate.now()) // 현재 날짜 설정
                    .build();
            Member savedMember = memberRepository.save(member);
            return savedMember.getMemberId(); //새로 생성된 회원의 id 반환
        } else {
            return existingMember.getMemberId(); //기존 회원의 id 반환
        }
    }

    // 쿠키로 부터 JWT 값 가져오기
    public String getJWTFromCookies(HttpServletRequest request) {
        String token = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                if (cookies[i].getName().equals("token")) {
                    token = cookies[i].getValue();
                    break;
                }
            }
        }
        return token;
    }

    // 쿠키 삭제
    public Cookie deleteJWTFromCookie() {
        Cookie cookie = new Cookie("token", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        cookie.setValue(null);
        return cookie;
    }

    // JWT로부터 user 정보 가져오기
    public int getUserInfo(String token) throws Exception {
        String userInfo = jwtUtil.getUserInfoFromToken(token);

        int userId;
        try {
            userId = Integer.parseInt(userInfo.split(":")[0]);
        } catch (NumberFormatException e) {
            throw new RuntimeException("유효하지 않은 사용자 ID 형식입니다.", e);
        }

        Optional<Member> user = memberRepository.findById(userId);

        if (user.isPresent()) {
            return userId;
        } else {
            throw new RuntimeException("유효하지 않은 사용자 정보입니다.");
        }
    }

    // JWT로부터 user id 가져오기
    public Long getUserIDFromJWT(String token) throws Exception {
        String userInfo = String.valueOf(getUserInfo(token));
        String[] parts = userInfo.split(":");

        if (parts.length < 2) {
            throw new RuntimeException("유효하지 않은 사용자 정보 형식입니다.");
        }

        try {
            return Long.parseLong(parts[0]); // Long으로 변환
        } catch (NumberFormatException e) {
            throw new RuntimeException("유효하지 않은 사용자 ID 형식입니다.", e);
        }
    }


    // JWT로부터 user access token 가져오기
    public String getUserAccessTokenFromJWT(String token) throws Exception {
        String userInfo = String.valueOf(getUserInfo(token));
        String[] parts = userInfo.split(":");

        if (parts.length < 2) {
            throw new RuntimeException("유효하지 않은 사용자 정보 형식입니다.");
        }

        return parts[1]; // access token 반환
    }


    // JWT가 유효한지 검사하고 만일 만료되었을 시 재발급 하기
    public String validateTokenAndRegenerate(HttpServletRequest request) throws Exception {
        String token = getJWTFromCookies(request);
        try {
            if (jwtUtil.isTokenExpired(token)) {
                String newToken = jwtUtil.refreshToken(token);
                return newToken;
            }
        } catch (ExpiredJwtException e) {
            try {
                String newToken = jwtUtil.refreshToken(token);
                return newToken;
            } catch (Exception ex) {
                throw new Exception("Token validation failed", ex);
            }
        }
        return token;
    }

    // 카카오 로그아웃 api get 요청
    public void kakaoLogout(String token) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        String logoutUrl = "https://kapi.kakao.com/v1/user/logout";

        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getUserAccessTokenFromJWT(token));

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(logoutUrl, HttpMethod.GET, requestEntity, Map.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed logout from kakao");
        }
    }

}