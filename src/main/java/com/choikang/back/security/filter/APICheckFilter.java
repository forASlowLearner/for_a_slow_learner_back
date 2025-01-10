package com.choikang.back.security.filter;

import com.choikang.back.security.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class APICheckFilter extends OncePerRequestFilter {
    private AntPathMatcher antPathMatcher; // 스프링부트에서 path의 패턴 스타일을 매칭할 때 사용하는 클래스
    private String pattern;
    private JWTUtil jwtUtil;

    public APICheckFilter(String pattern, JWTUtil jwtUtil){
        this.antPathMatcher = new AntPathMatcher();
        this.pattern = pattern;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException{
        if (antPathMatcher.match("/oauth2/kakao", request.getRequestURI()) || antPathMatcher.match("/api/logout", request.getRequestURI())){
            filterChain.doFilter(request, response);
            return;
        }

        if (antPathMatcher.match(pattern, request.getRequestURI())) {
            boolean checkHeader = checkAuthHeader(request);
            if (!checkHeader) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean checkAuthHeader(HttpServletRequest request){
        boolean checkResult = false;

        // Authrizaion 헤더 가져오기
        String authHeader = request.getHeader("Authorization");

        // 헤더가 유효한 지(값이 있는지 ,+ Bearer 로 시작하는지 ) 확인
        if(StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer")){
            try{
                // jwt 토큰 추출: 'Bearer '를 제외한 나머지 토큰을 추출함
                String userIDStr = jwtUtil.validateAndExtract(authHeader.substring(7));

                if(userIDStr != null){
                    String[] parts = userIDStr.split(":");
                    if(parts.length > 0){
                        int userID = Integer.parseInt(parts[0]);
                        checkResult = (userID > 0);
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return checkResult;
    }
}