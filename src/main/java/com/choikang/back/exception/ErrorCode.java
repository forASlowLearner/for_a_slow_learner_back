package com.choikang.back.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum ErrorCode {
    //400 BAD REQUEST

    //401 UNAUTHORIZED
    EXPIRED_TOKEN_EXCEPTION(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    //404 NOT FOUND
    NOT_FOUND_USER_EXCEPTION(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다.");

    //500 INTERNAL SERVER ERROR

    private final HttpStatus httpStatus;
    private final String message;
}

