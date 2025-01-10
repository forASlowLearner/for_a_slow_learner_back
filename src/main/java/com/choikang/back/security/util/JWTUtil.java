package com.choikang.back.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClock;
import io.jsonwebtoken.security.Keys;
import java.time.ZonedDateTime;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;

@Component
@Log4j2
public class JWTUtil {
    @Value("${jwt.util.secretkey}")
    private String secretKey;

    @Value("${jwt.util.expire}")
    private long expire;

    // 비밀 키를 SecretKey 객체로 변환
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    // 토큰 생성
    public String generateToken(String content) {
        SecretKey secretKeyDecoded = getSecretKey();

        return Jwts.builder()
                .setIssuedAt(new Date())
                .setSubject(content) // jwt에 저장되는 값, 보통 식별되는 값
                .setExpiration(Date.from(ZonedDateTime.now().plus(Duration.ofMillis(expire)).toInstant()))
                .signWith(secretKeyDecoded)
                .compact();
    }

    // 토큰으로부터 user 정보 가져오기
    public String getUserInfoFromToken(String tokenStr) throws Exception {
        try {
            Jws<Claims> jwt = Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .setClock(DefaultClock.INSTANCE)
                    .build()
                    .parseClaimsJws(tokenStr);

            Claims claims = jwt.getBody();
            return claims.getSubject();

        } catch (ExpiredJwtException e) {
            log.error("토큰이 만료되었습니다: " + e.getMessage());
            throw new Exception("토큰이 만료되었습니다", e);
        } catch (JwtException e) {
            log.error("JWT 예외 발생: " + e.getMessage());
            throw new Exception("토큰 파싱 실패", e);
        }
    }

    // 토큰 검증
    public String validateAndExtract(String tokenStr) {
        String contentValue = null;
        try {
            Jws<Claims> jws = Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(tokenStr);

            String subject = jws.getBody().getSubject();
            if (subject != null) {
                contentValue = subject;
            }
        } catch (NumberFormatException e) {
            log.error("ID 형식이 올바르지 않습니다. 에러 내용: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return contentValue;
    }

    // 토큰 재발급
    public String refreshToken(String oldToken) throws Exception {
        try {
            Jws<Claims> jws = Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(oldToken);

            String subject = jws.getBody().getSubject();
            return generateToken(subject);

        } catch (ExpiredJwtException e) {
            Claims claims = e.getClaims();
            String subject = claims.getSubject();
            return generateToken(subject);
        } catch (JwtException e) {
            throw new Exception("Token refresh failed: " + e.getMessage(), e);
        }
    }

    // 토큰 만료 여부 확인
    public boolean isTokenExpired(String token) throws Exception {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token);
            return false;
        } catch (ExpiredJwtException e) {
            return true;
        }
    }
}
